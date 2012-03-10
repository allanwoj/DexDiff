package patch;

import item.AnnotationItem;
import item.EncodedAnnotation;
import item.EncodedArray;
import item.EncodedValue;
import item.FieldIdItem;

import java.io.IOException;

import hu.uw.pallergabor.dedexer.DexParser;

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
    
    private long typeListSize;
    private long typeListOffset;
    
    private long annotationSetRefListSize;
    private long annotationSetRefListOffset;
    
    private long annotationSetItemSize;
    private long annotationSetItemOffset;
    
    private long classDataItemSize;
    private long classDataItemOffset;
    
    private long codeItemSize;
    private long codeItemOffset;
    
    private long stringDataItemSize;
    private long stringDataItemOffset;
    
    private long debugInfoItemSize;
    private long debugInfoItemOffset;
    
    private long annotationItemSize;
    private long annotationItemOffset;
    
    private long encodedArrayItemSize;
    private long encodedArrayItemOffset;
    
    private long annotationsDirectoryItemSize;
    private long annotationsDirectoryItemOffset;
    
    String[] stringData;
    int stringDataIndex = 0;
    
    int[] typeIds;
    int typeIdsIndex = 0;
    
    FieldIdItem[] fieldIds;
    int fieldIdsIndex = 0;
    
    AnnotationItem[] annotationItems;
    int annotationItemsIndex = 0;

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
	        
	        typeIds = new int[(int)typeIdsSize];
	        // Read type_ids
	        for(int i = 0 ; i < typeIdsSize; ++i) {
	        	typeIds[i] = (int) read32Bit();
	        }
	        
	        stringData = new String[(int)stringIdsSize];
	        // Read string_data_items
	        for(int i = 0 ; i < stringIdsSize; ++i) {
	        	setFilePosition(stringsPos[i]);
	        	stringData[i] = readString();
	        }
	        
	        setFilePosition(fieldIdsOffset);
	        fieldIds = new FieldIdItem[(int) fieldIdsSize];
	        // Read field_ids
	        for (int i = 0; i < fieldIdsSize; ++i) {
				fieldIds[i] = new FieldIdItem(read16Bit(), read16Bit(), (int)read32Bit());
			}
	        
	        setFilePosition(mapOffset);
	        // Read offsets
	        int mapSize = (int)read32Bit();
	        for (int i = 0; i < mapSize; ++i) {
	        	int type = read16Bit();
	        	read16Bit();
	        	switch (type) {
		        	/*case 0:break;
		        	case 1:break;
		        	case 2:break;
		        	case 3:break;
		        	case 4:break;
		        	case 5:break;
		        	case 6:break;
		        	case 4096:break;*/
		        	case 4097:
		        		typeListSize = read32Bit();
		        		typeListOffset = read32Bit();
		        		break;
		        	case 4098:
		        		annotationSetRefListSize = read32Bit();
		        		annotationSetRefListOffset = read32Bit();
		        		break;
		        	case 4099:
		        		annotationSetItemSize = read32Bit();
		        		annotationSetItemOffset = read32Bit();
		        		break;
		        	case 8192:
		        		classDataItemSize = read32Bit();
		        		classDataItemOffset = read32Bit();
		        		break;
		        	case 8193:
		        		codeItemSize = read32Bit();
		        		codeItemOffset = read32Bit();
		        		break;
		        	case 8194:
		        		stringDataItemSize = read32Bit();
		        		stringDataItemOffset = read32Bit();
		        		break;
		        	case 8195:
		        		debugInfoItemSize = read32Bit();
		        		debugInfoItemOffset = read32Bit();
		        		break;
		        	case 8196:
		        		annotationItemSize = read32Bit();
		        		annotationItemOffset = read32Bit();
		        		break;
		        	case 8197:
		        		encodedArrayItemSize = read32Bit();
		        		encodedArrayItemOffset = read32Bit();
		        		break;
		        	case 8198:
		        		annotationsDirectoryItemSize = read32Bit();
		        		annotationsDirectoryItemOffset = read32Bit();
		        		break;
		        	default:
		        		read32Bit();
		        		read32Bit();
	        	}
	        }
	        
	        setFilePosition(annotationItemOffset);
	        annotationItems = new AnnotationItem[(int)annotationItemSize];
	        // Read annotation_item
	        for (int i = 0; i < annotationItemSize; ++i) {
	        	int visibility = read8Bit();
	        	int type = readULEB128();
	        	int size = readULEB128();
	        	annotationItems[i] = new AnnotationItem(visibility, new EncodedAnnotation(type, size));
	        	for (int j = 0; j < size; ++j) {
	        		int name = readULEB128();
	        		Byte b = (byte) read8Bit();
	        		int valueType = b & 0x1F;
	        		int valueArg = (b & 0xFF) >> 5;

	        		if (valueType == 0x1C) {
	        			int anSize = readULEB128();
	        			EncodedValue[] vals = new EncodedValue[anSize];
	        			for (int k = 0; k < anSize; ++k) {
	        				vals[k] = getEncodedValue();
	        			}
	        			annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg, new EncodedArray(anSize, vals));
	        			break;
	        		} else if (valueType == 0x1D) {
	        			annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg, getEncodedAnnotation());
	        			break;
	        		} else if (valueType == 0x1E || valueArg == 0x1F) {
	        			annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg);
	        			break;
	        		}
	        	
	        		Byte[] by = new Byte[valueArg + 1];
	        		for (int k = 0; k <= valueArg; ++k) {
	        			by[k] = (byte)read8Bit();
	        		}
	        		
	        		annotationItems[i].getAnnotation().values[j] = new EncodedValue(name, valueType, valueArg, by);
	        		
	        	}
	        	
	        }
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private EncodedValue getEncodedValue() throws IOException {
		Byte b = (byte) read8Bit();
		int valueType = b & 0x1F;
		int valueArg = (b & 0xFF) >> 5;

		if (valueArg == 0x1C) {
			int anSize = readULEB128();
			EncodedValue[] vals = new EncodedValue[anSize];
			for (int k = 0; k < anSize; ++k) {
				vals[k] = getEncodedValue();
			}
			return new EncodedValue(-1, valueType, valueArg, new EncodedArray(anSize, vals));
		} else if (valueArg == 0x1D) {
			return new EncodedValue(-1, valueType, valueArg, getEncodedAnnotation());
		} else if (valueArg == 0x1E || valueArg == 0x1F) {
			return new EncodedValue(-1, valueType, valueArg);
		}
	
		Byte[] by = new Byte[valueArg + 1];
		for (int k = 0; k <= valueArg; ++k) {
			by[k] = (byte)read8Bit();
		}
		
		return new EncodedValue(-1, valueType, valueArg, by);
	}
	
	private EncodedAnnotation getEncodedAnnotation() throws IOException {
		int type = readULEB128();
    	int size = readULEB128();
    	EncodedAnnotation annotation = new EncodedAnnotation(type, size);
    	
    	for (int j = 0; j < size; ++j) {
    		int name = readULEB128();
    		Byte b = (byte) read8Bit();
    		int valueType = b & 0x1F;
    		int valueArg = (b & 0xFF) >> 5;

    		if (valueArg == 0x1C) {
    			int anSize = readULEB128();
    			EncodedValue[] vals = new EncodedValue[anSize];
    			for (int k = 0; k < anSize; ++k) {
    				vals[k] = getEncodedValue();
    			}
    			annotation.values[j] = new EncodedValue(name, valueType, valueArg, new EncodedArray(anSize, vals));
    			break;
    		} else if (valueArg == 0x1D) {
    			annotation.values[j] = new EncodedValue(name, valueType, valueArg, getEncodedAnnotation());
    			break;
    		} else if (valueArg == 0x1E || valueArg == 0x1F) {
    			annotation.values[j] = new EncodedValue(name, valueType, valueArg);
    		}
    	
    		Byte[] by = new Byte[valueArg + 1];
    		for (int k = 0; k <= valueArg; ++k) {
    			by[k] = (byte)read8Bit();
    		}
    		annotation.values[j] = new EncodedValue(name, valueType, valueArg, by);
    	}
    	return annotation;
	}
	
	public String getStringData() {
		return stringData[stringDataIndex++];
	}
	
	public int getTypeIdData() {
		return typeIds[typeIdsIndex++];
	}
	
	public FieldIdItem getFieldIdData() {
		return fieldIds[fieldIdsIndex++];
	}
	
	public AnnotationItem getAnnotationItem() {
		return annotationItems[annotationItemsIndex++];
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
    
    public long getAnnotationItemSize() {
        return annotationItemSize;
    }

    public long getAnnotationItemOffset() {
        return annotationItemOffset;
    }
    
}
