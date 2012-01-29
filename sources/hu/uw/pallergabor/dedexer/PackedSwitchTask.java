/**
  * Stores and processes a packed-switch task. During the first pass, the location of
  * the switch instruction and its jump table is stored. Between the first and
  * second passes, the jump table is processed and labels are placed. During the second
  * pass, the label block is inserted after the packed-switch instruction.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.ArrayList;

public class PackedSwitchTask extends DedexerTask {

    public PackedSwitchTask( DexInstructionParser instrParser, 
                        long base, 
                        long offset ) {
        super( instrParser, base, offset );
    }

    public boolean equals( DedexerTask o ) {
        if( !(o instanceof PackedSwitchTask ) )
            return false;
        return super.equals( o );
    }

    public void doTask( boolean isSecondPass ) throws IOException {
        if( !isSecondPass ) {
// Read the jump table
            if( jumpTable == null )
                readJumpTable();
            String switchLabelPrefix = "ps"+
                                    Long.toHexString( base )+
                                    "_";
            instrParser.placeTask( base, this );
            defaultLabelName = switchLabelPrefix + "default";
            instrParser.placeLabel( base+6L, defaultLabelName );
            for( int i = 0 ; i < jumpTable.length ; ++i ) {
                long target = jumpTable[i];
                String labelName = switchLabelPrefix +
                                Long.toHexString( target );
                instrParser.placeLabel( target,labelName );
                labels.add( labelName );
            }
            instrParser.placeTask( offset, this );
        }
    }

    public void renderTask( long position ) throws IOException {
        if( position == base )
            instrParser.
                getCodeGenerator().
                renderPackedSwitch( reg,
                                low,
                                defaultLabelName,
                                labels );
        else
        if( position == offset ) {
            long endTablePosition = instrParser.getFilePosition() + tableLength;
            instrParser.setFilePosition( endTablePosition );
        }
    }

    public void setReg( int reg ) {
        this.reg = reg;
    }

// Reads the jump table and returns the offsets (compared to the jump instruction base)
// as array of longs
    public long[] readJumpTable() throws IOException {
        long origPos = instrParser.getFilePosition();
        instrParser.setFilePosition( offset );
        int tableType = instrParser.read16Bit();
        if( tableType != 0x100 )    // type flag for packed switch tables
            throw new IOException( "Invalid packed-switch table type (0x"+
                            Integer.toHexString( tableType )+
                            ") at offset 0x"+
                            Long.toHexString( instrParser.getFilePosition()-2 ) );
        int tableElements = instrParser.read16Bit();
        low = instrParser.readSigned32Bit();
        jumpTable = new long[ tableElements ];
        for( int i = 0 ; i < tableElements ; ++i ) {
            int targetOffset = instrParser.readSigned32Bit();
            jumpTable[i] = base + ( targetOffset * 2 );
        }
        tableLength = instrParser.getFilePosition() - origPos;
        instrParser.setFilePosition( origPos );
        return jumpTable;
    }

    public boolean getParseFlag( long position ) {
        return position == offset;
    }

    private long tableLength = 0L;
    private int reg = 0;
    private int low = 0;
    private long jumpTable[] = null;
    String defaultLabelName = null;
    ArrayList<String> labels = new ArrayList<String>();
}

