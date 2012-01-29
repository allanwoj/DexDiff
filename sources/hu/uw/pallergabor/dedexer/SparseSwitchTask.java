/**
  * Stores a sparse-switch task.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class SparseSwitchTask extends DedexerTask {

    public SparseSwitchTask( DexInstructionParser instrParser, 
                        long base, 
                        long offset ) {
        super( instrParser, base, offset );
    }

    public boolean equals( DedexerTask o ) {
        if( !(o instanceof SparseSwitchTask ) )
            return false;
        return super.equals( o );
    }

    public void doTask( boolean isSecondPass ) throws IOException {
        if( !isSecondPass ) {
            if( jumpTable == null )
                readJumpTable();
            String sparseLabelPrefix = "sp"+
                                    Long.toHexString( base )+
                                    "_";
            instrParser.placeTask( base, this );
            defaultLabelName = sparseLabelPrefix + "default";
            instrParser.placeLabel( base+6L, defaultLabelName );
            switchValues = new String[ jumpTable.length ];
            for( int i = 0 ; i < jumpTable.length ; ++i ) {
                long target = jumpTable[i];
                String labelName = sparseLabelPrefix +
                                Long.toHexString( target );
                instrParser.placeLabel( target,labelName );
                switchValues[i] = labelName;
            }
            instrParser.placeTask( offset, this );
        }
    }

    public void renderTask( long position ) throws IOException {
        if( position == base )
            instrParser.
                getCodeGenerator().
                renderSparseSwitch( reg,
                                defaultLabelName,
                                switchKeys,
                                switchValues );
        else
        if( position == offset ) {
            long endTablePosition = instrParser.getFilePosition() + tableLength;
            instrParser.setFilePosition( endTablePosition );
        }
    }

    public void setReg( int reg ) {
        this.reg = reg;
    }

    public long[] readJumpTable() throws IOException {
        long origPos = instrParser.getFilePosition();
        instrParser.setFilePosition( offset );
        int tableType = instrParser.read16Bit();    
        if( tableType != 0x200 )    // type flag for sparse switch tables
                throw new IOException( "Invalid sparse-switch table type (0x"+
                            Integer.toHexString( tableType )+
                            ") at offset 0x"+
                            Long.toHexString( instrParser.getFilePosition()-2 ) );
        int tableElements = instrParser.read16Bit();
        switchKeys = new String[ tableElements ];
        for( int i = 0 ; i < tableElements ; ++i )
                switchKeys[i] = Integer.toString( instrParser.readSigned32Bit() );
        jumpTable = new long[ tableElements ];
        for( int i = 0 ; i < tableElements ; ++i ) {
            long targetOffset = instrParser.readSigned32Bit();
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
    private long jumpTable[] = null;
    private String switchKeys[];
    private String switchValues[];
    private int reg = 0;
    private String defaultLabelName;
}

