package hu.uw.pallergabor.dedexer;

/**
  * This wrapper class is used by DexEncodedArrayParser. This object is a Character
  * which formats itself appropriately when its toString method is called.
  */
public class StaticCharacter {
		public StaticCharacter( char c ) {
			instance = new Character( c );
		}

		public String toString() {
			return "'"+instance.toString()+"'";
		}

		private Character instance;
}
