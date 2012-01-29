/**
  * Parses the dependency section 
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.ArrayList;

public class DexDependencyParser extends DexParser {

    public int getDependencySize() {
        return depList.size();
    }

    public String getDependencyElement( int idx ) {
        return depList.get( idx );
    }

    public void parse() throws IOException {
        setFilePosition( dexSignatureBlock.
                            getDexOptimizationData().
                            getDepsOffset() );
// read the dependency header
        long modificationTimeStamp = read32Bit();
        dump( "Modification timestamp: 0x"+Long.toHexString( modificationTimeStamp ) );
        long crc = read32Bit();
        dump( "CRC: 0x"+Long.toHexString( crc ) );
        long dalvikVersion = read32Bit();
        dump( "Dalvik version: 0x"+Long.toHexString( dalvikVersion ) );
        long numDeps = read32Bit();
        dump( "Number of dependencies: "+numDeps );
        for( long l = 0 ; l < numDeps ; ++l ) {
            long depLen = read32Bit();
            long pos = getFilePosition();
            dump( "Length of dependency#"+l+": "+depLen );
            StringBuilder sb = new StringBuilder();
            int c = 0;
            do {
                c = file.read();
                if( c > 0 )
                    sb.append( (char)c );
            } while( c > 0 );
            String dep = new String( sb );
            depList.add( dep );
            dump( "Dependency#"+l+" : "+dep );
// 20 is the length of SHA digest after the name
            setFilePosition( pos+depLen+20 );
        }
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }

    DexSignatureBlock   dexSignatureBlock;
    ArrayList<String> depList = new ArrayList<String>();
}
