package hu.uw.pallergabor.dedexer;

import java.util.ArrayList;

/**
  * Class holding one annotation
  */
public class Annotation {
// note that these fields may not be set to valid values if the annotation
// is a placeholder null annotation (denotes parameter with no annotation for example,
// in case of a method where not each parameter is annotated)
        public int visibility = -1;
        public String type = null;
        public ArrayList<String> elementNames = new ArrayList<String>();
        public ArrayList elementValues = new ArrayList();
// Used only if the annotation is a parameter annotation. In this case this is
// the index of the parameter (starting with 0) that the annotation is related to
        public int parameterIndex = -1;

        public void addElement( 
                String elementName, Object elementValue ) {
            elementNames.add( elementName );
            elementValues.add( elementValue );
        }
}
