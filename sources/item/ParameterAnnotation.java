package item;

import java.util.ArrayList;
import java.util.Collection;

public class ParameterAnnotation {

	public long methodId;
	public int annotationSetRefListIndex;
	public long annotationSetRefListOffset;

	public ParameterAnnotation(long methodId, int annotationSetRefListIndex, long annotationSetRefListOffset) {
		this.methodId = methodId;
		this.annotationSetRefListIndex = annotationSetRefListIndex;
		this.annotationSetRefListOffset = annotationSetRefListOffset;
	}
	
	public Collection<Byte> getBytes(long[] methodIndexMap, long[] annotationSetRefListIndexMap, long[] annotationSetRefListPointerMap) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(methodIndexMap[(int)methodId]);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		temp = write32bit(annotationSetRefListPointerMap[(int)annotationSetRefListIndexMap[(int)annotationSetRefListIndex]]);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		return l;
	}
	
	public boolean isEqual(ParameterAnnotation other, long[] methodIndexMap, long[] annotationSetRefListIndexMap) {
		return (methodIndexMap[(int)methodId] == other.methodId &&
			annotationSetRefListIndexMap[annotationSetRefListIndex] == other.annotationSetRefListIndex);
	}
	
	public Collection<Byte> getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		
		byte[] temp = write32bit(methodId);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annotationSetRefListOffset);
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
