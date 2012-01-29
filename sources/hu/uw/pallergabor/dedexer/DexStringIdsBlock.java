/**
  * Parses the string table
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexStringIdsBlock extends DexParser {

    public void parse() throws IOException {
        setDexOptimizationData( dexSignatureBlock.getDexOptimizationData() );
        int stringsSize = (int)dexPointerBlock.getStringIdsSize();
        file.seek( dexPointerBlock.getStringIdsOffset() );
        long stringsPos[] = new long[ stringsSize ];
// Read the string offsets first
        for( int i = 0 ; i < stringsSize ; ++i ) {
            stringsPos[i] = readFileOffset();
            dump( "string["+i+"]: at 0x"+dumpLong( stringsPos[i] ) );
        }
// Then use these pointers to read the strings themselves
        setDumpOff();
        strings = new String[ stringsSize ];
        for( int i = 0 ; i < strings.length ; ++i ) {
            file.seek( stringsPos[i] );
            strings[i] = readString();
            dump( "// string["+i+"]: "+strings[i] );
        }
        setDumpOn();
    }

    public int getStringsSize() {
        return (int)dexPointerBlock.getStringIdsSize();
    }

    public String getString( int idx ) {
        return strings[ idx ];
    }

    public void setDexPointerBlock( DexPointerBlock dexPointerBlock ) {
        this.dexPointerBlock = dexPointerBlock;
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }

    private String              strings[] = null; 
    private DexPointerBlock     dexPointerBlock = null;
    private DexSignatureBlock   dexSignatureBlock;

}

