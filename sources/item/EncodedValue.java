package item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class EncodedValue {

	int name;
	int valueType;
	int valueArg;
	Byte[] value;
	EncodedAnnotation annotation;
	EncodedArray array;
	
	public EncodedValue(int name, int valueType, int valueArg) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
	}
	
	public EncodedValue(int name, int valueType, int valueArg, Byte[] value) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
		this.value = value;
	}
	
	public EncodedValue(int name, int valueType, int valueArg, EncodedAnnotation annotation) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
		this.annotation = annotation;
	}

	public EncodedValue(int name, int valueType, int valueArg, EncodedArray array) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
		this.array = array;
	}
	
	public Collection<Byte> getData(long[] fieldMap, long[] methodMap, long[] stringMap, long[] typeMap) {
		int ret = (valueArg << 5) | valueType;
		ArrayList<Byte> byteRet = new ArrayList<Byte>();
		byteRet.add((byte)ret);
		
		if (valueType == 0x17) {
			long val = readUint(value);
			long newVal = stringMap[(int)val];
			Byte[] blah = writeUint(newVal);
			for (int i = 0; i < blah.length; ++i) {
				byteRet.add(blah[i]);
			}
		} else if (valueType == 0x18) {
			long val = readUint(value);
			long newVal = typeMap[(int)val];
			Byte[] blah = writeUint(newVal);
			for (int i = 0; i < blah.length; ++i) {
				byteRet.add(blah[i]);
			}
		} else if (valueType == 0x19 || valueType == 0x1B) {
			long val = readUint(value);
			long newVal = fieldMap[(int)val];
			Byte[] blah = writeUint(newVal);
			for (int i = 0; i < blah.length; ++i) {
				byteRet.add(blah[i]);
			}
		} else if (valueType == 0x1A) {
			long val = readUint(value);
			long newVal = methodMap[(int)val];
			Byte[] blah = writeUint(newVal);
			for (int i = 0; i < blah.length; ++i) {
				byteRet.add(blah[i]);
			}
		} else if (value != null) {
			for (int i = 0; i < value.length; ++i) {
				byteRet.add(value[i]);
			}
		} else if (annotation != null) {
			byteRet.addAll(annotation.getData(fieldMap, methodMap, stringMap, typeMap));
		} else if (array != null){
			byteRet.addAll(array.getData(fieldMap, methodMap, stringMap, typeMap));
		}
		return byteRet;
	}
	
	public int read16Bit(Byte[] data) {
    	int size = data[0];
        size |= (data[1] << 8 );
        return size;
    }
	
	public long readUint(Byte[] bytes) {
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value |= (((long)(bytes[i] & 0xFF)) << i * 8);
        }
        return value;
    }
	
	public Byte[] writeUint(long value) {
        int requiredBytes = getRequiredBytes(value);

        Byte[] bytes = new Byte[requiredBytes];

        for (int i = 0; i < requiredBytes; i++) {
            bytes[i] = (byte) value;
            value >>= 8;
        }
        return bytes;
    }
	
	public byte getRequiredBytes(long value) {
        int requiredBits = 64 - Long.numberOfLeadingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }

        return (byte)((requiredBits + 0x07) >> 3);
    }
}
