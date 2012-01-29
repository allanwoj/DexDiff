/**
  * Parses a DEX method head block
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexMethodHeadParser extends DexParser {
    public void parse() throws IOException {
        setDexOptimizationData( dexSignatureBlock.getDexOptimizationData() );
        registersSize = read16Bit();
        dump( "registers size: "+registersSize );
        inputParameters = read16Bit();
        dump( "input arguments: "+inputParameters );
        outputParameters = read16Bit();
        dump( "output arguments: "+outputParameters );
        triesSize = read16Bit();
        dump( "try block size: "+triesSize );
        debugOffset = readFileOffset();
        dump( "debug info offset: 0x"+Long.toHexString( debugOffset ) );
        instructionsSize = read32Bit();
        dump( "instruction block size: "+instructionsSize );
        instructionBase = file.getFilePointer();
        dump( "method block: 0x"+Long.toHexString( instructionBase )+
                "-0x"+
                Long.toHexString( getInstructionEnd() ) );
        dump( "next block starts at: 0x"+
                Long.toHexString( getNextBlockOffset() ) );
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }

    public int getRegistersSize() {
        return registersSize;
    }

    public int getInputParameters() {
        return inputParameters;
    }

    public int getOutputParameters() {
        return outputParameters;
    }

    public int getTriesSize() {
        return triesSize;
    }

    public long getDebugOffset() {
        return debugOffset;
    }

    public long getInstructionBase() {
        return instructionBase;
    }

    public long getInstructionEnd() {
        return instructionBase + 2*instructionsSize;
    }

    public long getNextBlockOffset() {
        long off = getInstructionEnd();
        if( ( off % 4 ) > 0 )
            off = ( ( off / 4 ) + 1 ) * 4;
        return off;
    }

    private int registersSize;
    private int inputParameters;
    private int outputParameters;
    private int triesSize;
    private long debugOffset;
    private long instructionsSize;
    private long instructionBase;
    private DexSignatureBlock dexSignatureBlock;
}
