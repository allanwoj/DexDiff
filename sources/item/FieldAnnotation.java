package item;

import java.util.ArrayList;
import java.util.Collection;

public class FieldAnnotation {

	public long fieldId;
	public int annotationSetItemIndex;
	public long annotationSetItemOffset;
	public FieldAnnotation(long fieldId, int annotationSetItemIndex, long annotationSetItemOffset) {
		this.fieldId = fieldId;
		this.annotationSetItemIndex = annotationSetItemIndex;
		this.annotationSetItemOffset = annotationSetItemOffset;
	}
	
	public Collection<Byte> getBytes(long[] fieldIndexMap, long[] annotationSetItemIndexMap, long[] annotationSetItemPointerMap) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(fieldIndexMap[(int)fieldId]);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		temp = write32bit(annotationSetItemPointerMap[(int)annotationSetItemIndexMap[(int)annotationSetItemIndex]]);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		return l;
	}
	
	public boolean isEqual(FieldAnnotation other, long[] fieldIndexMap, long[] annotationSetItemIndexMap) {
		return (fieldIndexMap[(int)fieldId] == other.fieldId &&
			annotationSetItemIndexMap[annotationSetItemIndex] == other.annotationSetItemIndex);
	}
	
	public Collection<Byte> getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		
		byte[] temp = write32bit(fieldId);
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
