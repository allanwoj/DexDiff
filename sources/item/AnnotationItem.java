package item;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import patch.MapManager;


public class AnnotationItem extends DexItem<AnnotationItem> {

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
	
	public boolean isEqual(AnnotationItem other, MapManager mm) {
		return (visibility == other.visibility && annotation.isEqual(other.annotation, mm));
	}
	
	public List<Byte> getModifiedData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.add((byte)visibility);
		l.addAll(annotation.getData(mm));
				
		return l;
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.add((byte)visibility);
		l.addAll(annotation.getOutput());
		
		
		byte[] temp = write32bit(l.size());
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
