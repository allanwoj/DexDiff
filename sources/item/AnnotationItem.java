package item;

import java.util.ArrayList;
import java.util.Iterator;


public class AnnotationItem {

	private int visibility;
	private EncodedAnnotation annotation;
	
	public AnnotationItem(int visibility, EncodedAnnotation annotation) {
		this.visibility = visibility;
		this.annotation = annotation;
	}
	
	public EncodedAnnotation getAnnotation() {
		return annotation;
	}
	
	public int getVisibility() {
		return visibility;
	}
	
	public boolean isEqual(AnnotationItem other, long[] fieldMap, long[] methodMap, long[] stringMap, long[] typeMap) {
		return (visibility == other.visibility && annotation.isEqual(other.annotation, fieldMap, methodMap, stringMap, typeMap));
	}
	
	public byte[] getBytes(long[] fieldMap, long[] methodMap, long[] stringMap, long[] typeMap) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.add((byte)visibility);
		l.addAll(annotation.getData(fieldMap, methodMap, stringMap, typeMap));
		
		
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
		l.add((byte)visibility);
		l.addAll(annotation.getOutput());
		
		
		byte[] ret = new byte[4 + l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;

		byte[] temp = write32bit(l.size());
		for (int i = 0; i < 4; ++i)
			ret[count++] = temp[i];
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
}
