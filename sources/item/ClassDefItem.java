package item;

import java.util.ArrayList;
import java.util.Iterator;

import patch.MapManager;

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
	
	public byte[] getData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(mm.typeIndexMap[(int)classId]);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write32bit(accessFlags);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		if (superclassId != -1) {
			temp = write32bit(mm.typeIndexMap[(int)superclassId]);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		}
		
		if (interfacesOffset != 0) {
			temp = write32bit(mm.typeListPointerMap[(int)mm.typeListIndexMap[(int)interfacesIndex]]);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		}
		
		if (sourceFileId != -1) {
			temp = write32bit(mm.stringIndexMap[(int)sourceFileId]);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		}
		
		if (annotationsOffset != 0) {
			temp = write32bit(mm.annotationsDirectoryItemPointerMap[(int)mm.annotationsDirectoryItemIndexMap[annotationsIndex]]);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		}
		
		if (classDataOffset != 0) {
			temp = write32bit(mm.classDataItemPointerMap[(int)mm.classDataItemIndexMap[classDataIndex]]);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		}
		
		if (staticValuesOffset != 0) {
			temp = write32bit(mm.encodedArrayItemPointerMap[(int)mm.encodedArrayItemIndexMap[staticValuesIndex]]);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < 4; ++i)
				l.add(temp[i]);
		}
		
		byte[] ret = new byte[l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public byte[] getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(classId);
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
	
	public boolean isEqual(ClassDefItem other, MapManager mm) {
		if (mm.typeIndexMap[(int) classId] != other.classId || accessFlags != other.accessFlags) {
			return false;
		}
		
		if (superclassId != -1) {
			if (mm.typeIndexMap[(int) superclassId] != other.superclassId) {
				return false;
			}
		}
		
		if (interfacesOffset != 0) {
			if (mm.typeListIndexMap[interfacesIndex] != other.interfacesIndex) {
				return false;
			}
		}
		
		if (sourceFileId != -1) {
			if (mm.stringIndexMap[(int) sourceFileId] != other.sourceFileId) {
				return false;
			}
		}
		
		if (annotationsOffset != 0) {
			if (mm.annotationsDirectoryItemIndexMap[annotationsIndex] != other.annotationsIndex) {
				return false;
			}
		}
		
		if (classDataOffset != 0) {
			if (mm.classDataItemIndexMap[classDataIndex] != other.classDataIndex) {
				return false;
			}
		}
		
		if (staticValuesOffset != 0) {
			if (mm.encodedArrayItemIndexMap[staticValuesIndex] != other.staticValuesIndex) {
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
