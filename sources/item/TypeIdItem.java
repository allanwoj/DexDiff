package item;

import java.util.ArrayList;
import java.util.List;

import patch.MapManager;

public class TypeIdItem extends DexItem<TypeIdItem> {

	public int stringIndex;

	public TypeIdItem(int stringIndex) {
		this.stringIndex = stringIndex;
	}

	public boolean isEqual(TypeIdItem other, MapManager mm) {
		return mm.stringIndexMap[stringIndex] == other.stringIndex;
	}

	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();

		byte[] temp = write32bit(4L);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(stringIndex);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);

		return l;
	}

	public List<Byte> getModifiedData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();

		byte[] temp = write32bit(mm.stringIndexMap[stringIndex]);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);

		return l;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
}
