/**
  * Parses a DEX encoded array.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexEncodedArrayParser extends DexParser {

	public static final int VALUE_BYTE = 0x00;
	public static final int VALUE_SHORT = 0x02;
	public static final int VALUE_CHAR = 0x03;
	public static final int VALUE_INT = 0x04;
	public static final int VALUE_LONG = 0x06;
    public static final int VALUE_UNKNOWN1 = 0x0F;
	public static final int VALUE_FLOAT = 0x10;
	public static final int VALUE_DOUBLE = 0x11;
	public static final int VALUE_STRING = 0x17;
	public static final int VALUE_TYPE = 0x18;
    public static final int VALUE_FIELD = 0x19;
    public static final int VALUE_METHOD = 0x1a;
    public static final int VALUE_ENUM = 0x1b;
    public static final int VALUE_ARRAY = 0x1c;
    public static final int VALUE_ANNOTATION = 0x1d;
    public static final int VALUE_NULL = 0x1e;
    public static final int VALUE_BOOLEAN = 0x1f;


    public void parse() throws IOException {
		int arrayItemNum = (int)readVLN();
		dump( "array item count: "+arrayItemNum );
		elements = new Object[ arrayItemNum ];
		for( int i = 0 ; i < arrayItemNum ; ++i ) {
			elements[i] = readElement();
			dump( "Array element["+i+"]: "+elements[i] );
		}
    }

	public int getArraySize() {
		return elements.length;
	}

	public Object getArrayElement( int idx ) {
		return elements[idx];
	}

/**
  * Reads an encoded value (encoded_value in the DEX spec.)
  * @return The encoded 
  */
	public Object readElement() throws IOException {
		int b = read8Bit();
		int valueArg = ( b & 0xE0 ) >> 5;
		int valueType = b & 0x1F;
		Object returnObject = null;
		switch( valueType ) {
			case VALUE_BYTE: {
				int bv = readSigned8Bit();
				returnObject = new Byte( (byte)bv );
			}
			break;

			case VALUE_SHORT: {
				long sv = readVLNWithLength( valueArg + 1,true );
				returnObject = new Short( (short)sv );
			}
			break;

			case VALUE_CHAR: {
				long cv = readVLNWithLength( valueArg + 1,false );
				returnObject = new StaticCharacter( (char)cv );
			}
			break;

			case VALUE_INT: {
                long lv = readVLNWithLength( valueArg + 1,true );
				int iv = (int)lv;
				returnObject = new Integer( (int)iv );
			}
			break;

			case VALUE_LONG: {
				long lv = readVLNWithLength( valueArg + 1,true );
				returnObject = new Long( lv );
			}
			break;

/*
            case VALUE_UNKNOWN1: {
                returnObject = new String( "Unknown type (0x0F)" );
            }
            break;
*/

			case VALUE_FLOAT: {
				long lv = readFloatingPointVLNWithLength( valueArg + 1 ) >> 32;
				returnObject = new Float( Float.intBitsToFloat( (int)lv ) );
			}
			break;

			case VALUE_DOUBLE: {
				long lv = readFloatingPointVLNWithLength( valueArg + 1 );
				returnObject = new Double( Double.longBitsToDouble( lv ) );
			}
			break;

			case VALUE_STRING: {
                long si = readVLNWithLength( valueArg + 1,false );
                String stringValue = 
                    dexStringIdsBlock.getString( (int)si );
                returnObject = new StaticString( stringValue );
            }
            break;

            case VALUE_TYPE: {
                long ti = readVLNWithLength( valueArg + 1,false );
                String typeValue = 
                    dexTypeIdsBlock.getType( (int)ti );
                returnObject = typeValue;
            }
            break;

			case VALUE_FIELD: {
                long fi = readVLNWithLength( valueArg + 1,false );
                String fieldValue = 
                    dexFieldIdsBlock.getField( (int)fi );
                returnObject = fieldValue;
            }
            break;

            case VALUE_METHOD: {
                long mi = readVLNWithLength( valueArg + 1,false );
                String methodName = 
                    dexMethodIdsBlock.getMethod( (int)mi );
                String proto = 
                    dexMethodIdsBlock.getProto( (int)mi );
                String methodValue = DexMethodIdsBlock.
                    combineMethodNameAndProto( methodName, proto );
                returnObject = methodValue;
            }
            break;

            case VALUE_ENUM: {
                long fi = readVLNWithLength( valueArg + 1,false );
                String enumValue = dexFieldIdsBlock.getFieldName( (int)fi );
                returnObject = enumValue;
            }
            break;

            case VALUE_ARRAY: {
                DexEncodedArrayParser arrayParser = new DexEncodedArrayParser();
                arrayParser.setRandomAccessFile( file );
                arrayParser.setDumpFile( dump );
                arrayParser.setDexStringIdsBlock( dexStringIdsBlock );
                arrayParser.setDexTypeIdsBlock( dexTypeIdsBlock );
                arrayParser.setDexFieldIdsBlock( dexFieldIdsBlock );
                arrayParser.setDexMethodIdsBlock( dexMethodIdsBlock );
                arrayParser.parse();
                int arraySize = arrayParser.getArraySize();
                StaticArray array = new StaticArray( arraySize );
                for( int i = 0 ; i < arraySize ; ++i )
                    array.set( i,arrayParser.getArrayElement( i ) );
                returnObject = array;
            }
            break;

            case VALUE_ANNOTATION: {
                DexAnnotationParser annotationParser = new DexAnnotationParser();
                annotationParser.setRandomAccessFile( file );
                annotationParser.setDumpFile( dump );
                annotationParser.setDexTypeIdsBlock( dexTypeIdsBlock );
                annotationParser.setDexStringIdsBlock( dexStringIdsBlock );
                annotationParser.setDexFieldIdsBlock( dexFieldIdsBlock );
                annotationParser.setDexMethodIdsBlock( dexMethodIdsBlock );
                AnnotationHolder holder = new AnnotationHolder();
                holder.newAnnotation();
                annotationParser.readEncodedAnnotation(
                        holder,
                        "sub-annotation" );
                StaticAnnotation s = new StaticAnnotation( holder );
                returnObject = s;
            }
            break;

            case VALUE_NULL: {
					returnObject = "null";
				}
                break;  

            case VALUE_BOOLEAN: {
                returnObject = new Boolean( valueArg != 0 );
            }
            break;


			default:
				throw new IOException( "Unhandled array type (0x"+
							dumpByte( valueType ) + ") at 0x"+
							Long.toHexString( getFilePosition()-1L ) );
							
		}
		return returnObject;
	}

// Gets the type string out of the object type returned by readElement
    public static String getTypeString( Object o ) {
        Object obj = o;
        String typeString = "";
        if( obj instanceof StaticArray ) {
            typeString += "[";
            StaticArray array = (StaticArray)obj;
            obj = array.length() == 0 ? null : array.get( 0 );
        }
        if( obj != null ) {
            if( obj instanceof Boolean )
                typeString += "Z";
            else
            if( obj instanceof Byte )
                typeString += "B";
            else
            if( obj instanceof Short )
                typeString += "S";
            else
            if( obj instanceof StaticCharacter )
                typeString += "C";
            else
            if( obj instanceof Integer )
                typeString += "I";
            else
            if( obj instanceof Long )
                typeString += "J";
            else
            if( obj instanceof Float )
                typeString += "F";
            else
            if( obj instanceof Double )
                typeString += "D";
            else
            if( obj instanceof StaticString )
                typeString += "Ljava/lang/String;";
            else
            if( obj instanceof String )
                typeString += "<string>";
            else
            if( obj instanceof StaticAnnotation )
                typeString += "annotation";
            else
                typeString +="<unknown type>";
        }
        return typeString;
    }

    public void setDexStringIdsBlock( DexStringIdsBlock dexStringIdsBlock ) {
        this.dexStringIdsBlock = dexStringIdsBlock;
    }

    public void setDexTypeIdsBlock( DexTypeIdsBlock dexTypeIdsBlock ) {
        this.dexTypeIdsBlock = dexTypeIdsBlock;
    }

    public void setDexFieldIdsBlock( DexFieldIdsBlock dexFieldIdsBlock ) {
        this.dexFieldIdsBlock = dexFieldIdsBlock;
    }

    public void setDexMethodIdsBlock( DexMethodIdsBlock dexMethodIdsBlock ) {
        this.dexMethodIdsBlock = dexMethodIdsBlock;
    }

	private Object elements[];
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexTypeIdsBlock     dexTypeIdsBlock = null;
    private DexFieldIdsBlock    dexFieldIdsBlock = null;
    private DexMethodIdsBlock   dexMethodIdsBlock = null;

}
