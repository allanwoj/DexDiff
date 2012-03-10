package item;

import java.util.ArrayList;
import java.util.Collection;

public class EncodedAnnotation {
	private int type;
	private int size;
	public EncodedValue[] values;
	
	public EncodedAnnotation(int type, int size) {
		this.type = type;
		this.size = size;
		values = new EncodedValue[size];
	}
	
	public int getType() {
		return type;
	}

	public int getSize() {
		return size;
	}
	
	public Collection<Byte> getData(long[] fieldMap, long[] methodMap, long[] stringMap, long[] typeMap) {
		ArrayList<Byte> ret = new ArrayList<Byte>();
		ret.addAll(getULeb128((int)typeMap[type]));
		ret.addAll(getULeb128(size));
		int name = 0;
		for (int i = 0; i < size; ++i) {
			name = (int)stringMap[values[i].name];
			ret.addAll(getULeb128(name));
			ret.addAll(values[i].getData(fieldMap, methodMap, stringMap, typeMap));
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
