/**
  * Resolves offsets in optimized DEX files into method and field names
  */ 

package hu.uw.pallergabor.dedexer;
import java.util.*;
import java.io.*;

public class DexOffsetResolver {
    public void setDumpFile( PrintStream dump ) {
        this.dump = dump;
    }

/**
  * Adds classes of a DEX file to the offset resolver. At this stage the
  * offsets are not resolved, only the class names are associated with their
  * DexClassDefsBlocks.
  * @param dexClassDefsBlock class definitions to add to the class name table
  */
    public void addToOffsetResolver( DexClassDefsBlock dexClassDefsBlock ) {
        Iterator<Integer> classIterator = dexClassDefsBlock.getClassIterator();
        while( classIterator.hasNext() ) {
            Integer ci = classIterator.next();
            int classidx = ci.intValue();
            String className = dexClassDefsBlock.getClassNameOnly( classidx );
            DexOffsetDescriptor descriptor = 
                    new DexOffsetDescriptor( dexClassDefsBlock, classidx );
            descriptorCache.put( className,descriptor );
            if( dump != null ) {
                dump.println( "class added to offset resolver: "+className );
            }
            if( DEBUG_VTABLE )
                System.out.println( "class added to offset resolver: "+className );
        }
    }

/**
  * Obtains the method name for a vtable offset in a given class or null
  * if the offset cannot be resolved.
  */
    public String getMethodNameFromOffset( String className, int offset ) {
        DexOffsetDescriptor descriptor = resolveMethodOffsets( className );
        if( descriptor == null )
            return null;
        if( descriptor.methodOffsetToName == null )
            return null;
        VtableEntry entry = 
            descriptor.methodOffsetToName.get( new Integer( offset ) );
        return entry == null ?
                null :
                entry.toString();
    }

/**
  * Obtains the field name for a field offset in a given class or null
  * if the offset cannot be resolved.
  */
    public String getFieldNameFromOffset( String className, int offset ) {
        DexOffsetDescriptor descriptor = resolveFieldOffsets( className );
        if( descriptor == null )
            return null;
        if( descriptor.fieldOffsetToName == null )
            return null;
// Field of the superclass
        if( offset < descriptor.fieldStart ) {
            DexClassDefsBlock dexClassDefsBlock = descriptor.dexClassDefsBlock;
            int classIdx = descriptor.classIdx;
            String superClassName = dexClassDefsBlock.
                                    getSuperClass( classIdx );
            if( superClassName == null )
                return null;
            return getFieldNameFromOffset( superClassName,offset );
        }
        String fieldName = 
            descriptor.fieldOffsetToName.get( new Integer( offset ) );
        return fieldName;
    }

/**
  * Obtains a method,proto pair, separated by comma from an inline method index
  * @param index The index of the inline method
  * @return method name,proto separated by a comma
  */
    public static String getInlineMethodNameFromIndex( int index,
				DexSignatureBlock.OptVersion optVersion ) {
	String inlineMethodsForCurrentVersion[][] =
		optVersion == DexSignatureBlock.OptVersion.OPTVERSION_36 ? 			inlineMethods_36
		:
		inlineMethods_35;
        if( index >= inlineMethodsForCurrentVersion.length )
            return null;
        String clazz = inlineMethodsForCurrentVersion[ index ][ 0 ];
        String method = inlineMethodsForCurrentVersion[ index ][ 1 ];
        String signature = inlineMethodsForCurrentVersion[ index ][ 2 ];
        return clazz+"/"+method+","+method+signature;
    }


/**
  * Resolves the offsets in a class.
  * @return The DexOffsetDescriptor or null if the offsets cannot be resolved
  */
    public DexOffsetDescriptor resolveMethodOffsets( String inputClassName ) {
        String className = inputClassName;
        if( DEBUG_VTABLE )
            System.out.println( "resolveMethodOffsets/enter: "+className );
        if( className.startsWith( "[" ) )
            className = "java/lang/Object";
        DexOffsetDescriptor descriptor = descriptorCache.get( className );
// Descriptor table must be pre-initialized by addToOffsetResolver
        if( descriptor == null )
            return null;
// return the descriptor if the offsets have been resolved
        if( descriptor.methodOffsetToName != null )
            return descriptor;
        DexClassDefsBlock dexClassDefsBlock = descriptor.dexClassDefsBlock;
        DexMethodIdsBlock dexMethodIdsBlock = descriptor.dexMethodIdsBlock;
        int classIdx = descriptor.classIdx;
// first resolve the superclass (or obtain its descriptor from the cache)
        String superClassName = dexClassDefsBlock.
                                    getSuperClass( classIdx );
// If the class has superclass, resolve the offsets
        if( superClassName != null ) {
            if( DEBUG_VTABLE )
                System.out.println( "resolve superclass of "+className+
                            " : "+superClassName );
            DexOffsetDescriptor superClassDescriptor = 
                resolveMethodOffsets( superClassName );
            if( superClassDescriptor == null )
                return null;
// Copy all offsets from the superclass descriptor to this descriptor
            copySuperClassMethodOffsets( descriptor, superClassDescriptor );
        }
// Now take the own methods of the class and merge into the table inherited from
// the superclass
        if( descriptor.methodOffsetToName == null )
            descriptor.initMaps();
        int methodCount = descriptor.methodOffsetToName.size();
        for( int i = 0 ; 
            i < dexClassDefsBlock.getVirtualMethodsFieldsSize( classIdx ) ;
            ++i ) {
            int methodId = dexClassDefsBlock.getVirtualMethodId( classIdx, i );
            String methodName = dexMethodIdsBlock.getMethod( methodId );
            String methodProto = dexMethodIdsBlock.getProto( methodId );
            VtableEntry key = new VtableEntry( methodName, methodProto );
// not found in the superclass
            if( descriptor.methodNameToOffset.get( key ) == null ) {
                Integer value = new Integer( methodCount );
                if( DEBUG_VTABLE )
                    System.out.println( "Inserting "+key+" (offset: "+value+")" );
                descriptor.methodOffsetToName.put( value, key );
                descriptor.methodNameToOffset.put( key, value );
                ++methodCount;
            } else
            if( DEBUG_VTABLE )
                System.out.println( key+" was already in the table" );
        }
// Bizarre property of Java is that abstract classes don't have to define
// all the methods in the interfaces they implement (Dalvik source calls these
// "Miranda methods"). We have to allocate vtable entries for these methods. We 
// iterate over the interfaces the class implements and if we find any method in
// them that the vtable does not contain, we allocate a slot and insert them
        for( int i = 0 ; i < dexClassDefsBlock.getInterfacesSize( classIdx ) ; ++i ) {
            String implementedInterface = dexClassDefsBlock.getInterface( classIdx, i );
            if( DEBUG_VTABLE )
                System.out.println( 
                        "Starting to analyze: "+implementedInterface+
                        " implemented by "+className );
            DexOffsetDescriptor ifDescriptor = descriptorCache.get( implementedInterface );
            DexClassDefsBlock ifDefsBlock = ifDescriptor.dexClassDefsBlock;
            int ifIdx = ifDescriptor.classIdx;
            for( int n = 0 ; 
                    n < dexClassDefsBlock.getVirtualMethodsFieldsSize( ifIdx ) ;
                    ++n ) {
                int methodId = dexClassDefsBlock.getVirtualMethodId( ifIdx, n );
                String methodName = dexMethodIdsBlock.getMethod( methodId );
                String methodProto = dexMethodIdsBlock.getProto( methodId );
                VtableEntry key = new VtableEntry( methodName, methodProto );
// not found in the superclass
                if( descriptor.methodNameToOffset.get( key ) == null ) {
                    Integer value = new Integer( methodCount );
                    if( DEBUG_VTABLE )
                        System.out.println( 
                            "Inserting from interface "+
                            implementedInterface+
                            "; "+key+
                            " (offset: "+value+")" );
                    descriptor.methodOffsetToName.put( value, key );
                    descriptor.methodNameToOffset.put( key, value );
                    ++methodCount;
                } else
                if( DEBUG_VTABLE )
                    System.out.println( 
                        key+
                        " from "+
                        implementedInterface+
                        " was already in the table" );
            }
        }
        if( DEBUG_VTABLE )
            dumpMethodOffsets( System.out, descriptor );
        if( dump != null )
            dumpMethodOffsets( dump, descriptor );
        return descriptor;
    }

    public DexOffsetDescriptor resolveFieldOffsets( String className ) {
        if( DEBUG_OFFSETS )
            System.out.println( "resolveFieldOffsets,enter: "+className );
        DexOffsetDescriptor descriptor = descriptorCache.get( className );
// Descriptor table must be pre-initialized by addToOffsetResolver
        if( descriptor == null )
            return null;
        if( descriptor.fieldOffsetToName != null )
            return descriptor;
        DexClassDefsBlock dexClassDefsBlock = descriptor.dexClassDefsBlock;
        int classIdx = descriptor.classIdx;
// Resolve the superclass fields because we need them for the initial
// offset
        String superClassName = dexClassDefsBlock.
                                    getSuperClass( classIdx );
// If the class has superclass, resolve the offsets
        if( superClassName != null ) {
            if( DEBUG_OFFSETS )
                System.out.println( "resolving superclass: "+superClassName );
            DexOffsetDescriptor superClassDescriptor = 
                resolveFieldOffsets( superClassName );
            if( superClassDescriptor == null )
                return null;
// fieldEnd is also initialized in case the child class does not have fields
            descriptor.fieldEnd = 
                descriptor.fieldStart = 
                    superClassDescriptor.fieldEnd;
            if( DEBUG_OFFSETS )
                System.out.println( 
                    "initializing fieldStart from the superclass field end: "+
                    descriptor.fieldStart );
            if( DEBUG_OFFSETS )
                System.out.println( "Continue processing "+className );
        }

// member fields of the class have not been resolved to offsets, resolve
// them.
        int instanceFieldSize = dexClassDefsBlock.getInstanceFieldsSize( classIdx );
        if( DEBUG_OFFSETS )
            System.out.println( "resolveFieldOffsets: instanceFieldSize: "+
                instanceFieldSize );
        if( instanceFieldSize == 0 )
            return descriptor;
        ArrayList<String> fieldList = new ArrayList<String>();
        for( int i = 0 ; 
            i < instanceFieldSize ;
            ++i ) {
            String fieldNameAndType = dexClassDefsBlock.getInstanceFieldNameAndType( classIdx,i );
            if( DEBUG_OFFSETS )
                System.out.println( "resolveFieldOffset/initial: "+i+"; "+fieldNameAndType );
            fieldList.add( fieldNameAndType );
        }
// The following algorithm is a replica of the offset resolver in Dalvik
// First let's move the references to the head of the list using this 
// quicksort-like algorithm
        int referenceCount = 0;
        int j = instanceFieldSize - 1;
        int i;
        for( i = 0 ; i < fieldList.size() ; ++i ) {
            boolean wasReference = false;
            String fieldName = fieldList.get( i );
            String type = getFieldType( fieldName );
            if( DEBUG_OFFSETS )
                System.out.println( "references:"+i+"; fieldName: "+fieldName+"; type: "+type );
            if( type == null )  // can't happen
                continue;
            if( !type.startsWith( "[" ) &&
                !type.startsWith( "L" ) ) {
/* This isn't a reference field; see if any reference fields
 * follow this one.  If so, we'll move it to this position.
 * (quicksort-style partitioning)
 */
                while( j > i ) {
                    String fieldName2 = fieldList.get( j );
                    String type2 = getFieldType( fieldName2 );
                    if( DEBUG_OFFSETS )
                        System.out.println( "references/inner:"+j+"; fieldName: "+fieldName2+"; type: "+type2 );
                    if( type2 == null )
                        continue;
                    if( type2.startsWith( "[" ) ||
                        type2.startsWith( "L" ) ) {
/* Here's a reference field that follows at least one
 * non-reference field.  Swap it with the current field.
 * (When this returns, "pField" points to the reference
 * field, and "refField" points to the non-ref field.)
 */
                        fieldList.set( i,fieldName2 );
                        fieldList.set( j,fieldName );
                        wasReference = true;
                        --j;
                        break;
                    }
                    --j;
                }
            } else
                wasReference = true;
            if( !wasReference )
                break;
            ++referenceCount;
        }
// If there was odd number of references, we can insert a non-double field
// here before the doubles.
// Note that if the field area of this object starts at offset which is
// non double-word aligned (fields of the superclasses end at 
// non-doubleword-aligned offset), the "odd number of references" flag is
// negated
        if( DEBUG_OFFSETS )
            System.out.println( "Before padding swap: referenceCount: "+
                    referenceCount+
                    "; i: "+i+
                    "; fieldList: "+fieldList );
        boolean oddRefs = ( referenceCount & 1 ) != 0;
        if( ( descriptor.fieldStart % 8 ) != 0 )
            oddRefs = !oddRefs;
        if( DEBUG_OFFSETS )
            System.out.println( "oddRefs: "+oddRefs );
        if( oddRefs && 
              ( i < fieldList.size() ) ) {
            String fieldName = fieldList.get( i );
            String type = getFieldType( fieldName );
            if( !type.startsWith( "J" ) &&
                !type.startsWith( "D" ) ) {
                ++i;    // just skip it
                if( DEBUG_OFFSETS )
                    System.out.println( "Skipped field: "+fieldName );
            }
            else {
// else this double at non-aligned position have to be swapped with a non-double.
// if it is unsuccessful (there are no non-doubles), leave it like it is, the
// offset assignment code will insert a pad before the double
                j = instanceFieldSize - 1;
                if( DEBUG_OFFSETS )
                    System.out.println( "Padding swap: fieldName: "+fieldName );
                while( j > i ) {
                    String fieldName2 = fieldList.get( j );
                    String type2 = getFieldType( fieldName2 );
                    if( DEBUG_OFFSETS )
                        System.out.println( 
                            "Padding swap: fieldName2: "+fieldName2+
                                "; type2: "+type2 );
                    if( !type2.startsWith( "J" ) &&
                        !type2.startsWith( "D" ) ) {
// we found the non-double to swap with
                        fieldList.set( i,fieldName2 );
                        fieldList.set( j,fieldName );
                        ++i;    // don't swap this non-double out when arranging doubles
                        break;
                    }
                    --j;
                }
            }
        }
// Now pack the double fields with a similar algorithm
        if( DEBUG_OFFSETS )
            System.out.println( "Before double swap: ; i: "+i+
                    "; fieldList: "+fieldList );
        j = instanceFieldSize - 1;
        for( ; i < fieldList.size() ; ++i ) {
            boolean wasDouble = false;
            String fieldName = fieldList.get( i );
            String type = getFieldType( fieldName );
            if( DEBUG_OFFSETS )
                System.out.println( "doubles: "+i+"; fieldName: "+fieldName+"; type: "+type );
            if( type == null )  // can't happen
                continue;
            if( !type.startsWith( "J" ) &&
                !type.startsWith( "D" ) ) {
                while( j > i ) {
                    String fieldName2 = fieldList.get( j );
                    String type2 = getFieldType( fieldName2 );
                    if( DEBUG_OFFSETS )
                        System.out.println( "doubles/inner:"+j+"; fieldName: "+fieldName2+"; type: "+type2 );
                    if( type2 == null )
                        continue;
                    if( type2.startsWith( "J" ) ||
                        type2.startsWith( "D" ) ) {
                        fieldList.set( i,fieldName2 );
                        fieldList.set( j,fieldName );
                        --j;
                        wasDouble = true;
                        break;
                    }
                    --j;
                }
            } else
                wasDouble = true;
            if( !wasDouble )
                break;
        }
// At this point, we have the field list in fieldList. Assign offsets and set up
// fieldOffsetToName table
        boolean doubleEncountered = false;
        int offset = descriptor.fieldStart;
        descriptor.fieldOffsetToName = new HashMap<Integer,String>();
        for( i = 0 ; i < fieldList.size() ; ++i ) {
            String fieldName = fieldList.get( i );
            if( DEBUG_OFFSETS )
                System.out.println( i+"; fieldName: "+fieldName );
            String type = getFieldType( fieldName );
            int fieldOffset = offset;
            switch( type.charAt( 0 ) ) {
                case 'J':
                case 'D':
// If this is the first double value in the list, align offset to 64-bit boundary
                    if( !doubleEncountered ) {
                        doubleEncountered = true;
                        offset = ( offset + 7 ) & 0xFFF8;
                        fieldOffset = offset;
                    }
                    offset += 8;
                    break;

                default:
                    offset += 4;
                    break;
            }
            Integer fieldOffsetObject = new Integer( fieldOffset );
            descriptor.fieldOffsetToName.put( fieldOffsetObject, fieldName );
        }
        descriptor.fieldEnd = offset;
        if( DEBUG_OFFSETS )
            dumpFieldOffsets( System.out, descriptor );
        if( dump != null )
            dumpFieldOffsets( dump, descriptor );
        if( DEBUG_OFFSETS )
            System.out.println( "resolveFieldOffsets: "+className+
                            "; fieldStart: "+descriptor.fieldStart+
                            "; fieldEnd: "+descriptor.fieldEnd );
        return descriptor;
    }

/**
  * Finds the youngest common ancestor of clazz1 and clazz2 and returns
  * it.
  * @param clazz1 The first class
  * @param clazz2 The second class
  * @return Class name of the youngest common ancestor
  */
    public String findCommonAncestor( String clazz1, String clazz2 ) {
        String bare1 = cutToClassName( clazz1 );
        String bare2 = cutToClassName( clazz2 );
        DexOffsetDescriptor descriptor1 = descriptorCache.get( bare1 );
        DexOffsetDescriptor descriptor2 = descriptorCache.get( bare2 );
        if( ( descriptor1 == null ) ||
            ( descriptor2 == null ) )
            return "java/lang/Object";
        if( descriptor1.ancestors == null ) {
            descriptor1.ancestors = createAncestorList( bare1, descriptor1 );
        }
        if( descriptor2.ancestors == null ) {
            descriptor2.ancestors = createAncestorList( bare2, descriptor2 );
        }
// Find the longest common prefix of the ancestor path of the two classes. Note that
// because of the way createAncestorList is implemented, the oldest ancestors
// are at the end of the lists.
        int idx1 = descriptor1.ancestors.size() -1;
        int idx2 = descriptor2.ancestors.size() -1;
        while( ( idx1 >= 0 ) && ( idx2 >= 0 ) ) {
            String ancestor1 = descriptor1.ancestors.get( idx1 );
            String ancestor2 = descriptor2.ancestors.get( idx2 );
            if( !ancestor1.equals( ancestor2 ) )
                break;
            --idx1;
            --idx2;
        }
        if( idx1 < 0 )
            return bare1;
        if( idx2 < 0 )
            return bare2;
        ++idx1;     // that's were the youngest common ancestor is
        if( idx1 >= descriptor1.ancestors.size() )  // hiccup: at least java.lang.Object should be common
            return "java/lang/Object";
        return descriptor1.ancestors.get( idx1 );
    }

// Cache for classes with resolved offsets
    HashMap<String,DexOffsetDescriptor> descriptorCache =
            new HashMap<String,DexOffsetDescriptor>();
    PrintStream dump;

    private static String inlineMethods_35[][] =
    { { "Lorg/apache/harmony/dalvik/NativeTestTarget" ,
        "emptyInlineMethod",
        "()V" },    /* 0 */
      { "Ljava/lang/String",
        "charAt",
        "(I)C" },   /* 1 */
      { "Ljava/lang/String",
        "compareTo",
        "(Ljava/lang/String;)I" },  /* 2 */
      { "Ljava/lang/String",
        "equals",
        "(Ljava/lang/Object;)Z" },  /* 3 */
      { "Ljava/lang/String",
        "length",
        "()I" },                    /* 4 */
      { "Ljava/lang/Math",
        "abs",
        "(I)I" },                   /* 5 */
      { "Ljava/lang/Math",
        "abs",
        "(J)J" },                   /* 6 */
      { "Ljava/lang/Math",
        "abs",
        "(F)F" },                   /* 7 */
      { "Ljava/lang/Math",
        "abs",
        "(D)D" },                   /* 8 */
      { "Ljava/lang/Math",
        "min",
        "(II)I" },                  /* 9 */
      { "Ljava/lang/Math",
        "max",
        "(II)I" },                  /* 10 */
      { "Ljava/lang/Math",
        "sqrt",
        "(D)D" },                   /* 11 */
      { "Ljava/lang/Math",
        "cos",
        "(D)D" },                   /* 12 */
      { "Ljava/lang/Math",
        "sin",
        "(D)D" }                    /* 13 */
    };

    private static String inlineMethods_36[][] =
    { { "Lorg/apache/harmony/dalvik/NativeTestTarget" ,
        "emptyInlineMethod",
        "()V" },    /* 0 */
      { "Ljava/lang/String",
        "charAt",
        "(I)C" },   /* 1 */
      { "Ljava/lang/String",
        "compareTo",
        "(Ljava/lang/String;)I" },  /* 2 */
      { "Ljava/lang/String",
        "equals",
        "(Ljava/lang/Object;)Z" },  /* 3 */
      { "Ljava/lang/String",
        "fastIndexOf",
        "(II)I" },  		/* 4 */
      { "Ljava/lang/String",
        "isEmpty",
        "()Z" },  		/* 5 */
      { "Ljava/lang/String",
        "length",
        "()I" },                    /* 6 */
      { "Ljava/lang/Math",
        "abs",
        "(I)I" },                   /* 7 */
      { "Ljava/lang/Math",
        "abs",
        "(J)J" },                   /* 8 */
      { "Ljava/lang/Math",
        "abs",
        "(F)F" },                   /* 9 */
      { "Ljava/lang/Math",
        "abs",
        "(D)D" },                   /* 10 */
      { "Ljava/lang/Math",
        "min",
        "(II)I" },                  /* 11 */
      { "Ljava/lang/Math",
        "max",
        "(II)I" },                  /* 12 */
      { "Ljava/lang/Math",
        "sqrt",
        "(D)D" },                   /* 13 */
      { "Ljava/lang/Math",
        "cos",
        "(D)D" },                   /* 14 */
      { "Ljava/lang/Math",
        "sin",
        "(D)D" },                    /* 15 */
      { "Ljava/lang/Float",
        "floatToIntBits",
        "(F)I" },                    /* 16 */
      { "Ljava/lang/Float",
        "floatToRawIntBits",
        "(F)I" },                    /* 17 */
      { "Ljava/lang/Float",
        "intBitsToFloat",
        "(I)F" },                    /* 18 */
      { "Ljava/lang/Double",
        "doubleToLongBits",
        "(D)J" },                    /* 19 */
      { "Ljava/lang/Double",
        "doubleToRawLongBits",
        "(D)J" },                    /* 20 */
      { "Ljava/lang/Double",
        "longBitsToDouble",
        "(J)D" }                    /* 21 */
    };

    private static final boolean DEBUG_VTABLE = false;
    private static final boolean DEBUG_OFFSETS = false;

// Creates the ancestor list of a class. Note that the oldest ancestor is
// at the end of the list.
    private ArrayList<String> createAncestorList( 
                String clazz, DexOffsetDescriptor descriptor ) {
        ArrayList<String> ancestors = new ArrayList<String>();
        DexClassDefsBlock dexClassDefsBlock = descriptor.dexClassDefsBlock;
        DexOffsetDescriptor currentDescriptor = descriptor;
        ancestors.add( clazz );
        while( dexClassDefsBlock != null ) {
            String ancestorName = 
                    dexClassDefsBlock.getSuperClass( currentDescriptor.classIdx );
            if( ancestorName == null )
                break;
            ancestors.add( ancestorName );
            if( ancestorName.equals( "java/lang/Object" ) )
                break;
            currentDescriptor = descriptorCache.get( ancestorName );
        }
        return ancestors;
    }

    private String cutToClassName( String clazz ) {
        String bare = clazz;
        if( bare.startsWith( "L" ) )
            bare = bare.substring( 1 );
        if( bare.endsWith( ";" ) )
            bare = bare.substring( 0, bare.length() - 1 );
        return bare;
    }

    private void copySuperClassMethodOffsets( 
                DexOffsetDescriptor descriptor, 
                DexOffsetDescriptor superClassDescriptor ) {
        if( descriptor.methodOffsetToName == null )
            descriptor.initMaps();
        String targetClassName = 
                descriptor.
                    dexClassDefsBlock.
                        getClassNameOnly( descriptor.classIdx );
        if( DEBUG_VTABLE )
            System.out.println( "copySuperClassMethodOffsets: source: "+
                superClassDescriptor.
                    dexClassDefsBlock.
                        getClassNameOnly( superClassDescriptor.classIdx )+
                " ; destination: "+targetClassName );
// Iterate the methodOffsetToName map and copy it to the child class descriptor
        for( Iterator<Integer> it = superClassDescriptor.methodOffsetToName.keySet().iterator() ;
                it.hasNext() ; ) {
            Integer offset = it.next();
            VtableEntry entry = superClassDescriptor.methodOffsetToName.get( offset );
            VtableEntry newEntry = new VtableEntry( 
                targetClassName+"/"+entry.getMethodNameOnly(),
                entry.getProto() );
            if( DEBUG_VTABLE )
                System.out.println( "Copying "+newEntry+" (offset: "+offset+")" );
            descriptor.methodOffsetToName.put( offset,newEntry );
            descriptor.methodNameToOffset.put( newEntry, offset );
        }
    }

    private void dumpMethodOffsets( 
            PrintStream dump, 
            DexOffsetDescriptor descriptor ) {
        dump.println();
        dump.println( "Method offset resolver: class "+
                    descriptor.dexClassDefsBlock.getClassNameOnly( descriptor.classIdx ) );
        if( descriptor.methodNameToOffset != null ) {
            for( Iterator<VtableEntry> it = descriptor.methodNameToOffset.keySet().iterator() ;
                it.hasNext() ; ) {
                VtableEntry entry = it.next();
                Integer offset = descriptor.methodNameToOffset.get( entry );
                dump.println( "method key: "+entry+
                            " ; offset: "+offset );
            }
        }
        dump.println();
    }

    private void dumpFieldOffsets( 
            PrintStream dump, 
            DexOffsetDescriptor descriptor ) {
        dump.println();
        dump.println( "Field offset resolver: class "+
                    descriptor.dexClassDefsBlock.getClassName( descriptor.classIdx ) );
        if( descriptor.fieldOffsetToName != null ) {
            for( Iterator<Integer> it = descriptor.fieldOffsetToName.keySet().iterator() ;
                it.hasNext() ; ) {
                Integer offset = it.next();
                String fieldName = descriptor.fieldOffsetToName.get( offset );
                dump.println( "field offset: "+offset+
                            " ; field: "+fieldName );
            }
        }
        dump.println();
    }


    private String getFieldType( String fieldName ) {
        int idx = fieldName.indexOf( ' ' );
        return idx < 0 ?
            null
            : fieldName.substring( idx+1 );
    }

// This class represents a loaded class in the class set.
// Originally it stores only the DexClassDefsBlock (essentially DEX file)
// that knows about the class. If the offsets in the class are resolved, this 
// instance stores method/field offsets too.
    class DexOffsetDescriptor {
        public DexOffsetDescriptor( DexClassDefsBlock dexClassDefsBlock,
                    int classIdx ) {
            this.dexClassDefsBlock = dexClassDefsBlock;
            dexMethodIdsBlock = dexClassDefsBlock.getDexMethodIdsBlock();
            this.classIdx = classIdx;
        }

        public void initMaps() {
            methodOffsetToName = new HashMap<Integer,VtableEntry>();
            methodNameToOffset = new HashMap<VtableEntry,Integer>();
        }

        public DexClassDefsBlock    dexClassDefsBlock;
        public DexMethodIdsBlock    dexMethodIdsBlock;
        public int classIdx;
        public HashMap<Integer,VtableEntry> methodOffsetToName = null;
        public HashMap<VtableEntry,Integer> methodNameToOffset = null;
        public HashMap<Integer,String> fieldOffsetToName = null;
        public ArrayList<String> ancestors;
// Check out vm/oo/Object.h, DataObject
        int fieldStart = 8;
        int fieldEnd = 8;
    }

    class VtableEntry {
        public VtableEntry( String methodName, String proto ) {
            this.methodName = methodName;
            this.proto = proto;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getMethodNameOnly() {
            int idx = methodName.lastIndexOf( '/' );
            if( idx < 0 )
                return methodName;
            return methodName.substring( idx+1 );
        }

        public String getProto() {
            return proto;
        }

        public int hashCode() {
            return proto.hashCode();
        }

        public boolean equals( Object otherObject ) {
            if( !( otherObject instanceof VtableEntry ) )
                return false;
            VtableEntry otherEntry = (VtableEntry)otherObject;
            return proto.equals( otherEntry.proto );
        }

        public String toString() {
            return methodName+","+proto;
        }

        String methodName;
        String proto;
    }
}
