/**
  * Parses the class id table
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.*;

public class DexClassDefsBlock extends DexParser {
    public static final int ACCESS_PUBLIC = 0x0001;
    public static final int ACCESS_PRIVATE = 0x0002;
    public static final int ACCESS_PROTECTED = 0x0004;
    public static final int ACCESS_STATIC = 0x0008;
    public static final int ACCESS_FINAL = 0x0010;
    public static final int ACCESS_VOLATILE = 0x0040;
    public static final int ACCESS_TRANSIENT = 0x0080;
    public static final int ACCESS_NATIVE = 0x0100;
    public static final int ACCESS_INTERFACE = 0x0200;
    public static final int ACCESS_ABSTRACT = 0x0400;
    public static final int ACCESS_STRICTFP = 0x0800;
    public static final int ACCESS_ANNOTATION = 0x2000;
    public static final int ACCESS_ENUM = 0x4000;
    public static final int DECLARED_SYNCHRONIZED = 0x20000;

    public void parse() throws IOException {
        setDexOptimizationData( dexSignatureBlock.getDexOptimizationData() );
        int classSize = (int)dexPointerBlock.getClassDefsSize();
        file.seek( dexPointerBlock.getClassDefsOffset() );
        classMap = new HashMap<Integer,ClassHolder>();
        HashMap<Integer,TempClassHolder> tempClassMap = new HashMap<Integer,TempClassHolder>();
        for( int i = 0 ; i < classSize ; ++i ) {
            int classIdx = (int)read32Bit();
            ClassHolder classHolder = new ClassHolder();
            TempClassHolder tempClassHolder = new TempClassHolder();
            classHolder.access = (int)read32Bit();
            classHolder.superclassTypeId = (int)read32Bit();
            tempClassHolder.interfacesOffset = readFileOffset();
            classHolder.sourceFileNameStringId = (int)read32Bit();
            tempClassHolder.annotationsOffset = readFileOffset();
            tempClassHolder.classDataOffset = readFileOffset();
            tempClassHolder.staticValuesOffset = readFileOffset();
            Integer mapKey = new Integer( classIdx );
            classMap.put( mapKey,classHolder );
            tempClassMap.put( mapKey,tempClassHolder );
            dump( accessFlagsToString( classHolder.access,ItemType.CLASS )+
                    dexTypeIdsBlock.getClassName( classIdx ) );
        }
// Now process the auxiliary data blocks according to the offsets stored 
// in TempClassHolder instances
        for( Iterator<Integer> it = classMap.keySet().iterator() ;
                it.hasNext() ; ) {
            Integer key = it.next();
            ClassHolder classHolder = classMap.get( key );
            dump( "// "+accessFlagsToString( classHolder.access,ItemType.CLASS )+
                " class "+dexTypeIdsBlock.getClassName( key.intValue() )+":" );
            if( classHolder.superclassTypeId >= 0 )
                dump( "// super: "+
                    dexTypeIdsBlock.getClassName( classHolder.superclassTypeId ) );
            if( classHolder.sourceFileNameStringId >= 0 )
                dump( "// source: "+
                    dexStringIdsBlock.getString( classHolder.sourceFileNameStringId ) );
            TempClassHolder tempClassHolder = tempClassMap.get( key );
// Build the interface type table for the class
            if( tempClassHolder.interfacesOffset != 0L ) {
                file.seek( tempClassHolder.interfacesOffset );
                int interfacesSize = (int)read32Bit();
                classHolder.interfaceTypes = new int[ interfacesSize ];
                for( int i = 0 ; i < interfacesSize ; ++i ) {
                    classHolder.interfaceTypes[i] = read16Bit();
                    dump( "// implements "+
                            dexTypeIdsBlock.getType( classHolder.interfaceTypes[i] ) );
                }
            }
// Build the class data table
            if( tempClassHolder.classDataOffset != 0L ) {
                file.seek( tempClassHolder.classDataOffset );
                int staticFieldsSize = (int)readVLN();
                dump( "static fields size: "+staticFieldsSize );
                int instanceFieldsSize = (int)readVLN();
                dump( "instance fields size: "+instanceFieldsSize );
                int directMethodsSize = (int)readVLN();
                dump( "direct methods size: "+directMethodsSize );
                int virtualMethodsSize = (int)readVLN();
                dump( "virtual methods size: "+virtualMethodsSize );
// Process the static fields
                if( staticFieldsSize > 0 ) {
                    classHolder.staticFields = new FieldHolder[ staticFieldsSize ];
                    int fieldIdCounter = 0;
                    for( int i = 0 ; i < staticFieldsSize ; ++i ) {
                        FieldHolder staticFieldHolder = new FieldHolder();
                        fieldIdCounter += (int)readVLN();
                        staticFieldHolder.fieldId = fieldIdCounter;
                        staticFieldHolder.access = (int)readVLN();
                        classHolder.staticFields[i] = staticFieldHolder;
                        dump( "// static field["+i+"]: "+
                                dexFieldIdsBlock.getFieldShortName( 
                                    staticFieldHolder.fieldId ) );
                    }
                }
// Process the instance fields
                if( instanceFieldsSize > 0 ) {
                    classHolder.instanceFields = new FieldHolder[ instanceFieldsSize ];
                    int fieldIdCounter = 0;
                    for( int i = 0 ; i < instanceFieldsSize ; ++i ) {
                        FieldHolder instanceFieldHolder = new FieldHolder();
                        fieldIdCounter += (int)readVLN();
                        instanceFieldHolder.fieldId = fieldIdCounter;
                        instanceFieldHolder.access = (int)readVLN();
                        classHolder.instanceFields[i] = instanceFieldHolder;
                        dump( "// instance field["+i+"]: "+
                                dexFieldIdsBlock.getFieldShortName( 
                                    instanceFieldHolder.fieldId ) );
                    }
                }
// Process the direct methods
                if( directMethodsSize > 0 ) {
                    classHolder.directMethods = new MethodHolder[ directMethodsSize ];
                    int methodIdCounter = 0;
                    for( int i = 0 ; i < directMethodsSize ; ++i ) {
                        MethodHolder directMethodHolder = new MethodHolder();
                        methodIdCounter += (int)readVLN();
                        directMethodHolder.methodId = methodIdCounter;
                        directMethodHolder.access = (int)readVLN();
                        directMethodHolder.offset = readFileOffsetVLN();
                        classHolder.directMethods[i] = directMethodHolder;
                        dump( "// direct method["+i+"]: "+
                                dexMethodIdsBlock.getProto( 
                                    directMethodHolder.methodId ) );
                    }
                }
// Process the virtual methods
                if( virtualMethodsSize > 0 ) {
                    classHolder.virtualMethods = new MethodHolder[ virtualMethodsSize ];
                    int methodIdCounter = 0;
                    for( int i = 0 ; i < virtualMethodsSize ; ++i ) {
                        MethodHolder virtualMethodHolder = new MethodHolder();
                        methodIdCounter += (int)readVLN();
                        virtualMethodHolder.methodId = methodIdCounter;
                        virtualMethodHolder.access = (int)readVLN();
                        virtualMethodHolder.offset = readFileOffsetVLN();
                        classHolder.virtualMethods[i] = virtualMethodHolder;
                        dump( "// virtual method["+i+"]: "+
                                dexMethodIdsBlock.getProto( 
                                    virtualMethodHolder.methodId ) );
                    }
                }
            }
			if( tempClassHolder.staticValuesOffset != 0L ) {
				DexEncodedArrayParser deap = new DexEncodedArrayParser();
				deap.setRandomAccessFile( file );
				deap.setDumpFile( dump );
				deap.setDexStringIdsBlock( dexStringIdsBlock );
				deap.setDexTypeIdsBlock( dexTypeIdsBlock );
				deap.setDexFieldIdsBlock( dexFieldIdsBlock );
				deap.setDexMethodIdsBlock( dexMethodIdsBlock );
				deap.setFilePosition( tempClassHolder.staticValuesOffset );
				deap.parse();
				for( int i = 0 ; i < deap.getArraySize() ; ++i ) {
					if( i < classHolder.staticFields.length ) { // paranoia, it has to be smaller
						FieldHolder fieldHolder = classHolder.staticFields[i];
						fieldHolder.initialValue = deap.getArrayElement( i );
					}
				}
			}
            if( tempClassHolder.annotationsOffset != 0L ) {
                DexAnnotationParser dexAnnotationParser = new DexAnnotationParser();
                dexAnnotationParser.setRandomAccessFile( file );
                dexAnnotationParser.setDumpFile( dump );
                dexAnnotationParser.setDexTypeIdsBlock( dexTypeIdsBlock );
                dexAnnotationParser.setDexStringIdsBlock( dexStringIdsBlock );
                dexAnnotationParser.setDexFieldIdsBlock( dexFieldIdsBlock );
                dexAnnotationParser.setDexMethodIdsBlock( dexMethodIdsBlock );
                dexAnnotationParser.setDexSignatureBlock( dexSignatureBlock );
                try {
                    dexAnnotationParser.parse( tempClassHolder.annotationsOffset );
                } catch( UnknownInstructionException ex ) {}    // can't happen
                classHolder.annotationParser = dexAnnotationParser;
            }
        }
    }

    public Iterator<Integer> getClassIterator() {
        return classMap.keySet().iterator();
    }

    public boolean isInterface( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return false;
        return ( classHolder.access & ACCESS_INTERFACE ) != 0;
    }

    public String getClassName( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        StringBuilder b = new StringBuilder();
        b.append( accessFlagsToString( classHolder.access,ItemType.CLASS ) );
        b.append( dexTypeIdsBlock.getClassName( classIdx ) );
        return new String( b );
    }

    public String getClassNameOnly( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        StringBuilder b = new StringBuilder();
        b.append( dexTypeIdsBlock.getClassName( classIdx ) );
        return new String( b );
    }

    public String getSuperClass( int classIdx ) {
        if( classIdx < 0 )  // if the class has no superclass
            return null;
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        int superType = classHolder.superclassTypeId;
        if( superType < 0 )
            return null;
        return dexTypeIdsBlock.getClassName( superType );
    }

    public String getSourceName( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        return classHolder.sourceFileNameStringId >= 0 ?
                dexStringIdsBlock.getString( classHolder.sourceFileNameStringId ) :
                null;
    }

    public int getInterfacesSize( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        if( classHolder.interfaceTypes == null )
            return 0;
        return classHolder.interfaceTypes.length;
    }

    public String getInterface( int classIdx, int interfaceIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        return dexTypeIdsBlock.getClassName(
                classHolder.interfaceTypes[ interfaceIdx ] );
    }

    public int getStaticFieldsSize( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        if( classHolder.staticFields == null )
            return 0;
       return classHolder.staticFields.length;
    }

    public String getStaticField( int classIdx, int fieldIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        FieldHolder fieldHolder = classHolder.staticFields[ fieldIdx ];
        return getFullFieldName( fieldHolder );
    }

    public String getStaticFieldShortName( int classIdx, int fieldIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        FieldHolder fieldHolder = classHolder.staticFields[ fieldIdx ];
        return dexFieldIdsBlock.getFieldName( fieldHolder.fieldId );
    }

	public Object getStaticFieldInitializer( int classIdx, int fieldIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        FieldHolder fieldHolder = classHolder.staticFields[ fieldIdx ];
        return fieldHolder.initialValue;
	}

    public int getInstanceFieldsSize( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        if( classHolder.instanceFields == null )
            return 0;
        return classHolder.instanceFields.length;
    }

    public String getInstanceField( int classIdx, int fieldIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        FieldHolder fieldHolder = classHolder.instanceFields[ fieldIdx ];
        return getFullFieldName( fieldHolder );
    }

    public String getInstanceFieldShortName( int classIdx, int fieldIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        FieldHolder fieldHolder = classHolder.instanceFields[ fieldIdx ];
        return dexFieldIdsBlock.getFieldName( fieldHolder.fieldId );
    }

    public String getInstanceFieldNameAndType( int classIdx, int fieldIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        FieldHolder fieldHolder = classHolder.instanceFields[ fieldIdx ];
        return dexFieldIdsBlock.getFieldShortName( fieldHolder.fieldId );
    }

    public int getDirectMethodsFieldsSize( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        if( classHolder.directMethods == null )
            return 0;
        return classHolder.directMethods.length;
    }

    public String getDirectMethodName( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        MethodHolder methodHolder = classHolder.directMethods[ methodIdx ];
        return getFullMethodName( methodHolder );
    }

    public String getDirectMethodShortName( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        MethodHolder methodHolder = classHolder.directMethods[ methodIdx ];
        return DexMethodIdsBlock.getMethodName(
                dexMethodIdsBlock.getMethod( methodHolder.methodId ) );
    }

    public int getDirectMethodAccess( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        MethodHolder methodHolder = classHolder.directMethods[ methodIdx ];
        return methodHolder.access;
    }


    public long getDirectMethodOffset( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        MethodHolder methodHolder = classHolder.directMethods[ methodIdx ];
        return methodHolder.offset;
    }

    public ArrayList getDirectMethodParameterOffsets( int classIdx, int methodIdx, int regSize ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        MethodHolder methodHolder = classHolder.directMethods[ methodIdx ];
        return getMethodParameterOffsets( methodHolder, regSize );
    }

    public int getVirtualMethodsFieldsSize( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        if( classHolder.virtualMethods == null )
            return 0;
        return classHolder.virtualMethods.length;
    }

    public String getVirtualMethodName( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        MethodHolder methodHolder = classHolder.virtualMethods[ methodIdx ];
        return getFullMethodName( methodHolder );
    }

    public String getVirtualMethodShortName( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        MethodHolder methodHolder = classHolder.virtualMethods[ methodIdx ];
        return DexMethodIdsBlock.getMethodName(
                dexMethodIdsBlock.getMethod( methodHolder.methodId ) );
    }

    public int getVirtualMethodId( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        MethodHolder methodHolder = classHolder.virtualMethods[ methodIdx ];
        return methodHolder.methodId;
    }

    public int getVirtualMethodAccess( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        MethodHolder methodHolder = classHolder.virtualMethods[ methodIdx ];
        return methodHolder.access;
    }


    public long getVirtualMethodOffset( int classIdx, int methodIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return -1;
        MethodHolder methodHolder = classHolder.virtualMethods[ methodIdx ];
        return methodHolder.offset;
    }

    public ArrayList getVirtualMethodParameterOffsets( int classIdx, int methodIdx, int regSize ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        MethodHolder methodHolder = classHolder.virtualMethods[ methodIdx ];
        return getMethodParameterOffsets( methodHolder, regSize );
    }

    public DexAnnotationParser getDexAnnotationParser( int classIdx ) {
        Integer i = new Integer( classIdx );
        ClassHolder classHolder = classMap.get( i );
        if( classHolder == null )
            return null;
        return classHolder.annotationParser;
    }

    public static String getClassNameWithoutPackage( String fullClassName ) {
        int idx = fullClassName.lastIndexOf( '$' );
        if( idx < 0 )
            return fullClassName;
        String cn = fullClassName.substring( idx+1 );
        if( cn.endsWith( ";" ) )
            cn = cn.substring( 0,cn.length() - 1 );
        return cn;
    }

    public static String getClassNameWithoutPrePostfix( String fullClassName ) {
        String cn = fullClassName;
        if( cn.startsWith( "L" ) )
            cn = cn.substring( 1 );
        if( cn.endsWith( ";" ) )
            cn = cn.substring( 0,cn.length() - 1 );
        return cn;
    }

/** Returns the register offsets and the types of the method parametes.
 * The arraylist returned contains the following values: offset_0,type_0, offset_1,type_1 ...
 * where offset_n is the register offset of the nth parameter and type_n is the type of the nth
 * parameter.
 * @param proto The prototype of the method
 * @param regSize The total number of registers allocated to the method
 * @return Returns the register offsets and their types in a list as described above.
 */
    public static ArrayList getMethodParameterOffsets( String proto, int regSize ) {
        int idx1 = proto.indexOf( '(' );
        if( idx1 < 0 )
            return null;
        ++idx1;
        int idx2 = proto.indexOf( ')',idx1 );
        if( idx2 < 0 )
            return null;
        String parms = proto.substring( idx1, idx2 );
        ArrayList returnList = new ArrayList();
        int idx = 0;
        int regIdx = 0;
        int allocLength = 0;
        int nextIdx = 0;
        while( idx < parms.length() ) {
            switch( parms.charAt( idx ) ) {
                case '[':
                    allocLength = 1;
                    while (nextIdx < parms.length() && parms.charAt(nextIdx) == '[')
                        nextIdx++;
                    if( ( nextIdx < parms.length() ) &&
                        parms.charAt( nextIdx ) == 'L' ) {
                        nextIdx = parms.indexOf( ';', nextIdx );
                        nextIdx = nextIdx < 0 ? parms.length() : nextIdx + 1;
                    } else
                        nextIdx++;
                    break;

                case 'L':
                    allocLength = 1;
                    nextIdx = parms.indexOf( ';',idx );
                    nextIdx = nextIdx < 0 ? parms.length() : nextIdx + 1;
                    break;

                case 'J':
                case 'D':
                    nextIdx = idx + 1;
                    allocLength = 2;
                    break;

                default:
                    nextIdx = idx + 1;
                    allocLength = 1;
                    break;
            }
            String parm = parms.substring( idx,nextIdx );
            returnList.add( new Integer( regIdx ) );
            returnList.add( parm );
            idx = nextIdx;
            regIdx += allocLength;
        }
// Parameter registers are allocated at the end of the register block. Now that we know the full
// length of the parameter register block, we recalculate the register offsets
        int offset = regSize - regIdx;
        for( int i = 0 ; i < returnList.size() ; i += 2 ) {
            int regOffset = ((Integer)returnList.get( i )).intValue();
            returnList.set( i,new Integer( regOffset + offset ) );
        }
        return returnList;
    }

/** Returns the width of the method parameters. Elements in the result list are 
 * Booleans. The value of that Boolean is true if the corresponding method 
 * parameter is wide (double or long)
 * @param proto The prototype of the method
 * @return Returns the register offsets and their types in a list as described above.
 */
    public static ArrayList<Boolean> getMethodParameterWidth( String proto ) {
        int idx1 = proto.indexOf( '(' );
        if( idx1 < 0 )
            return null;
        ++idx1;
        int idx2 = proto.indexOf( ')',idx1 );
        if( idx2 < 0 )
            return null;
        String parms = proto.substring( idx1, idx2 );
        ArrayList<Boolean> returnList = new ArrayList<Boolean>();
        int idx = 0;
        int allocLength = 0;
        int nextIdx = 0;
        Boolean wideFlag = null;
        while( idx < parms.length() ) {
            switch( parms.charAt( idx ) ) {
                case '[':
                    wideFlag = Boolean.FALSE;
                    if( ( idx+1 < parms.length() ) && 
                        parms.charAt( idx+1 ) == 'L' ) {
                        nextIdx = parms.indexOf( ';',idx );
                        nextIdx = nextIdx < 0 ? parms.length() : nextIdx + 1;
                    } else
                        nextIdx = idx+2;
                    break;

                case 'L':
                    wideFlag = Boolean.FALSE;
                    nextIdx = parms.indexOf( ';',idx );
                    nextIdx = nextIdx < 0 ? parms.length() : nextIdx + 1;
                    break;

                case 'J':
                case 'D':
                    nextIdx = idx + 1;
                    wideFlag = Boolean.TRUE;
                    break;

                default:
                    nextIdx = idx + 1;
                    wideFlag = Boolean.FALSE;
                    break;
            }
            returnList.add( wideFlag );
            idx = nextIdx;
        }
        return returnList;
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

    public void setDexFieldIdsBlock( DexFieldIdsBlock dexFieldIdsBlock ) {
        this.dexFieldIdsBlock = dexFieldIdsBlock;
    }

    public void setDexMethodIdsBlock( DexMethodIdsBlock dexMethodIdsBlock ) {
        this.dexMethodIdsBlock = dexMethodIdsBlock;
    }

    public DexMethodIdsBlock getDexMethodIdsBlock() {
        return dexMethodIdsBlock;
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }

    private HashMap<Integer,ClassHolder> classMap;
    private DexPointerBlock     dexPointerBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexTypeIdsBlock     dexTypeIdsBlock = null;
    private DexFieldIdsBlock    dexFieldIdsBlock = null;
    private DexMethodIdsBlock   dexMethodIdsBlock = null;
    private DexSignatureBlock   dexSignatureBlock = null;
    private enum ItemType {
        CLASS,
        FIELD,
        METHOD
    }

    private String accessFlagsToString( int accessFlags, ItemType itemType ) {
        StringBuilder b = new StringBuilder();
        if( ( accessFlags & ACCESS_PUBLIC ) != 0 )
            b.append( "public " );
        if( ( accessFlags & ACCESS_PRIVATE ) != 0 )
            b.append( "private " );
        if( ( accessFlags & ACCESS_PROTECTED ) != 0 )
            b.append( "protected " );
        if( ( accessFlags & ACCESS_ABSTRACT ) != 0 )
            b.append( "abstract " );
        if( ( accessFlags & ACCESS_STATIC ) != 0 )
            b.append( "static " );
        if( ( accessFlags & ACCESS_FINAL ) != 0 )
            b.append( "final " );
        if( ( ( accessFlags & ACCESS_VOLATILE ) != 0 ) &&
                ( itemType == ItemType.FIELD ) )
            b.append( "volatile " );
        if( ( ( accessFlags & ACCESS_TRANSIENT ) != 0 ) &&
                ( itemType == ItemType.FIELD ) )
            b.append( "transient " );
        if( ( ( accessFlags & ACCESS_NATIVE ) != 0 ) &&
                ( itemType == ItemType.METHOD ) )
            b.append( "native " );
        if( ( accessFlags & ACCESS_ANNOTATION ) != 0 )
            b.append( "annotation " );
        if( ( accessFlags & DECLARED_SYNCHRONIZED ) != 0 )
            b.append( "synchronized " );
        return new String( b );
    }

    private String getFullFieldName( FieldHolder fieldHolder ) {
        StringBuilder b = new StringBuilder();
        b.append( accessFlagsToString( fieldHolder.access, ItemType.FIELD ) );
        b.append( dexFieldIdsBlock.getFieldShortName( 
                        fieldHolder.fieldId ) );
        return new String( b );
    }

    private String getFullMethodName( MethodHolder methodHolder ) {
        StringBuilder b = new StringBuilder();
        b.append( accessFlagsToString( methodHolder.access,ItemType.METHOD ) );
        b.append( dexMethodIdsBlock.getProto( 
                        methodHolder.methodId ) );
        return new String( b );
    }

    private ArrayList getMethodParameterOffsets( MethodHolder methodHolder, int regSize ) {
        String proto = dexMethodIdsBlock.getProto( methodHolder.methodId );
        return getMethodParameterOffsets( proto, regSize );
    }

// Temporary instance to store file offsets before we start to seek to these offsets
    class TempClassHolder {
        long interfacesOffset;
        long annotationsOffset;
        long classDataOffset;
        long staticValuesOffset;
    }

    class ClassHolder {
        int access;     // access flags
        int superclassTypeId;   // type id of the superclass
        int sourceFileNameStringId; // string id of the source file 
        int interfaceTypes[] = null;    // vector of type ids of implemented interfaces
        FieldHolder staticFields[] = null;  // vector of static fields
        FieldHolder instanceFields[] = null; // vector of instance fields
        MethodHolder directMethods[] = null; // vector of direct methods
        MethodHolder virtualMethods[] = null; // vector of virtual methods
        DexAnnotationParser annotationParser = null;    // annotation parser (if the class has annotations)
    }

    class FieldHolder {
        int fieldId;    // id of the field
        int access;     // access flags
		Object initialValue;	// initial value (if any)
    }

    class MethodHolder {
        int methodId;    // id of the method
        int access;     // access flags
        long offset;    // method code offset
    }

}
