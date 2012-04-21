package item;

import java.util.ArrayList;
import java.util.Iterator;

public class ClassDefItem {
	public long classId;
	public long accessFlags;
	public long superclassId;
	public int interfacesIndex;
	public long interfacesOffset;
	public long sourceFileId;
	public int annotationsIndex;
	public long annotationsOffset;
	public int classDataIndex;
	public long classDataOffset;
	public int staticValuesIndex;
	public long staticValuesOffset;
	
	public ClassDefItem(long classId, long accessFlags, long superclassId,
			int interfacesIndex, long interfacesOffset, long sourceFileId,
			int annotationsIndex, long annotationsOffset, int classDataIndex,
			long classDataOffset, int staticValuesIndex, long staticValuesOffset) {
		this.classId = classId;
		this.accessFlags = accessFlags;
		this.superclassId = superclassId;
		this.interfacesIndex = interfacesIndex;
		this.interfacesOffset = interfacesOffset;
		this.sourceFileId = sourceFileId;
		this.annotationsIndex = annotationsIndex;
		this.annotationsOffset = annotationsOffset;
		this.classDataIndex = classDataIndex;
		this.classDataOffset = classDataOffset;
		this.staticValuesIndex = staticValuesIndex;
		this.staticValuesOffset = staticValuesOffset;
	}
	
	/*public byte[] getData(long[] annotationsDirectoryItemIndexMap, long[] annotationsDirectoryItemPointerMap, long[] classDataItemIndexMap, long[] classDataItemPointerMap,
			long[] encodedArrayItemIndexMap, long[] encodedArrayItemPointerMap,long[] stringIndexMap, long[] typeIndexMap, long[] typeListIndexMap, long[] typeListPointerMap) {
		if (typeIndexMap[classId] != other.classId || accessFlags != other.accessFlags) {
			return false;
		}
		
		if (superclassId != -1) {
			if (typeIndexMap[superclassId] != superclassId) {
				return false;
			}
		}
		
		if (interfacesOffset != 0) {
			if (typeListIndexMap[interfacesIndex] != other.interfacesIndex) {
				return false;
			}
		}
		
		if (sourceFileId != -1) {
			if (stringIndexMap[sourceFileId] != other.sourceFileId) {
				return false;
			}
		}
		
		if (annotationsOffset != 0) {
			if (annotationsDirectoryItemIndexMap[annotationsIndex] != other.annotationsIndex) {
				return false;
			}
		}
		
		if (classDataOffset != 0) {
			if (classDataItemIndexMap[classDataIndex] != other.classDataIndex) {
				return false;
			}
		}
		
		if (staticValuesOffset != 0) {
			if (encodedArrayItemIndexMap[staticValuesIndex] != other.staticValuesIndex) {
				return false;
			}
		}
		
		return true;
	}*/
	
	public byte[] getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(classId);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(classId);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(accessFlags);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(superclassId);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(interfacesOffset);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(sourceFileId);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annotationsOffset);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(classDataOffset);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(staticValuesOffset);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		byte[] ret = new byte[4 + l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		temp = write32bit(l.size());
		for (int i = 0; i < 4; ++i)
			ret[count++] = temp[i];
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public boolean isEqual(ClassDefItem other, long[] annotationsDirectoryItemIndexMap, long[] classDataItemIndexMap, long[] encodedArrayItemIndexMap, long[] stringIndexMap, long[] typeIndexMap, long[] typeListIndexMap) {
		if (typeIndexMap[(int) classId] != other.classId || accessFlags != other.accessFlags) {
			return false;
		}
		
		if (superclassId != -1) {
			if (typeIndexMap[(int) superclassId] != superclassId) {
				return false;
			}
		}
		
		if (interfacesOffset != 0) {
			if (typeListIndexMap[interfacesIndex] != other.interfacesIndex) {
				return false;
			}
		}
		
		if (sourceFileId != -1) {
			if (stringIndexMap[(int) sourceFileId] != other.sourceFileId) {
				return false;
			}
		}
		
		if (annotationsOffset != 0) {
			if (annotationsDirectoryItemIndexMap[annotationsIndex] != other.annotationsIndex) {
				return false;
			}
		}
		
		if (classDataOffset != 0) {
			if (classDataItemIndexMap[classDataIndex] != other.classDataIndex) {
				return false;
			}
		}
		
		if (staticValuesOffset != 0) {
			if (encodedArrayItemIndexMap[staticValuesIndex] != other.staticValuesIndex) {
				return false;
			}
		}
		
		return true;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
}
