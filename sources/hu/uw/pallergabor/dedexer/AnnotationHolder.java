package hu.uw.pallergabor.dedexer;

import java.util.ArrayList;

/**
  * Class holding a list of annotations with name/element pairs
  */

public class AnnotationHolder {

        public String asset = null;    // the element the annotation is associated with (e.g. method)
        public long offset = -1L;     // temporary variable, used before the annotation is entirely read
        public ArrayList<Annotation>    annotations = new ArrayList<Annotation>();

        public void setVisibility( int visibility ) {
            int lastAnnotationIdx = annotations.size() - 1;
            Annotation lastAnnotation = annotations.get( lastAnnotationIdx );
            lastAnnotation.visibility = visibility;
        }

        public void setType( String type ) {
            int lastAnnotationIdx = annotations.size() - 1;
            Annotation lastAnnotation = annotations.get( lastAnnotationIdx );
            lastAnnotation.type = type;
        }

        public void addElement( 
                String elementName, Object elementValue ) {
            int lastAnnotationIdx = annotations.size() - 1;
            Annotation lastAnnotation = annotations.get( lastAnnotationIdx );
            lastAnnotation.addElement( elementName, elementValue );
        }

        public void newAnnotation() {
            Annotation annotation = new Annotation();
            annotations.add( annotation );
        }

        public Annotation lastAnnotation() {
            int idx = annotations.size();
            if( idx == 0 )
                return null;
            return annotations.get( idx-1 );
        }
}
