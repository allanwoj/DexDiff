package item;

import java.util.ArrayList;
import java.util.Collection;

public class EncodedArray {
	int size;
	EncodedValue[] values;
	
	public EncodedArray(int size, EncodedValue[] values) {
		this.size = size;
		this.values = values;
	}
	
	public boolean isEqual(EncodedArray other, long[] fieldIndexMap, long[] methodIndexMap, long[] stringIndexMap, long[] typeIndexMap) {
		if (size != other.size)
			return false;
		
		for (int i = 0; i < values.length; ++i) {
			if (!values[i].isEqual(other.values[i], fieldIndexMap, methodIndexMap, stringIndexMap, typeIndexMap)) {
				return false;
			}
		}
		
		return true;
	}
	
	public Collection<Byte> getData(long[] fieldMap, long[] methodMap, long[] stringMap, long[] typeMap) {
		ArrayList<Byte> ret = new ArrayList<Byte>();
		ret.addAll(getULeb128(size));
		
		for(int i = 0; i < size; ++i) {
			ret.addAll(values[i].getData(fieldMap, methodMap, stringMap, typeMap));
		}
		return ret;
	}
	
	public Collection<Byte> getOutput() {
		ArrayList<Byte> ret = new ArrayList<Byte>();
		ret.addAll(getULeb128(size));
		
		for(int i = 0; i < size; ++i) {
			ret.addAll(values[i].getOutput());
		}
		return ret;
	}
	
	Collection<Byte> getULeb128(int value) {
		ArrayList<Byte> ret = new ArrayList<Byte>();
		long remaining = (value & 0xFFFFFFFFL) >> 7;
        long lValue = value;
        int count = 0;

        while (remaining != 0) {
        	ret.add((byte) ((lValue & 0x7f) | 0x80));
            lValue = remaining;
            remaining >>= 7;
            count++;
        }
        ret.add((byte)(lValue & 0x7f));
        return ret;
	}
}
