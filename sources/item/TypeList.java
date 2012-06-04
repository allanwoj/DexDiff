package item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import patch.MapManager;

public class TypeList extends DexItem<TypeList> {

	public long size;
	public int[] types;
	
	public TypeList(long size, int[] types) {
		this.size = size;
		this.types = types;
	}
	
	public List<Byte> getModifiedData(MapManager mm) {
		return null;
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		
		int length = 4 + 2 * (int)size;
		if (size % 2 == 1) {
			length += 2;
		}
		
		byte[] temp = write32bit((long)length);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit((long)size);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		for (int i = 0; i < size; ++i) {
			temp = write16bit(types[i]);
			for (int j = 0 ; j < 2; ++j) {
				l.add(temp[j]);
			}
		}
		
		if (size % 2 == 1) {
			l.add((byte) 0);
			l.add((byte) 0);
		}
		
		return l;
	}
	
	public boolean isEqual(TypeList other, MapManager mm) {
		if (size != other.size) {
        	return false;
        }
		for(int k = 0; k < size; ++k) {
        	if(mm.typeIndexMap[types[k]] != other.types[k]) {
        		return false;
        	}
        }
		return true;
	}
	
	public byte[] write16bit(long data) {
		byte[] output = new byte[2];
		
		for(int i = 0; i < 2; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}

	
	
}
