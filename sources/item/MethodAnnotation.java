package item;

import java.util.ArrayList;
import java.util.Collection;

import patch.MapManager;

public class MethodAnnotation {

	public long methodId;
	public int annotationSetItemIndex;
	public long annotationSetItemOffset;
	
	public MethodAnnotation(long methodId, int annotationSetItemIndex, long annotationSetItemOffset) {
		this.methodId = methodId;
		this.annotationSetItemIndex = annotationSetItemIndex;
		this.annotationSetItemOffset = annotationSetItemOffset;
	}
	
	public Collection<Byte> getBytes(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(mm.methodIndexMap[(int)methodId]);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		temp = write32bit(mm.annotationSetItemPointerMap[(int)mm.annotationSetItemIndexMap[(int)annotationSetItemIndex]]);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		return l;
	}
	
	public boolean isEqual(MethodAnnotation other, MapManager mm) {
		return (mm.methodIndexMap[(int)methodId] == other.methodId &&
				mm.annotationSetItemIndexMap[annotationSetItemIndex] == other.annotationSetItemIndex);
	}
	
	public Collection<Byte> getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();

		byte[] temp = write32bit(methodId);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annotationSetItemOffset);
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
