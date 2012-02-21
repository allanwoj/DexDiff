package patch;

import java.io.IOException;

import hu.uw.pallergabor.dedexer.DexParser;
import hu.uw.pallergabor.dedexer.UnknownInstructionException;

public class DexOriginalFile extends DexParser {

	// Offsets and sizes
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
    
    String[] stringData;
    int dataIndex = 0;

	@Override
	public void parse() {
		try {
			setFilePosition(0x20L);
			fileSize = read32Bit();
	        headerSize = read32Bit();
	        parseExpected32Bit( 0x12345678L );
	        linkSize = read32Bit();
	        linkOffset = readFileOffset();
	        mapOffset = readFileOffset();
	        stringIdsSize = read32Bit();
	        stringIdsOffset = readFileOffset();
	        typeIdsSize = read32Bit();
	        typeIdsOffset = readFileOffset();
	        protoIdsSize = read32Bit();
	        protoIdsOffset = readFileOffset();
	        fieldIdsSize = read32Bit();
	        fieldIdsOffset = readFileOffset();
	        methodIdsSize = read32Bit();
	        methodIdsOffset = readFileOffset();
	        classDefsSize = read32Bit();
	        classDefsOffset = readFileOffset();
	        dataSize = read32Bit();
	        dataOffset = readFileOffset();
			
	        setFilePosition(stringIdsOffset);
	        long stringsPos[] = new long[(int)stringIdsSize];
	        // Read the string offsets first
	        for(int i = 0 ; i < stringIdsSize; ++i) {
	        	stringsPos[i] = readFileOffset();
	        }
	        
	        stringData = new String[(int)stringIdsSize];
	        for(int i = 0 ; i < stringIdsSize; ++i) {
	        	setFilePosition(stringsPos[i]);
	        	stringData[i] = readString();
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String getStringData() {
		return stringData[dataIndex++];
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
    
}
