/**
  * Parses the type id table
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexTypeIdsBlock extends DexParser {

    public void parse() throws IOException {
        int typesSize = (int)dexPointerBlock.getTypeIdsSize();
        file.seek( dexPointerBlock.getTypeIdsOffset() );
        types = new int[ typesSize ];
        for( int i = 0 ; i < typesSize ; ++i ) {
            long typeIdx = read32Bit();
            types[ i ] = (int)typeIdx;
            String typeName = dexStringIdsBlock.getString( types[i] );
            dump( "type["+i+"] index: "+dumpLong( typeIdx )+" ("+typeName+")" );
        }
    }

    public int getTypesSize() {
        return (int)dexPointerBlock.getTypeIdsSize();
    }

    public String getType( int idx ) {
        return dexStringIdsBlock.getString( types[ idx ] );
    }

    public String getClassName( int idx ) {
        String className = dexStringIdsBlock.getString( types[ idx ] );
        className = LTypeToJava( className );
        return className;
    }

    public void setDexPointerBlock( DexPointerBlock dexPointerBlock ) {
        this.dexPointerBlock = dexPointerBlock;
    }

    public void setDexStringIdsBlock( DexStringIdsBlock dexStringIdsBlock ) {
        this.dexStringIdsBlock = dexStringIdsBlock;
    }

    public static String LTypeToJava( String LType ) {
        String className = LType;
        if( className.startsWith( "L" ) )
            className = className.substring( 1 );
        if( className.endsWith( ";" ) )
            className = className.substring( 0, className.length() - 1 );
        return className;
    }


    private int                 types[] = null; 
    private DexPointerBlock     dexPointerBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
}

