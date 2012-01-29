package hu.uw.pallergabor.dedexer;

/**
  * This wrapper class is used by DexEncodedArrayParser. This object is a Character
  * which formats itself appropriately when its toString method is called.
  */
public class StaticArray {
		public StaticArray( int size ) {
			instance = new Object[ size ];
		}

        public void set( int idx, Object o ) {
            instance[idx] = o;
        }

        public int length() {
            return instance.length;
        }

        public Object get( int idx ) {
            return instance[idx];
        }

		public String toString() {
            StringBuilder b = new StringBuilder();
            b.append( "{ " );
            for( int i = 0 ; i < instance.length ; ++i ) {
                if( i > 0 )
                    b.append( " , " );
                b.append( instance[i].toString() );
            }
            b.append( " }" );
			return b.toString();
		}

		private Object instance[];
}
