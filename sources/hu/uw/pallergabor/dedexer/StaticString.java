package hu.uw.pallergabor.dedexer;

/**
  * This wrapper class is used by DexEncodedArrayParser. This object is a String
  * which formats itself appropriately when its toString method is called.
  */

public class StaticString {
		public StaticString( String s ) {
			instance = new String( s );
		}

		public String toString() {
			return "\""+instance.toString()+"\"";
		}

        public boolean equals( Object o ) {
            return instance.equals( o );
        }

		private String instance;
}

