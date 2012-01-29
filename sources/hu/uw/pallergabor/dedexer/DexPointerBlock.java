/**
  * Parser for the pointer block (normally starting at 0x20)
  */
package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexPointerBlock extends DexParser {

    public void parse() throws IOException {
// The exact structure of ODEX signature block is unknown, the signature parser
// does not analyse it. We have to skip it.
        DexOptimizationData dexOptimizationData = dexSignatureBlock.getDexOptimizationData();
        setDexOptimizationData( dexOptimizationData );
        if( ( dexOptimizationData != null ) && dexOptimizationData.isOptimized() )
                setFilePosition( 0x48L ); 
        fileSize = read32Bit();
        dump( "file size: 0x"+dumpLong( fileSize ) );
        headerSize = read32Bit();
        dump( "header size: 0x"+dumpLong( headerSize ) );
        parseExpected32Bit( 0x12345678L );
        linkSize = read32Bit();
        dump( "link size: 0x"+dumpLong( linkSize ) );
        linkOffset = readFileOffset();
        dump( "link offset: 0x"+dumpLong( linkOffset ) );
        mapOffset = readFileOffset();
        dump( "map offset: 0x"+dumpLong( mapOffset ) );
        stringIdsSize = read32Bit();
        dump( "string ids size: 0x"+dumpLong( stringIdsSize ) );
        stringIdsOffset = readFileOffset();
        dump( "string ids offset: 0x"+dumpLong( stringIdsOffset ) );
        typeIdsSize = read32Bit();
        dump( "type ids size: 0x"+dumpLong( typeIdsSize ) );
        typeIdsOffset = readFileOffset();
        dump( "type ids offset: 0x"+dumpLong( typeIdsOffset ) );
        protoIdsSize = read32Bit();
        dump( "proto ids size: 0x"+dumpLong( protoIdsSize ) );
        protoIdsOffset = readFileOffset();
        dump( "proto ids offset: 0x"+dumpLong( protoIdsOffset ) );
        fieldIdsSize = read32Bit();
        dump( "field ids size: 0x"+dumpLong( fieldIdsSize ) );
        fieldIdsOffset = readFileOffset();
        dump( "field ids offset: 0x"+dumpLong( fieldIdsOffset ) );
        methodIdsSize = read32Bit();
        dump( "method ids size: 0x"+dumpLong( methodIdsSize ) );
        methodIdsOffset = readFileOffset();
        dump( "method ids offset: 0x"+dumpLong( methodIdsOffset ) );
        classDefsSize = read32Bit();
        dump( "class defs size: 0x"+dumpLong( classDefsSize ) );
        classDefsOffset = readFileOffset();
        dump( "class defs offset: 0x"+dumpLong( classDefsOffset ) );
        dataSize = read32Bit();
        dump( "data size: 0x"+dumpLong( dataSize ) );
        dataOffset = readFileOffset();
        dump( "data offset: 0x"+dumpLong( dataOffset ) );
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getHeaderSize() {
        return headerSize;
    }

    public long getLinkSize() {
        return linkSize;
    }

    public long getLinkOffset() {
        return linkOffset;
    }

    public long getMapOffset() {
        return mapOffset;
    }

    public long getStringIdsSize() {
        return stringIdsSize;
    }

    public long getStringIdsOffset() {
        return stringIdsOffset;
    }

    public long getTypeIdsSize() {
        return typeIdsSize;
    }

    public long getTypeIdsOffset() {
        return typeIdsOffset;
    }

    public long getProtoIdsSize() {
        return protoIdsSize;
    }

    public long getProtoIdsOffset() {
        return protoIdsOffset;
    }

    public long getFieldIdsSize() {
        return fieldIdsSize;
    }

    public long getFieldIdsOffset() {
        return fieldIdsOffset;
    }

    public long getMethodIdsSize() {
        return methodIdsSize;
    }

    public long getMethodIdsOffset() {
        return methodIdsOffset;
    }

    public long getClassDefsSize() {
        return classDefsSize;
    }

    public long getClassDefsOffset() {
        return classDefsOffset;
    }

    public long getDataSize() {
        return dataSize;
    }

    public long getDataOffset() {
        return dataOffset;
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }


    private DexSignatureBlock dexSignatureBlock;
    private long fileSize;
    private long headerSize;
    private long linkSize;
    private long linkOffset;
    private long mapOffset;
    private long stringIdsSize;
    private long stringIdsOffset;
    private long typeIdsSize;
    private long typeIdsOffset;
    private long protoIdsSize;
    private long protoIdsOffset;
    private long fieldIdsSize;
    private long fieldIdsOffset;
    private long methodIdsSize;
    private long methodIdsOffset;
    private long classDefsSize;
    private long classDefsOffset;
    private long dataSize;
    private long dataOffset;
}
