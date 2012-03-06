package item;

public class EncodedAnnotation {
	int type;
	int size;
	public EncodedValue[] values;
	
	public EncodedAnnotation(int type, int size) {
		this.type = type;
		this.size = size;
		values = new EncodedValue[size];
	}
}
