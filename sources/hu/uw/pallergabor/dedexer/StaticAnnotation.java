package hu.uw.pallergabor.dedexer;

/**
  * This wrapper class is used by DexEncodedArrayParser. This object is an annotation
  * which formats itself appropriately when its toString method is called.
  */

public class StaticAnnotation {
		public StaticAnnotation( AnnotationHolder holder ) {
            this.holder = holder;
		}

		public String toString() {
            Annotation a = holder.lastAnnotation();
            StringBuffer sb = new StringBuffer();
            sb.append( ".annotation "+a.type+"\n" );
            for( int i = 0 ; i < a.elementNames.size() ; ++i ) {
                Object o = a.elementValues.get( i );
                sb.append( "  "+
                                a.elementNames.get( i ) +
                                " "+
                                DexEncodedArrayParser.getTypeString( o )+
                                " = "+
                                o.toString()+
                                "\n" );
            }
            sb.append( ".end annotation\n" );
            return new String( sb );
		}

        public boolean equals( Object o ) {
            return holder.equals( o );
        }

        AnnotationHolder holder;
}

