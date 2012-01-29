/**
  * Stores a fill-array task.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class FillArrayTask extends DedexerTask {

    public FillArrayTask( DexInstructionParser instrParser, 
                        long base, 
                        long offset ) {
        super( instrParser, base, offset );
    }

    public boolean equals( DedexerTask o ) {
        if( !(o instanceof FillArrayTask ) )
            return false;
        return super.equals( o );
    }

    public void setType( String typeString ) {
        this.typeString = typeString;
    }

    public void doTask( boolean isSecondPass ) throws IOException {
        instrParser.placeTask( offset, this );
    }

    public void renderTask( long position ) throws IOException {
            int tableType = instrParser.read16Bit();    
            if( tableType != 0x300 )    // type flag for array-data
                throw new IOException( "Invalid array-data table type (0x"+
                            Integer.toHexString( tableType )+
                            ") at offset 0x"+
                            Long.toHexString( instrParser.getFilePosition()-2 ) );
            int bytesPerElement = instrParser.read16Bit();
            long numberOfElements = instrParser.read32Bit();
            CodeGenerator cg = instrParser.getCodeGenerator();
            String arrayDataLabel = instrParser.labelForAddress( offset );
            cg.openDataArray( arrayDataLabel );
            for( long l = 0 ; l < numberOfElements ; ++l ) {
                StringBuilder element = new StringBuilder();
                long elementValue = 0L;
                int byteOffset = 0;
                for( int i = 0 ; i < bytesPerElement ; ++i ) {
                    if( i > 0 )
                        element.append( ", " );
                    int b = instrParser.read8Bit();
                    element.append( "0x" );
                    element.append( instrParser.dumpByte( b ) );
                }
                cg.writeElement( l,new String( element ) );
            }
            cg.closeDataArray( arrayDataLabel );
    }

/**
  * renderTask parses code
  */
    public boolean getParseFlag( long position ) {
        return true;
    }

    private String typeString;
}

