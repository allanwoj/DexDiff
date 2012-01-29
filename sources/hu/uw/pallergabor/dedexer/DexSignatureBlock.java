/**
  * Parses the DEX signature block.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexSignatureBlock extends DexParser {
    public enum OptVersion {
	OPTVERSION_35,
	OPTVERSION_36
    }

    public void parse() throws IOException {
// Parse the magic numbers
        parseExpected8Bit( 0x64 );
        parseExpected8Bit( 0x65 );
        int allowedBytes[] = { 0x78,0x79 };
        int idx = parseExpected8BitValues( allowedBytes  );
        boolean isOptimized = idx == 1;
        parseExpected8Bit( 0x0A );
        parseExpected8Bit( 0x30 );
// Some DEX files have 0x31 here instead of the standard 0x33
        int allowedBytes2[] = { 0x33, 0x31 };
        idx = parseExpected8BitValues( allowedBytes2 );
// Some DEX files have 0x33 here instead of the standard 0x35
        int allowedBytes3[] = { 0x36, 0x35, 0x33 };
        int optVersionIdx = parseExpected8BitValues( allowedBytes3 );
        parseExpected8Bit( 0x00 );
        if( isOptimized ) {
 	    if( optVersionIdx == 0 )
            	dump( "magic: dey\\n036\\0" );
	    else
            	dump( "magic: dey\\n035\\0" );
        } else
            dump( "magic: dex\\n035\\0" );
        if( !isOptimized ) {
            checksum = read32Bit();
            dump( "checksum" );
            for( int i = 0 ; i < 20 ; ++i )
                signature[i] = read8Bit();
            dump( "signature" );
        } else {
// Read pointers from the ODEX header
            long dexOffset = read32Bit();
            dump( "DEX start offset: 0x"+Long.toHexString( dexOffset ) );
            long dexLength = read32Bit();
            dump( "DEX length: 0x"+Long.toHexString( dexLength ) );
            long depsOffset = read32Bit();
            dump( "Dependencies start offset: 0x"+Long.toHexString( depsOffset ) );
            long depsLength = read32Bit();
            dump( "Dependencies length: 0x"+Long.toHexString( depsLength ) );
            long auxOffset = read32Bit();
            dump( "Auxiliary data offset: 0x"+Long.toHexString( auxOffset ) );
            long auxLength = read32Bit();
            dump( "Auxiliary data length: 0x"+Long.toHexString( auxLength ) );
            DexOptimizationData dexOptimizationData =
                    new DexOptimizationData();
            dexOptimizationData.setDexOffset( dexOffset );
            dexOptimizationData.setDexLength( dexLength );
            dexOptimizationData.setDepsOffset( depsOffset );
            dexOptimizationData.setDepsLength( depsLength );
            dexOptimizationData.setAuxOffset( auxOffset );
            dexOptimizationData.setAuxLength( auxLength );
            optVersion =
		optVersionIdx == 0 ?
		OptVersion.OPTVERSION_36 :
		OptVersion.OPTVERSION_35;
            setDexOptimizationData( dexOptimizationData );
        }
    }

    public long getChecksum() {
        return checksum;
    }

    public int[] getSignature() {
        return signature;
    }

    public OptVersion getOptVersion() {
	return optVersion;
    }

    public void setOptVersion( OptVersion optVersion ) {
	this.optVersion = optVersion;
    }

    private OptVersion optVersion;
    private long checksum;
    private int signature[] = new int[20];
    
}
