package item;


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
	
}
