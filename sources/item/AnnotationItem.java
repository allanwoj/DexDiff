package item;


public class AnnotationItem {

	int visibility;
	private EncodedAnnotation annotation;
	
	public AnnotationItem(int visibility, EncodedAnnotation annotation) {
		this.visibility = visibility;
		this.annotation = annotation;
	}
	
	public EncodedAnnotation getAnnotation() {
		return annotation;
	}
	
}
