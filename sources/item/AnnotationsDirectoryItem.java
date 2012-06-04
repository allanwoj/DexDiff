package item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import patch.MapManager;

public class AnnotationsDirectoryItem extends DexItem<AnnotationsDirectoryItem>{

	public long classOffset;
	public int classIndex;
	public long fieldsSize;
	public long annMethodsSize;
	public long annParamsSize;
	
	public FieldAnnotation[] fieldAnnotations;
	public MethodAnnotation[] methodAnnotations;
	public ParameterAnnotation[] parameterAnnotations;

	public AnnotationsDirectoryItem(long classOffset, int classIndex, long fieldsSize,
			long annMethodsSize, long annParamsSize,
			FieldAnnotation[] fieldAnnotations,
			MethodAnnotation[] methodAnnotations,
			ParameterAnnotation[] parameterAnnotations) {
		this.classOffset = classOffset;
		this.classIndex = classIndex;
		this.fieldsSize = fieldsSize;
		this.annMethodsSize = annMethodsSize;
		this.annParamsSize = annParamsSize;
		this.fieldAnnotations = fieldAnnotations;
		this.methodAnnotations = methodAnnotations;
		this.parameterAnnotations = parameterAnnotations;
	}
	
	
	public List<Byte> getModifiedData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp;
		
		if (classIndex != - 1) {
			temp = write32bit(mm.annotationSetItemPointerMap[(int)mm.annotationSetItemIndexMap[classIndex]]);
			for (int i = 0; i < temp.length; ++i)
				l.add(temp[i]);
		} else {
			temp = write32bit(0);
			for (int i = 0; i < temp.length; ++i)
				l.add(temp[i]);
		}
		
		temp = write32bit(fieldsSize);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annMethodsSize);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annParamsSize);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		for (int i = 0; i < fieldsSize; ++i) {
			l.addAll(fieldAnnotations[i].getBytes(mm));
		}
		
		for (int i = 0; i < annMethodsSize; ++i) {
			l.addAll(methodAnnotations[i].getBytes(mm));
		}
		
		for (int i = 0; i < annParamsSize; ++i) {
			l.addAll(parameterAnnotations[i].getBytes(mm));
		}
		
		return l;
	}
	
	public boolean isEqual(AnnotationsDirectoryItem other, MapManager mm) {
		if (fieldsSize != other.fieldsSize ||
				annMethodsSize != other.annMethodsSize || annParamsSize != other.annParamsSize) {
			return false;
		}
		
		if (classIndex != -1 && mm.annotationSetItemIndexMap[classIndex] != other.classIndex)
			return false;
		
		for (int i = 0; i < fieldsSize; ++i) {
			if (!fieldAnnotations[i].isEqual(other.fieldAnnotations[i], mm)) {
				return false;
			}
		}
		
		for (int i = 0; i < annMethodsSize; ++i) {
			if (!methodAnnotations[i].isEqual(other.methodAnnotations[i], mm)) {
				return false;
			}
		}
		
		for (int i = 0; i < annParamsSize; ++i) {
			if (!parameterAnnotations[i].isEqual(other.parameterAnnotations[i], mm)) {
				return false;
			}
		}
		
		return true;
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		byte[] temp = write32bit(classOffset);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		temp = write32bit(fieldsSize);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annMethodsSize);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		temp = write32bit(annParamsSize);
		for (int i = 0; i < temp.length; ++i)
			l.add(temp[i]);
		
		for (int i = 0; i < fieldsSize; ++i) {
			l.addAll(fieldAnnotations[i].getOutput());
		}
		
		for (int i = 0; i < annMethodsSize; ++i) {
			l.addAll(methodAnnotations[i].getOutput());
		}
		
		for (int i = 0; i < annParamsSize; ++i) {
			l.addAll(parameterAnnotations[i].getOutput());
		}
		
		temp = write32bit(l.size());
		for (int i = 3; i >= 0; --i)
			l.add(0, temp[i]);
		
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
