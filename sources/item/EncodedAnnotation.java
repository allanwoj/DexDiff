package item;

import java.util.ArrayList;
import java.util.Collection;

import patch.MapManager;

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
	
	public boolean isEqual(EncodedAnnotation other, MapManager mm) {
		if (size != other.size || mm.typeIndexMap[type] != other.type)
			return false;
		
		for (int i = 0; i < size; ++i) {
			if (!values[i].isEqual(other.values[i], mm)) {
				return false;
			}
		}
		
		return true;
	}
	
	public Collection<Byte> getData(MapManager mm) {
		ArrayList<Byte> ret = new ArrayList<Byte>();
		ret.addAll(getULeb128((int)mm.typeIndexMap[type]));
		ret.addAll(getULeb128(size));
		int name = 0;
		for (int i = 0; i < size; ++i) {
			name = (int)mm.stringIndexMap[values[i].name];
			ret.addAll(getULeb128(name));
			ret.addAll(values[i].getData(mm));
		}
		return ret;
	}
	
	public Collection<Byte> getOutput() {
		ArrayList<Byte> ret = new ArrayList<Byte>();
		ret.addAll(getULeb128(type));
		ret.addAll(getULeb128(size));
		int name = 0;
		for (int i = 0; i < size; ++i) {
			name = values[i].name;
			ret.addAll(getULeb128(name));
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
