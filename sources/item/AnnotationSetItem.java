package item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import patch.MapManager;

public class AnnotationSetItem extends DexItem<AnnotationSetItem> {

	public long size;
	public int[] entriesIndex;
	public long[] entriesOffsets;

	public AnnotationSetItem(long size, long[] entriesOffsets, int[] entriesIndex) {
		this.size = size;
		this.entriesOffsets = entriesOffsets;
		this.entriesIndex = entriesIndex;
	}
	
	public boolean isEqual(AnnotationSetItem other, MapManager mm) {
		if (size != other.size)
			return false;
		
		for (int i = 0; i < size; ++i) {
			if (mm.annotationItemIndexMap[entriesIndex[i]] != other.entriesIndex[i])
				return false;
		}
		return true;
	}
	
	public List<Byte> getModifiedData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
			
		byte[] temp = write32bit(size);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		for (int i = 0; i < size; ++i) {
			temp = write32bit(mm.annotationItemPointerMap[(int)mm.annotationItemIndexMap[entriesIndex[i]]]);
			for (int j = 0; j < 4; ++j)
				l.add(temp[j]);
		}
		
		return l;
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(4 + 4 * size);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(size);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		for (int i = 0; i < size; ++i) {
			temp = write32bit(entriesOffsets[i]);
			for (int j = 0; j < 4; ++j)
				l.add(temp[j]);
		}
		
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
