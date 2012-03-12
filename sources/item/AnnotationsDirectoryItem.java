package item;

public class AnnotationsDirectoryItem {

	public long classOffset;
	public long fieldsSize;
	public long annMethodsSize;
	public long annParamsSize;
	
	public FieldAnnotation[] fieldAnnotations;
	public MethodAnnotation[] methodAnnotations;
	public ParameterAnnotation[] parameterAnnotations;

	public AnnotationsDirectoryItem(long classOffset, long fieldsSize,
			long annMethodsSize, long annParamsSize,
			FieldAnnotation[] fieldAnnotations,
			MethodAnnotation[] methodAnnotations,
			ParameterAnnotation[] parameterAnnotations) {
		this.classOffset = classOffset;
		this.fieldsSize = fieldsSize;
		this.annMethodsSize = annMethodsSize;
		this.annParamsSize = annParamsSize;
		this.fieldAnnotations = fieldAnnotations;
		this.methodAnnotations = methodAnnotations;
		this.parameterAnnotations = parameterAnnotations;
	}
	
	
}
