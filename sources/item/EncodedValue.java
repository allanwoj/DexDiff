package item;

public class EncodedValue {

	int name;
	int valueType;
	int valueArg;
	Byte[] value;
	EncodedAnnotation annotation;
	EncodedArray array;
	
	public EncodedValue(int name, int valueType, int valueArg) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
	}
	
	public EncodedValue(int name, int valueType, int valueArg, Byte[] value) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
		this.value = value;
	}
	
	public EncodedValue(int name, int valueType, int valueArg, EncodedAnnotation annotation) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
		this.annotation = annotation;
	}

	public EncodedValue(int name, int valueType, int valueArg, EncodedArray array) {
		this.name = name;
		this.valueType = valueType;
		this.valueArg = valueArg;
		this.array = array;
	}
}
