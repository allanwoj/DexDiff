package item;

public class ClassDataItem {

	public long staticFieldsSize;
	public long instanceFieldsSize;
	public long directMethodsSize;
	public long virtualMethodsSize;
	public EncodedField[] staticFields;
	public EncodedField[] instanceFields;
	public EncodedMethod[] directMethods;
	public EncodedMethod[] virtualMethods;
	
	public ClassDataItem(long staticFieldsSize, long instanceFieldsSize,
			long directMethodsSize, long virtualMethodsSize,
			EncodedField[] staticFields, EncodedField[] instanceFields,
			EncodedMethod[] directMethods, EncodedMethod[] virtualMethods) {
		this.staticFieldsSize = staticFieldsSize;
		this.instanceFieldsSize = instanceFieldsSize;
		this.directMethodsSize = directMethodsSize;
		this.virtualMethodsSize = virtualMethodsSize;
		this.staticFields = staticFields;
		this.instanceFields = instanceFields;
		this.directMethods = directMethods;
		this.virtualMethods = virtualMethods;
	}
	
}
