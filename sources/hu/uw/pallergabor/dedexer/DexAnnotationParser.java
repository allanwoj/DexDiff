/**
  * Parses an annotation block of a class
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public class DexAnnotationParser extends DexParser {
    public static final int VISIBILITY_BUILD = 0;
    public static final int VISIBILITY_RUNTIME = 1;
    public static final int VISIBILITY_SYSTEM = 2;

    public enum AnnotationType {
        FIELD,
        METHOD,
        PARAMETER,
        CLASS
    }

    public DexAnnotationParser() {
        arrayParser = new DexEncodedArrayParser();
    }

    public void parse() throws IOException {
        setDexOptimizationData( dexSignatureBlock.getDexOptimizationData() );
        arrayParser.setRandomAccessFile( file );
        arrayParser.setDumpFile( dump );
        long classAnnotationOffset = readFileOffset();
        dump( "class annotation offset: 0x"+Long.toHexString( classAnnotationOffset ) );
        long fieldAnnotationSize = read32Bit();
        dump( "field annotation size: "+fieldAnnotationSize );
        long methodAnnotationSize = read32Bit();
        dump( "method annotation size: "+methodAnnotationSize );
        long parameterAnnotationSize = read32Bit();
        dump( "parameter annotation size: "+parameterAnnotationSize );
        if( fieldAnnotationSize != 0 ) {
            for( int i = 0 ; i < fieldAnnotationSize ; ++i ) {
                int fieldIdx = (int)read32Bit();
                dump( "field idx: "+fieldIdx );
                long annotationOffset = readFileOffset();
                dump( "annotation offset: 0x"+Long.toHexString( annotationOffset ) );
                AnnotationHolder h = new AnnotationHolder();
                h.asset = dexFieldIdsBlock.getFieldName( fieldIdx );
                h.offset = annotationOffset;
                fieldAnnotationHolders.add( h );
                fieldMap.put( h.asset,new Integer( i ) );
            }
        }
        if( methodAnnotationSize != 0 ) {
            for( int i = 0 ; i < methodAnnotationSize ; ++i ) {
                int methodIdx = (int)read32Bit();
                dump( "method idx: "+methodIdx );
                long annotationOffset = readFileOffset();
                dump( "annotation offset: 0x"+Long.toHexString( annotationOffset ) );
                AnnotationHolder h = new AnnotationHolder();
                h.asset = DexMethodIdsBlock.getMethodName( 
                            dexMethodIdsBlock.getMethod( methodIdx ) );
                h.offset = annotationOffset;
                methodAnnotationHolders.add( h );
                methodMap.put( h.asset,new Integer( i ) );
            }
        }
        if( parameterAnnotationSize != 0 ) {
            for( int i = 0 ; i < parameterAnnotationSize ; ++i ) {
                int methodIdx = (int)read32Bit();
                dump( "method idx: "+methodIdx );
                long annotationOffset = readFileOffset();
                dump( "annotation offset: 0x"+Long.toHexString( annotationOffset ) );
                AnnotationHolder h = new AnnotationHolder();
                h.asset = DexMethodIdsBlock.getMethodName(
                                dexMethodIdsBlock.getMethod( methodIdx ) );
                System.out.println( "putting method for parameter: "+h.asset );
                h.offset = annotationOffset;
                parameterAnnotationHolders.add( h );
                parameterMap.put( h.asset,new Integer( i ) );
            }
        }
        for( int i = 0 ; i < fieldAnnotationHolders.size() ; ++i ) {
            AnnotationHolder h = fieldAnnotationHolders.get( i );
            file.seek( h.offset );
            int count = (int)read32Bit();
            long addresses[] = new long[ count ];
            for( int n = 0 ; n < count ; ++n ) {
                addresses[n] = readFileOffset();
                dump( "field annotation offset: 0x"+Long.toHexString( addresses[n] ) );
            }
            for( int n = 0 ; n < count ; ++n ) {
                file.seek( addresses[n] );
                readAnnotation( h,"fieldAnnotation for "+h.asset+", entry["+n+"]" );
            }
        }
        for( int i = 0 ; i < methodAnnotationHolders.size() ; ++i ) {
            AnnotationHolder h = methodAnnotationHolders.get( i );
            file.seek( h.offset );
            int count = (int)read32Bit();
            long addresses[] = new long[ count ];
            for( int n = 0 ; n < count ; ++n ) {
                addresses[n] = readFileOffset();
                dump( "method annotation offset: 0x"+Long.toHexString( addresses[n] ) );
            }
            for( int n = 0 ; n < count ; ++n ) {
                file.seek( addresses[n] );
                readAnnotation( h,"methodAnnotation for "+h.asset+", entry["+n+"]" );
            }
        }
        for( int i = 0 ; i < parameterAnnotationHolders.size() ; ++i ) {
            AnnotationHolder h = parameterAnnotationHolders.get( i );
            file.seek( h.offset );
// count for method parameters
            int count = (int)read32Bit();
            long addresses[] = new long[ count ];
            for( int n = 0 ; n < count ; ++n ) {
                addresses[n] = readFileOffset();
                dump( "parameter annotation offset: 0x"+Long.toHexString( addresses[n] ) );
            }
            for( int n = 0 ; n < count ; ++n ) {
                file.seek( addresses[n] );
// count for annotations for each parameter. may be 0 if the parameter is not annotated
                int acount = (int)read32Bit();
                if( acount == 0 )
                    continue;
                long annotationAddresses[] = new long[acount];
                for( int k = 0 ; k < acount ; ++k ) {
                    annotationAddresses[k] = readFileOffset();
                    dump( "parameter annotation offset for parameter "+n+
                            ": 0x"+Long.toHexString( addresses[n] ) );
                }
                for( int k = 0 ; k < acount ; ++k ) {
                    file.seek( annotationAddresses[k] );
                    readAnnotation( h,"parameterAnnotation for "+h.asset+
                                        ", parameter["+n+"], entry["+k+"]" );
                    Annotation a = h.lastAnnotation();
                    a.parameterIndex = n;
                }
            }
        }
        if( classAnnotationOffset != 0L ) {
            file.seek( classAnnotationOffset );
            int count = (int)read32Bit();
            long entries[] = new long[ count ];
            for( int i = 0 ; i < count ; ++i )
                entries[i] = readFileOffset();
            for( int i = 0 ; i < count ; ++i ) {
                file.seek( entries[i] );
                AnnotationHolder classAnnotationHolder = readAnnotation( "classAnnotation" );
                classAnnotationHolders.add( classAnnotationHolder );
            }
        }
    }

    public void setRandomAccessFile( RandomAccessFile file ) {
        super.setRandomAccessFile( file );
        arrayParser.setRandomAccessFile( file );
    }

    public void setDumpFile( PrintStream dump ) {
        super.setDumpFile( dump );
        arrayParser.setDumpFile( dump );
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
    }

    public void setDexTypeIdsBlock( DexTypeIdsBlock dexTypeIdsBlock ) {
        this.dexTypeIdsBlock = dexTypeIdsBlock;
        arrayParser.setDexTypeIdsBlock( dexTypeIdsBlock );
    }

    public void setDexStringIdsBlock( DexStringIdsBlock dexStringIdsBlock ) {
        this.dexStringIdsBlock = dexStringIdsBlock;
        arrayParser.setDexStringIdsBlock( dexStringIdsBlock );
    }

    public void setDexFieldIdsBlock( DexFieldIdsBlock dexFieldIdsBlock ) {
        this.dexFieldIdsBlock = dexFieldIdsBlock;
        arrayParser.setDexFieldIdsBlock( dexFieldIdsBlock );
    }

    public void setDexMethodIdsBlock( DexMethodIdsBlock dexMethodIdsBlock ) {
        this.dexMethodIdsBlock = dexMethodIdsBlock;
        arrayParser.setDexMethodIdsBlock( dexMethodIdsBlock );
    }

/**
  * Returns the annotation block index for a given asset name.
  * @param type Type of the annotation whose mapping list we query.
  * @param asset Name of the asset to query.
  * @return index of the annotation block or -1 if the asset is not found
  */
    public int getBlockIndexFromAsset( AnnotationType type, String asset ) {
        HashMap<String,Integer> map = getAnnotationMap( type );
        Integer i = map.get( asset );
        if( i == null )
            return -1;
        return i.intValue();
    }

/**
  * Gets the number of annotation blocks for a given type of annotation.
  * @param type Type of the annotation whose list size to query
  * @return Number of elements in the list of the given annotation type.
  */
    public int getAnnotationBlocksSize( AnnotationType type ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        return annotationList.size();
    }

/**
  * Queries the number of annotations in the given annotation block with a given type
  * @param type Type of the annotation
  * @param idx Index of the annotation block 
  * @return Number of annotations in the given block.
  */
    public int getAnnotationsSize( AnnotationType type, int idx ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        if( idx >= annotationList.size() )
            return 0;
        AnnotationHolder h = annotationList.get( idx );
        return h.annotations.size();
    }

/**
  * Gets the asset associated to the given annotation block with a given type
  * @param type Type of the annotation.
  * @param idx Index of the annotation block.
  * @return Asset the annotation block is associated with.
  */
    public String getAnnotationAsset( AnnotationType type, int idx ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        return h.asset;
    }

/**
  * Gets the visibility flag of the annotation with given index.
  * @param type Type of the annotation
  * @param idx Index of the class annotation block
  * @param aidx Index of the annotation within the annotation block
  * @return visibility flag of the annotation. Use the VISIBILITY_ constants
  *  to decode.
  */
    public int getAnnotationVisibilityFlag( AnnotationType type,
                                                int idx, int aidx ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        Annotation a = h.annotations.get( aidx );
        return a.visibility;
    }

/**
  * Gets the type of the annotation  with given index.
  * @param type Type of the annotation
  * @param idx Index of the class annotation block
  * @param aidx Index of the annotation within the annotation block
  * @return Type of the annotation. 
  */
    public String getAnnotationType( AnnotationType type,
                                    int idx, int aidx ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        Annotation a = h.annotations.get( aidx );
        return a.type;
    }

/**
  * Searches for the given annotation type and returns its index if found.
  * The implementation is inefficient but we are not talking about huge amount
  * of data.
  * @param type Type of the annotation
  * @param idx Index of the class annotation block
  * @param typeToSearch Type to look for.
  * @return Index of the annotation within the annotation block with the given type or -1 
  *     if not found
  */
    public int searchAnnotationType( AnnotationType type, int idx, String typeToSearch ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        for( int i = 0 ; i < h.annotations.size() ; ++i ) {
            Annotation a = h.annotations.get( i );
            if( typeToSearch.equals( a.type ) )
                return i;
        }
        return -1;
    }

/**
  * Gets the number of elements the annotation block with given index.
  * @param type Type of the annotation
  * @param idx Index of the class annotation block
  * @param aidx Index of the annotation within the annotation block
  * @return Number of elements in the annotation. 
  */
    public int getAnnotationElementsSize( AnnotationType type,
                                    int idx, int aidx ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        Annotation a = h.annotations.get( aidx );
        return a.elementNames.size();
    }

/**
  * Gets the element name in the annotation block with given index.
  * @param type Type of the annotation
  * @param idx Index of the class annotation block
  * @param aidx Index of the annotation within the annotation block
  * @param element Index of the element within the annotation.
  * @return Element name with the given element index in the class annotation block.
  */
    public String getAnnotationElementName( AnnotationType type,
                                    int idx, int aidx, int element ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        Annotation a = h.annotations.get( aidx );
        return a.elementNames.get( element );
    }

/**
  * Gets the element value in class annotation block with given index.
  * @param idx Index of the class annotation block
  * @param aidx Index of the annotation within the annotation block
  * @param element Index of the element in the element list
  * @return Element value with the given element index in the class annotation block. 
  */
    public Object getAnnotationElementValue( AnnotationType type,
                                    int idx, int aidx, int element ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        Annotation a = h.annotations.get( aidx );
        return a.elementValues.get( element );
    }

/**
  * Gets the parameter index in class annotation block with given index. Meaningful only
  * for parameter annotations.
  * @param idx Index of the class annotation block
  * @param aidx Index of the annotation within the annotation block
  * @return Parameter index of the given annotation. 
  */
    public int getAnnotationParameterIndex( AnnotationType type,
                                    int idx, int aidx ) {
        ArrayList<AnnotationHolder> annotationList = getAnnotationList( type );
        AnnotationHolder h = annotationList.get( idx );
        Annotation a = h.annotations.get( aidx );
        return a.parameterIndex;
    }

/**
  * Figures out whether the annotation type is a known system annotation.
  * @param type Type of the annotation.
  * @return true if the annotation type is a system annotation.
  */
    public static boolean isSystemAnnotation( String type ) {
        for( int i = 0 ; i < systemAnnotations.length ; ++i ) {
            if( type.equals( systemAnnotations[i] ) )
                return true;
        }
        return false;
    }

    private static String systemAnnotations[] = {
        "Ldalvik/annotation/Throws;",
        "Ldalvik/annotation/EnclosingClass;",
        "Ldalvik/annotation/InnerClass;",
        "Ldalvik/annotation/MemberClasses;",
        "Ldalvik/annotation/EnclosingMethod;"
    };

    private DexTypeIdsBlock     dexTypeIdsBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexFieldIdsBlock    dexFieldIdsBlock = null;
    private DexMethodIdsBlock   dexMethodIdsBlock = null;
    private DexEncodedArrayParser   arrayParser = null;
    private DexSignatureBlock   dexSignatureBlock = null;

    private ArrayList<AnnotationHolder> fieldAnnotationHolders =
                    new ArrayList<AnnotationHolder>();
    private ArrayList<AnnotationHolder> methodAnnotationHolders =
                    new ArrayList<AnnotationHolder>();
    private ArrayList<AnnotationHolder> parameterAnnotationHolders =
                    new ArrayList<AnnotationHolder>();
    private ArrayList<AnnotationHolder> classAnnotationHolders = 
                    new ArrayList<AnnotationHolder>();

    private HashMap<String,Integer> fieldMap = new HashMap<String,Integer>();
    private HashMap<String,Integer> methodMap = new HashMap<String,Integer>();
    private HashMap<String,Integer> parameterMap = new HashMap<String,Integer>();

    private ArrayList<AnnotationHolder> getAnnotationList( AnnotationType type ) {
        ArrayList<AnnotationHolder> list = null;
        switch( type ) {
            case FIELD:
                list = fieldAnnotationHolders;
                break;

            case METHOD:
                list = methodAnnotationHolders;
                break;

            case PARAMETER:
                list = parameterAnnotationHolders;
                break;

            case CLASS:
                list = classAnnotationHolders;
                break;
        }
        return list;
    }

    private HashMap<String,Integer> getAnnotationMap( AnnotationType type ) {
        HashMap<String,Integer> map = null;
        switch( type ) {
            case FIELD:
                map = fieldMap;
                break;

            case METHOD:
                map = methodMap;
                break;

            case PARAMETER:
                map = parameterMap;
                break;
        }
        return map;
    }

    private AnnotationHolder readAnnotation( String tag ) throws IOException {
        AnnotationHolder annotationHolder
                             = new AnnotationHolder();
        return readAnnotation( annotationHolder,tag );
    }

    private AnnotationHolder readAnnotation( 
                AnnotationHolder annotationHolder,
                String tag ) throws IOException {
        annotationHolder.newAnnotation();
        int visibility = read8Bit();
        annotationHolder.setVisibility( visibility );
        dump( tag+": visibility: "+
                    visibilityString( visibility ) );

        return readEncodedAnnotation( annotationHolder, tag );
    }

    public AnnotationHolder readEncodedAnnotation(
                    AnnotationHolder annotationHolder,
                    String tag ) throws IOException {
        String type = 
                    dexTypeIdsBlock.getType( (int)readVLN() );
        annotationHolder.setType( type );
        dump( tag+": type: "+type );
        int annotationElements = (int)readVLN();
        for( int n = 0 ; n < annotationElements ; ++n ) {
            int elementNameIdx = (int)readVLN();
            String elementName = dexStringIdsBlock.getString( elementNameIdx );
            dump( tag+": element["+n+"], name: "+elementName );
            Object elementValue = arrayParser.readElement();
            dump( tag+": element["+n+"], value: "+elementValue );
            annotationHolder.addElement( elementName, elementValue );
        }
        return annotationHolder;
    }

    private String visibilityString( int visibilityFlag ) {
            String visibilityString = null;
            switch( visibilityFlag  ) {
                case VISIBILITY_BUILD:
                    visibilityString = "VISIBILITY_BUILD";
                    break;

                case VISIBILITY_RUNTIME:
                    visibilityString = "VISIBILITY_RUNTIME";
                    break;

                case VISIBILITY_SYSTEM:
                    visibilityString = "VISIBILITY_SYSTEM";
                    break;

                default:
                    visibilityString = "Unknown visibility (0x"+
                                        Integer.toHexString( visibilityFlag )+")";
                    break;
            }
            return visibilityString;
    }


}
