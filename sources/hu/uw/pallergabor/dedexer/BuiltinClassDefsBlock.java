/**
  * DexClassDefsBlock faking built-in classes like arrays. This object
  * behaves like a DexClassDefsBlock but never parses anything
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.*;

public class BuiltinClassDefsBlock extends DexClassDefsBlock {
    public static final int CLASSIDX_ARRAY = 0;

/**
  * Silent error even though this method should not be invoked.
  */
    public void parse() {}

/**
  * We don't iterate - built-in classes are recognized by their well-known
  * indexes
  */
    public Iterator<Integer> getClassIterator() {
        return null;
    }

    public boolean isInterface( int classIdx ) {
        switch( classIdx ) {
            case CLASSIDX_ARRAY:
                return false;
        }
        throw new IndexOutOfBoundsException( "no such built-in class: "+classIdx );
    }

    public String getClassName( int classIdx ) {
        switch( classIdx ) {
            case CLASSIDX_ARRAY:
                return "[";
        }
        throw new IndexOutOfBoundsException( "no such built-in class: "+classIdx );
    }

}
