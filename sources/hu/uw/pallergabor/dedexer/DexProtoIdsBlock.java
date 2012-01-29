/**
  * Parses the proto id table
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexProtoIdsBlock extends DexParser {

    public void parse() throws IOException {
        setDexOptimizationData( dexSignatureBlock.getDexOptimizationData() );
        int protoSize = (int)dexPointerBlock.getProtoIdsSize();
        file.seek( dexPointerBlock.getProtoIdsOffset() );
        protos = new ProtoHolder[ protoSize ];
        long parmOffsets[] = new long[ protoSize ];
// Read the proto table
        for( int i = 0 ; i < protoSize ; ++i ) {
            protos[i] = new ProtoHolder();
            long shortyIdx = read32Bit();
            protos[i].returnType = (int)read32Bit();
            parmOffsets[i] = readFileOffset();
            dump( "proto["+i+"]: short signature: "+
                    dexStringIdsBlock.getString( (int)shortyIdx )+
                    "; return type: "+
                    dexTypeIdsBlock.getType( protos[i].returnType )+
                    "; parameter block offset: "+
                    dumpLong( parmOffsets[i] ) );
        }
// Resolve the parameter blocks
        setDumpOff();
        for( int i = 0 ; i < protoSize ; ++i ) {
// if there is parameter block at all
            if( parmOffsets[i] > 0L ) {
                file.seek( parmOffsets[i] );
                int parmNo = (int)read32Bit();
                ProtoHolder protoHolder = protos[i];
                protoHolder.parameterTypes = new int[ parmNo ];
                for( int n = 0 ; n < parmNo ; ++n ) {
                    int parameterTypeIdx = read16Bit();
                    protoHolder.parameterTypes[ n ] = parameterTypeIdx;
                }
            }
            dump( "// proto["+i+"]: "+
                    getReturnValueType( i )+
                    " proto( "+
                    getParameterValueTypes( i )+
                    " )" );
        }
        setDumpOn();
    }

    public int getProtosSize() {
        return (int)dexPointerBlock.getProtoIdsSize();
    }

    public String getReturnValueType( int idx ) {
        ProtoHolder protoHolder = protos[ idx ];
        return dexTypeIdsBlock.getType( protoHolder.returnType );
    }

    public String getParameterValueTypes( int idx ) {
        ProtoHolder protoHolder = protos[ idx ];
        if( protoHolder.parameterTypes == null )
            return "";
        StringBuilder b = new StringBuilder();
        for( int i = 0 ; i < protoHolder.parameterTypes.length ; ++i )
            b.append( dexTypeIdsBlock.getType( 
                            protoHolder.parameterTypes[i] ) );
        return new String( b );
    }

    public void setDexPointerBlock( DexPointerBlock dexPointerBlock ) {
        this.dexPointerBlock = dexPointerBlock;
    }

    public void setDexStringIdsBlock( DexStringIdsBlock dexStringIdsBlock ) {
        this.dexStringIdsBlock = dexStringIdsBlock;
    }

    public void setDexTypeIdsBlock( DexTypeIdsBlock dexTypeIdsBlock ) {
        this.dexTypeIdsBlock = dexTypeIdsBlock;
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }

    private ProtoHolder         protos[];
    private DexPointerBlock     dexPointerBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexTypeIdsBlock     dexTypeIdsBlock = null;
    private DexSignatureBlock   dexSignatureBlock = null;

    class ProtoHolder {
        public int returnType = -1;
        public int parameterTypes[] = null;
    }
}
