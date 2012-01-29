/**
  * Code generator for Jasmin-style DEX code
  */ 

package hu.uw.pallergabor.dedexer;
import java.util.*;
import java.io.*;

public class JasminStyleCodeGenerator implements CodeGenerator {
    public void generate() throws IOException, UnknownInstructionException {
        Iterator<Integer> classIterator = dexClassDefsBlock.getClassIterator();
        while( classIterator.hasNext() ) {
            Integer ci = classIterator.next();
            int classidx = ci.intValue();
            String className = dexClassDefsBlock.getClassNameOnly( classidx );
            System.out.println( "Processing "+className );
            if( dump != null ) {
                dump.println( "--------------------------------------" );
                dump.println( "Class: "+className );
            }
            File targetFile = new File( 
                                generatedSourceBaseDir+
                                "/"+
                                className+
                                ".ddx" );
            File parent = targetFile.getParentFile();
            if( parent != null )
                    parent.mkdirs();
            PrintStream ps = new PrintStream( targetFile );
            currentOutput = ps;
            if( dexClassDefsBlock.isInterface( classidx ) )
                ps.println( ".interface "+dexClassDefsBlock.getClassName( classidx ) );
            else
                ps.println( ".class "+dexClassDefsBlock.getClassName( classidx ) );
            String superClass = dexClassDefsBlock.getSuperClass( classidx );
            if( superClass != null )
                ps.println( ".super "+dexClassDefsBlock.getSuperClass( classidx ) );
            if( dexClassDefsBlock.getSourceName( classidx ) != null )
                ps.println( ".source "+dexClassDefsBlock.getSourceName( classidx ) );
            for( int i = 0 ; 
                    i < dexClassDefsBlock.getInterfacesSize( classidx ) ;
                    ++i )
                ps.println( ".implements "+
                    dexClassDefsBlock.getInterface( classidx, i ) );
            ps.println();
// Class annotations
            if( dexClassDefsBlock.getDexAnnotationParser( classidx ) != null )
                generateClassAnnotations( ps, classidx );
// Generate the fields (static and instance
            for( int i = 0 ; i < dexClassDefsBlock.getStaticFieldsSize( classidx ) ; ++i ) {
				Object initializer = dexClassDefsBlock.getStaticFieldInitializer( classidx,i );
				String initializerString = "";
				if( initializer != null ) {
					if( initializer instanceof Integer ) {
						Integer iv = (Integer)initializer;
						initializerString = " = "+iv.toString()+"\t; 0x"+
											Integer.toHexString( iv.intValue() );
					} else
					if( initializer instanceof Long ) {
						Long lv = (Long)initializer;
						initializerString = " = "+lv.toString()+"\t; 0x"+
											Long.toHexString( lv.longValue() );
					} else
						initializerString = " = "+initializer.toString();
				}
                ps.println( ".field "+
					dexClassDefsBlock.getStaticField( classidx, i )+
					initializerString );
                String shortFieldName = 
                    dexClassDefsBlock.getStaticFieldShortName( classidx, i );
                addFieldAnnotation( ps, shortFieldName, classidx, i );
			}
            for( int i = 0 ; i < dexClassDefsBlock.getInstanceFieldsSize( classidx ) ; ++i ) {
                ps.println( ".field "+dexClassDefsBlock.getInstanceField( classidx, i ) );
                String shortFieldName = dexClassDefsBlock.getInstanceFieldShortName( classidx, i );
                addFieldAnnotation( ps, shortFieldName, classidx, i );
            }
            ps.println();
// Generate the methods (direct and virtual)
            for( int i = 0 ; i < dexClassDefsBlock.getDirectMethodsFieldsSize( classidx ) ; ++i ) {
                String methodName = dexClassDefsBlock.getDirectMethodName( classidx, i );
                ps.println( ".method "+methodName );
                dumpMethodName( methodName );
                addMethodAnnotation( ps, 
                            dexClassDefsBlock.getDirectMethodShortName( classidx, i ) , 
                            classidx, 
                            i );
                if( ( dexClassDefsBlock.getDirectMethodAccess( classidx, i ) &
                        ( DexClassDefsBlock.ACCESS_ABSTRACT | DexClassDefsBlock.ACCESS_NATIVE ) ) 
                        == 0 )
                    generateMethodBody(
                        methodName, 
                        dexClassDefsBlock.getDirectMethodOffset( classidx, i ),
                        ps,
                        classidx,
                        i,
                        true );
                ps.println( ".end method" );
                ps.println();
            }
            for( int i = 0 ; i < dexClassDefsBlock.getVirtualMethodsFieldsSize( classidx ) ; ++i ) {
                String methodName = dexClassDefsBlock.getVirtualMethodName( classidx, i );
                ps.println( ".method "+methodName );
                addMethodAnnotation( ps,
                                dexClassDefsBlock.getVirtualMethodShortName( classidx, i ), 
                                classidx,
                                i );
                dumpMethodName( methodName );
                int access = dexClassDefsBlock.getVirtualMethodAccess( classidx, i );
                if( ( access & 
                        ( DexClassDefsBlock.ACCESS_ABSTRACT | DexClassDefsBlock.ACCESS_NATIVE ) ) == 0 )
                    generateMethodBody( 
                        methodName,
                        dexClassDefsBlock.getVirtualMethodOffset( classidx, i ),
                        ps,
                        classidx,
                        i,
                        false );
                ps.println( ".end method" );
                ps.println();
            }
            ps.println();
            currentOutput = null;
            ps.close();
        }
    }

    public void setDexSignatureBlock( DexSignatureBlock dexSignatureBlock ) {
        this.dexSignatureBlock = dexSignatureBlock;
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

    public void setDexClassDefsBlock( DexClassDefsBlock dexClassDefsBlock ) {
        this.dexClassDefsBlock = dexClassDefsBlock;
    }

    public void setDexOffsetResolver( DexOffsetResolver dexOffsetResolver ) {
        this.dexOffsetResolver = dexOffsetResolver;
    }

    public void setGeneratedSourceBaseDir( String generatedSourceBaseDir ) {
        this.generatedSourceBaseDir = generatedSourceBaseDir;
    }

    public void setRandomAccessFile( RandomAccessFile file ) {
        this.file = file;
    }

    public void setDumpFile( PrintStream dump ) {
        this.dump = dump;
    }

    public void setRegTracing( boolean regTracing ) {
        this.regTracing = regTracing;
    }

    public void setRegTraceLog( boolean regTraceLog ) {
        this.regTraceLog = regTraceLog;
    }

// CodeGenerator

    public void renderLabel( String label ) throws IOException {
        currentOutput.println( label+":" );
    }

    public void renderLineNumber( int lineNumber ) throws IOException {
        currentOutput.println( ".line "+Integer.toString( lineNumber ) );
    }

    public void renderPackedSwitch( 
        int reg, int low, String defaultLabelName, ArrayList<String> labels )
                    throws IOException {
        currentOutput.println( "\tpacked-switch\tv"+reg+","+low );
        int labelCtr = low;
        for( int i = 0 ; i < labels.size() ; ++i,++labelCtr )
            currentOutput.println( "\t\t"+labels.get( i )+"\t; case "+labelCtr );
        currentOutput.println( "\t\tdefault: "+defaultLabelName );
    }

    public void renderSparseSwitch( 
        int reg, String defaultLabelName,
        String switchKeys[], String switchValues[] ) throws IOException {
        currentOutput.println( "\tsparse-switch\tv"+reg );
        for( int i = 0 ; i < switchKeys.length ; ++i )
            currentOutput.println( "\t\t"+switchKeys[i]+" : "+switchValues[i] );
        currentOutput.println( "\t\tdefault: "+defaultLabelName );
    }

    public void openDataArray( String label ) throws IOException {
        currentOutput.println( label+":\tdata-array" );
    }

    public void writeElement( long elementIdx, String element ) throws IOException {
        currentOutput.println( "\t\t"+element+"\t; #"+elementIdx );
    }

    public void writeByteArray( String element ) throws IOException {
        currentOutput.println( "\t\t"+element );
    }

    public void closeDataArray( String label ) throws IOException {
        currentOutput.println( "\tend data-array" );
    }

    public void writeTryCatchBlock( String startLabel, 
                                String endLabel, 
                                String exception,
                                String handlerLabel ) throws IOException {
        currentOutput.println( ".catch "+
                            exception+
                            " from "+
                            startLabel+
                            " to "+
                            endLabel+
                            " using "+
                            handlerLabel );
    }

    public void writeLocalVariable( int regNum,
                                String variableName,
                                String variableType,
                                String startOffsetLabel,
                                String endOffsetLabel ) {
        currentOutput.println( ".var "+
                            regNum+
                            " is "+
                            variableName+
                            " "+
                            variableType+
                            " from "+
                            startOffsetLabel+
                            " to "+
                            endOffsetLabel );
    }

    public void writeClassAnnotation( int classIdx, 
                                    int visibility, 
                                    String type, 
                                    ArrayList<String> parmNames, 
                                    ArrayList<Object> parmValues ) {
        currentOutput.println( ".annotation "+
                                getVisibility( visibility )+
                                " "+
                                type );
        for( int i = 0 ; i < parmNames.size() ; ++i ) {
            Object o = parmValues.get( i );
            currentOutput.println( "  "+
                                parmNames.get( i ) +
                                " "+
                                DexEncodedArrayParser.getTypeString( o )+
                                " = "+
                                o.toString() );
        }
        currentOutput.println( ".end annotation" );
        currentOutput.println();
    }


    private DexSignatureBlock   dexSignatureBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexTypeIdsBlock     dexTypeIdsBlock = null;
    private DexFieldIdsBlock    dexFieldIdsBlock = null;
    private DexMethodIdsBlock   dexMethodIdsBlock = null;
    private DexClassDefsBlock   dexClassDefsBlock = null;
    private DexOffsetResolver   dexOffsetResolver = null;
    private String              generatedSourceBaseDir = null;
    private RandomAccessFile    file;
    private PrintStream         currentOutput;
    private PrintStream         dump;
    private boolean             regTraceLog = false;
    private boolean             regTracing = false;
    private static final boolean DEBUG_EXCP = false;
    private static final boolean DEBUG_REGMAPS = false;
    private static final boolean DEBUG_REGTRACE = false;
    private static final boolean DEBUG_MERGE = false;
    private static final boolean DEBUG_FLOW = false;
	private static final int 	REVISIT_LIMIT = 100;

    private void generateMethodBody( String methodName,
                                    long pos, 
                                    PrintStream ps, 
                                    int classidx, 
                                    int methodidx, 
                                    boolean direct ) 
                        throws IOException, UnknownInstructionException {
        if( DEBUG_FLOW )
            System.out.println( "Flow: method "+methodName );
        DexMethodHeadParser dexMethodHeadParser = new DexMethodHeadParser();
        dexMethodHeadParser.setRandomAccessFile( file );
        dexMethodHeadParser.setDexSignatureBlock( dexSignatureBlock );
        dexMethodHeadParser.setDumpFile( dump );
        dexMethodHeadParser.parse( pos );
        int regSize = dexMethodHeadParser.getRegistersSize();
        ps.println( ".limit registers "+ regSize );
        ArrayList parmRegs = 
                direct 
                    ?
                dexClassDefsBlock.getDirectMethodParameterOffsets( classidx, methodidx, regSize ) 
                    :
                dexClassDefsBlock.getVirtualMethodParameterOffsets( classidx, methodidx, regSize );
        int accessFlags =
                direct 
                    ?
                dexClassDefsBlock.getDirectMethodAccess( classidx, methodidx )
                    :
                dexClassDefsBlock.getVirtualMethodAccess( classidx, methodidx );
        HashMap<Integer,String> initRegMap = new HashMap<Integer,String>();
        if( ( accessFlags & DexClassDefsBlock.ACCESS_STATIC ) == 0 ) {
            int thisReg = 0;
            if( parmRegs.size() < 2 )   // no parameters - "this" is in the last register
                thisReg = regSize - 1;
            else
                thisReg = ((Integer)parmRegs.get( 0 )).intValue() - 1;
            String type = "L"+dexClassDefsBlock.getClassNameOnly( classidx )+";";
            ps.println( "; this: v"+thisReg+" ("+type+")" );
            initRegMap.put( new Integer( thisReg ),type );
        }
        int parmctr = 0;
        for( int i = 0 ; i < parmRegs.size() ; i += 2,++parmctr ) {
            ps.println( "; parameter["+parmctr+"] : v"+
                        ((Integer)parmRegs.get( i ))+
                        " ("+
                        ((String)parmRegs.get( i+1 ))+
                        ")" );
            initRegMap.put( (Integer)parmRegs.get( i ),
                        DexInstructionParser.convertJavaTypeToInternal( 
                            (String)parmRegs.get( i+1 ) ) );
        }
        long startPos = dexMethodHeadParser.getInstructionBase();
        long endPos = dexMethodHeadParser.getInstructionEnd();
        DexInstructionParser instructionParser = new DexInstructionParser();
	instructionParser.setDexSignatureBlock( dexSignatureBlock ); 
        instructionParser.setDexStringIdsBlock( dexStringIdsBlock );
        instructionParser.setDexTypeIdsBlock( dexTypeIdsBlock );
        instructionParser.setDexFieldIdsBlock( dexFieldIdsBlock );
        instructionParser.setDexMethodIdsBlock( dexMethodIdsBlock );
        instructionParser.setDexOffsetResolver( dexOffsetResolver );
        instructionParser.setCodeGenerator( this );
        instructionParser.setDumpFile( dump );
        instructionParser.setRandomAccessFile( file );
        instructionParser.setDumpOff();

// First pass: discover just the labels and the code areas. The trace disassembler
// simulates the instruction flow and discovers code/data areas.

// Each bit represents the starting offset of an instruction in the
// method body. 
        BitSet visitSet = new BitSet( (int)( endPos - startPos ) );

// Branches in the execution flow are stored in this stack
        Stack<VisitStackEntry> visitStack = 
                new Stack<VisitStackEntry>();

// This map stores reg trace strings (suitable for displaying to the user)
// per locations (-r flag)
        HashMap<Long,String> regTraceMap = null;
// This map stores the exception block start addresses and the associated exception 
// handlers
        ArrayList<ExceptionHandlerMapEntry> exceptionHandlerEntryPointList = null;
// This vector stores the currently active debug register traces
        ArrayList<RegisterTraces> regTraces = null;
// This map stores the saved register maps for distinguished locations.
// Targets of jump instructions are such locations.
        HashMap<Long,HashMap<Integer,String>> registerMaps = null;
// This map stores the counter, how many times a certain distinguished location was visited.
// This protects about endless loops when the regmap solution does not converge
		HashMap<Long,Integer> overrunCounter = null;
        if( regTraceLog )
            regTraceMap = new HashMap<Long,String>();
        if( regTracing ) {
            exceptionHandlerEntryPointList = new ArrayList<ExceptionHandlerMapEntry>();
            regTraces = new ArrayList<RegisterTraces>();
            registerMaps = new HashMap<Long,HashMap<Integer,String>>();
			overrunCounter = new HashMap<Long,Integer>();
        }

// Process the try-catch blocks if any. Pushes any exception handlers to the visit stack
        if( DEBUG_FLOW )
            System.out.println( "Flow: about to process try-catch blocks" );

        if( dexMethodHeadParser.getTriesSize() != 0 )
            processTryCatchBlock( methodName,
                                    instructionParser, 
                                    dexMethodHeadParser, 
                                    visitStack, 
                                    initRegMap,
                                    exceptionHandlerEntryPointList );
        DexDebugInfoParser debugInfoParser = null;
        if( DEBUG_FLOW )
            System.out.println( "Flow: about to initialize reg trace" );
        if( dexMethodHeadParser.getDebugOffset() != 0L ) {
            debugInfoParser = parseDebugInfoBlock( 
                instructionParser, 
                dexMethodHeadParser );
            if( regTraces != null ) {
                initializeRegTrace( regTraces, 
                                    debugInfoParser,
                                    dexMethodHeadParser );
                instructionParser.setRegTraces( regTraces );
            }
        }
        instructionParser.setFilePosition( dexMethodHeadParser.getInstructionBase() );

        instructionParser.setPass( false );
        instructionParser.setRegisterMap( (HashMap<Integer,String>)initRegMap.clone() );
        do {
            long filePos = instructionParser.getFilePosition();
            if( DEBUG_FLOW )
                System.out.println( 
                        "Flow: about to enter block parsing, file pos: 0x"+
                        Long.toHexString( filePos ) );
            while(  filePos < endPos ) {
                filePos = instructionParser.getFilePosition();
                if( DEBUG_FLOW )
                    System.out.println( 
                        "Flow: block parsing, file pos: 0x"+
                        Long.toHexString( filePos ) );
                if( DEBUG_REGTRACE )
                    System.out.println( "regTrace: 0x"+
                        Long.toHexString( filePos )+
                        "; regMap: ["+
                        instructionParser.getRegisterMap()+
                        "]" );
                int basePos = (int)( filePos - startPos );
// Continue here or not? The rules are:
// - If we have not been here yet, continue
// - If we have been here but there is no saved register map here, continue.
// - If we have been here and there is saved register map but the overrun counter exceeds the limit, break
//   the analysis 
// - Otherwise if the register consistency check indicates that we should continue, do it.
// - Otherwise break the analysis of the flow.
                boolean haveBeenHere = visitSet.get( basePos );
                if( haveBeenHere ) {
// No register tracing, if we have been here, break
                    if( registerMaps == null )
                        break;
                    Long posObj = new Long( filePos );
                    HashMap<Integer,String> savedRegMap =
                        registerMaps.get( posObj );
                    if( DEBUG_REGMAPS )
                        System.out.println( "regMaps: 0x"+
                            Long.toHexString( filePos )+
                            "; haveBeenHere: 0x"+
                            Long.toHexString( filePos )+"; regmap: ["+
                            savedRegMap+"]" );
                    HashMap<Integer,String> currentRegMap =
                        (HashMap<Integer,String>)instructionParser.getRegisterMap();
// The location is target of a jump instruction but no register map has been saved.
// Save it now and continue.
                    if( registerMaps.containsKey( posObj ) &&
                        ( savedRegMap == null ) ) {
                        if( DEBUG_REGMAPS )
                            System.out.println( "regMaps: saving reg map at 0x"+
                                Long.toHexString( filePos )+
                                " ; reg map: "+currentRegMap );
                        registerMaps.put( 
                            posObj,
                            (HashMap<Integer,String>)currentRegMap.clone() );
                    } else
                    if( savedRegMap != null ) {
                        if( DEBUG_REGMAPS )
                            System.out.println( "regMaps: currentRegMap: ["+
                                    currentRegMap+"]" );
						if( !overrunCheck( posObj, overrunCounter ) ) {
							if( DEBUG_REGMAPS )
								System.out.println( "regMaps: overrun at 0x"+Long.toHexString( filePos ) );
							break;
						}
                        if( !mergeCheckRegTraceMaps( currentRegMap, savedRegMap ) ) {
                            if( DEBUG_REGMAPS )
                                System.out.println( "regMaps: break" );
                            break;
                        }
                        if( DEBUG_REGMAPS )
                                System.out.println( "regMaps: update" );
                        registerMaps.put( 
                            posObj,
                            (HashMap<Integer,String>)currentRegMap.clone() );
                    }
                }
// Check if an exception block is starting here. If so, save the register maps for the handler(s)
// Also, if there is a saved register map for this location, restore the register map from the
// saved version
                if( exceptionHandlerEntryPointList != null ) {
                    if( DEBUG_FLOW )
                        System.out.println( 
                            "Flow: handleRegMaps, file pos: 0x"+
                                Long.toHexString( filePos ) );
                    handleRegMaps( 
                        methodName,
                        exceptionHandlerEntryPointList,
                        instructionParser );
                }
// Insert debug variables into the register set to handle the case when
// the debug variable goes into scope ...
                if( DEBUG_FLOW )
                        System.out.println( 
                            "Flow: before parse" );
                instructionParser.parse();
                if( DEBUG_FLOW )
                        System.out.println( 
                            "Flow: after parse" );
// Now handle the case when the debug variable goes out of scope.
// Save the register trace after the instruction if register tracing is enabled
                if( ( regTraceMap != null ) && ( instructionParser.getAffectedRegisters() != null ) ) {
                    if( DEBUG_FLOW )
                        System.out.println( 
                            "Flow: before saveRegTraceMap" );
                    saveRegTraceMap( instructionParser, regTraceMap );
                    if( DEBUG_FLOW )
                        System.out.println( 
                            "Flow: after saveRegTraceMap" );
                }

// Mark that we have visited this place
                int instructionEndPos = (int)( instructionParser.getFilePosition() - startPos );
                visitSet.set( basePos, instructionEndPos );

// Determine, where to continue the tracing
                DexInstructionParser.ForkStatus forkStatus = instructionParser.getForkStatus();
                if( DEBUG_FLOW )
                        System.out.println( 
                            "Flow: forkStatus: "+forkStatus );
                if( forkStatus == DexInstructionParser.ForkStatus.TERMINATE )
                    break;
                if( ( forkStatus == DexInstructionParser.ForkStatus.FORK_UNCONDITIONALLY ) ||
                    ( forkStatus == DexInstructionParser.ForkStatus.FORK_AND_CONTINUE ) ) {
                    int baseIndex = 
                        forkStatus == DexInstructionParser.ForkStatus.FORK_UNCONDITIONALLY ?
                        1 : 0;
                    long forkData[] = instructionParser.getForkData();
// Mark the jump target locations that they are target of jump instructions
                    if( registerMaps != null ) {
                        for( int i = 0 ; i < forkData.length ; ++i ) {
                            Long targetObj = new Long( forkData[i] );
                            if( !registerMaps.containsKey( targetObj ) ) {
                                if( DEBUG_REGMAPS )
                                    System.out.println( "regMaps: 0x"+
                                        Long.toHexString( filePos )+
                                        "; marking 0x"+
                                        Long.toHexString( forkData[i] ) );
                                registerMaps.put( targetObj, null );
                            }
                        }
                    }
// we go to forkData[0], push the rest of the addresses to the visit stack
                    for( int i = baseIndex ; i < forkData.length ; ++i ) {
                        long target = forkData[i];
                        if( DEBUG_FLOW )
                            System.out.println( 
                                "Flow: processing forkData["+i+"]: target: 0x"+
                                    Long.toHexString( target ) );
                        if( ( target >= startPos ) &&
                            ( target <= endPos ) ) {
                            HashMap<Integer,String> currentRegMap = 
                                instructionParser.getRegisterMap();
                            HashMap<Integer,String> clonedCurrentRegMap =
                                (HashMap<Integer,String>)currentRegMap.clone();
                            visitStack.push( new VisitStackEntry( 
                                                target, 
                                                clonedCurrentRegMap ) );
                        }
                    }
                    if( forkStatus == DexInstructionParser.ForkStatus.FORK_UNCONDITIONALLY )
                        instructionParser.setFilePosition( forkData[0] );
                }
            }
            if( DEBUG_FLOW )
                System.out.println( 
                        "Flow: block parsing exit"+
                        Long.toHexString( filePos ) );
// Branch ended (either by reaching end of method or hitting a previously visited instruction)
// Pull a new address from the stack or finish if the stack is empty
            if( visitStack.empty() )
                break;
            VisitStackEntry entry = visitStack.pop();
            long target = entry.getLocation();
            Long targetObj = new Long( target );
            instructionParser.setRegisterMap( entry.getRegMap() );
// If this is an exception handler entry point, we should have a saved
// register map for it.
            if( DEBUG_EXCP )
                System.out.println( methodName+"/pop: 0x"+
                    Long.toHexString( target )+" ; regmap: "+
                    dumpRegMap( instructionParser.getRegisterMap() ) );
            if( DEBUG_FLOW )
                System.out.println( 
                        "Flow: iteration, target address: "+
                        Long.toHexString( target ) );
            instructionParser.setFilePosition( target );
        } while( true );
// Run the post-first pass processing
        instructionParser.postPassProcessing( false );
// Process the debug info if any
        if( debugInfoParser != null )
            processDebugInfoBlock( 
                debugInfoParser,
                instructionParser, 
                dexMethodHeadParser );
// Second pass: generate the code
        instructionParser.setFilePosition( dexMethodHeadParser.getInstructionBase() );
        instructionParser.setDumpFile( ps );
        instructionParser.setPass( true );
        long actualPosition = 0L;
        while( ( actualPosition = instructionParser.getFilePosition() ) < endPos ) {
            DedexerTask task = instructionParser.getTaskForAddress( actualPosition );
            boolean parseFlag = false;
            if( task != null ) {
                try {
                    task.renderTask( actualPosition );
                    parseFlag = task.getParseFlag( actualPosition );
                } catch( IOException ex ) {
                    System.out.println( "*** ERROR ***: "+ex.getMessage() );
                }
            }
            if( !parseFlag ) {
// Let's check whether the first pass visited this region. If not, turn it into data block
                int visitOffset = (int)( actualPosition - startPos );
                if( visitSet.get( visitOffset ) ) {
                    instructionParser.parse();
                    if( regTraceLog ) {
                        long tracePos = instructionParser.getFilePosition();
                        String s = regTraceMap.get( new Long( tracePos ) );
                        if( s != null )
                            ps.println( "; "+s );
                    }
                } else {
// We have run into an unvisited block. Turn it into a byte dump
                    String label = DexInstructionParser.labelForAddress( 
                            instructionParser.getFilePosition() );
                    openDataArray( label );
                    StringBuilder element = new StringBuilder();
                    boolean firstByte = true;
                    while( ( ( actualPosition = instructionParser.getFilePosition() ) < endPos ) &&
                             ( !visitSet.get( visitOffset++ ) ) ) {
                        task = instructionParser.getTaskForAddress( actualPosition );
                        if( ( task != null ) && task.getParseFlag( actualPosition ) )
                            break;
                        if( !firstByte )
                            element.append( ", " );
                        else
                            firstByte = false;
                        int b = instructionParser.read8Bit();
                        element.append( "0x" );
                        element.append( instructionParser.dumpByte( b ) );
                    }
                    writeByteArray( new String( element ) );
                    closeDataArray( label );
                }
            }
        }
// Run the post-second pass processing
        instructionParser.postPassProcessing( true );
    }

    private void generateClassAnnotations( PrintStream ps, int classIdx ) {
        DexAnnotationParser dap = dexClassDefsBlock.getDexAnnotationParser( classIdx );
        for( int i = 0 ; 
            i < dap.getAnnotationBlocksSize( DexAnnotationParser.AnnotationType.CLASS ) ; 
            ++i ) {
            for( int n = 0 ; 
                n < dap.getAnnotationsSize( DexAnnotationParser.AnnotationType.CLASS, i ) ;
                ++n ) {
                int visibility = dap.getAnnotationVisibilityFlag( 
                        DexAnnotationParser.AnnotationType.CLASS,
                        i, n );
                String type = dap.getAnnotationType( 
                        DexAnnotationParser.AnnotationType.CLASS,
                        i, n );
                if( "Ldalvik/annotation/MemberClasses;".equals( type ) )
                    printMemberClassesAnnotation( ps, classIdx, dap, i, n );
                else
                if( "Ldalvik/annotation/EnclosingClass;".equals( type ) )
                    printEnclosingClassesAnnotation( ps, classIdx, dap, i, n );
                else
                if( "Ldalvik/annotation/EnclosingMethod;".equals( type ) )
                    printEnclosingMethodAnnotation( ps, classIdx, dap, i, n );
                if( !DexAnnotationParser.isSystemAnnotation( type ) ) {
                    ArrayList<String> parmNames = new ArrayList<String>();
                    ArrayList<Object> parmValues = new ArrayList<Object>();
                    for( int k = 0 ; k < dap.getAnnotationElementsSize( 
                        DexAnnotationParser.AnnotationType.CLASS,
                                    i,n ) ; ++k ) {
                        parmNames.add( dap.getAnnotationElementName( 
                            DexAnnotationParser.AnnotationType.CLASS,
                                    i,n,k ) );
                        parmValues.add( dap.getAnnotationElementValue( 
                            DexAnnotationParser.AnnotationType.CLASS,
                                    i,n,k ) );
                        writeClassAnnotation( classIdx, visibility, type, parmNames, parmValues );
                    }
                }

            }
        }
    }

    private void processTryCatchBlock( 
                    String methodName,
                    DexInstructionParser instructionParser,
                    DexMethodHeadParser dexMethodHeadParser,
                    Stack<VisitStackEntry> visitStack,
                    HashMap<Integer,String> initRegMap,
                    ArrayList<ExceptionHandlerMapEntry> exceptionHandlerList ) 
                            throws IOException {
        DexTryCatchBlockParser dtcb = new DexTryCatchBlockParser();
        dtcb.setDexMethodHeadParser( dexMethodHeadParser );
        dtcb.setDexTypeIdsBlock( dexTypeIdsBlock );
        dtcb.setDumpFile( dump );
        dtcb.setRandomAccessFile( file );
        dtcb.parse();
        for( int i = dtcb.getTriesSize() - 1 ; i >= 0  ; --i ) {
            long start = dtcb.getTryStartOffset( i );
            long end = dtcb.getTryEndOffset( i );
            String startLabel = instructionParser.labelForAddress( start );
            String endLabel = instructionParser.labelForAddress( end );
            instructionParser.placeLabel( start, startLabel );
            instructionParser.placeLabel( end, endLabel );
            for( int n = 0 ; n < dtcb.getTryHandlersSize( i ) ; ++n ) {
                String excpType = dtcb.getTryHandlerType( i,n );
                long handlerOffset = dtcb.getTryHandlerOffset( i,n );
                HashMap<Integer,String> clonedInitRegMap =
                        (HashMap<Integer,String>)initRegMap.clone();
                visitStack.push( new VisitStackEntry( 
                                    handlerOffset,
                                    clonedInitRegMap ) );
                String handlerOffsetName = instructionParser.
                                            labelForAddress( handlerOffset );
// Put a marker for the first pass that register map needs to be saved for a certain
// exception handler at the start location
                if( exceptionHandlerList != null ) {
                    clonedInitRegMap =
                        (HashMap<Integer,String>)initRegMap.clone();
                    saveExceptionHandlerMapMarker( 
                            methodName,
                            exceptionHandlerList, 
                            start,
                            end,
                            handlerOffset,
                            excpType,
                            clonedInitRegMap );
                }
                instructionParser.placeLabel( handlerOffset, handlerOffsetName );
                instructionParser.
                    getCodeGenerator().
                    writeTryCatchBlock( startLabel,endLabel,excpType,handlerOffsetName );
            }
        }

    }

    private void saveExceptionHandlerMapMarker( 
                        String methodName,
                        ArrayList<ExceptionHandlerMapEntry> exceptionHandlerList, 
                        long start,
                        long end,
                        long handlerOffset,
                        String exceptionType,
                        HashMap<Integer,String> regMap ) {
        ExceptionHandlerMapEntry entry = new ExceptionHandlerMapEntry(
                    start,
                    end,
                    handlerOffset,
                    exceptionType,
                    regMap );
        exceptionHandlerList.add( entry );
        if( DEBUG_EXCP )
            System.out.println( "excp,saveMarker: "+methodName+"; entry: "+entry );
    }

    private void handleRegMaps( 
                        String methodName,
                        ArrayList<ExceptionHandlerMapEntry> exceptionHandlerEntryPointList,
                        DexInstructionParser instructionParser ) 
                                    throws IOException {
        long pos = instructionParser.getFilePosition();
// Iterate over the handlers and figure out the current position is in the range
// of any active exception handlers. If so, merge the current register map into
// the register map belonging to the exception handler. If the current position
// is the handler address of the exception, activate the saved register map
// belonging to the exception
        for( int i = 0 ; i < exceptionHandlerEntryPointList.size() ; ++i ) {
            ExceptionHandlerMapEntry entry = exceptionHandlerEntryPointList.get( i );
            if( entry.withinRange( pos ) ) {
                if( DEBUG_EXCP )
                    System.out.println( "excp,withinRange: "+
                                Long.toHexString( pos )+" : "+
                                entry );
                HashMap<Integer,String> regMap = instructionParser.getRegisterMap();
                mergeExcpRegMaps( entry.getRegMap(), regMap );
                if( DEBUG_EXCP )
                    System.out.println( "excp,merged regmap: 0x"+
                            Long.toHexString( pos )+"; entry: "+
                            entry+"; merged regmap: "+
                            entry.getRegMap() );
            }
            if( entry.atHandler( pos ) ) {
                HashMap<Integer,String> excpRegMap = 
                        entry.getRegMap();
// We can't set the original instance to instruction parser - that would
// corrupt the register map for further executions of the handler.
                excpRegMap = 
                        (HashMap<Integer,String>)excpRegMap.clone();
                excpRegMap.put( DexInstructionParser.REGMAP_RESULT_KEY, 
                                entry.getExceptionType() );
                if( DEBUG_EXCP )
                    System.out.println( "excp,setRegMap: 0x"+
                            Long.toHexString( pos )+
                            "; exception register map set: ["+
                            excpRegMap+
                            "]" );
                instructionParser.setRegisterMap( excpRegMap );
            }
        }
    }

    private DexDebugInfoParser parseDebugInfoBlock( 
                    DexInstructionParser instructionParser,
                    DexMethodHeadParser dexMethodHeadParser ) throws IOException {
        DexDebugInfoParser ddp = new DexDebugInfoParser();
        ddp.setDexStringIdsBlock( dexStringIdsBlock );
        ddp.setDexTypeIdsBlock( dexTypeIdsBlock );
        ddp.setDumpFile( dump );
        ddp.setRandomAccessFile( file );
        ddp.setFilePosition( dexMethodHeadParser.getDebugOffset() );
        ddp.parse();
        return ddp;
    }

    private void processDebugInfoBlock(
                    DexDebugInfoParser ddp,
                    DexInstructionParser instructionParser,
                    DexMethodHeadParser dexMethodHeadParser ) {
        for( int i = 0 ; i < ddp.getLineNumbers() ; ++i ) {
            int lineNumber = ddp.getLineNumber( i );
            int address = ddp.getLineNumberAddress( i );
            int fileOffset = (int)dexMethodHeadParser.getInstructionBase()+address*2;
            instructionParser.placeTask( 
                fileOffset,
                new LineNumberTask( instructionParser,lineNumber ) );
        }
        for( int i = 0 ; i < ddp.getLocalVariables() ; ++i ) {
            int regNum = ddp.getLocalVariableRegNum( i );
            String variableName = ddp.getLocalVariableName( i );
            String variableType = ddp.getLocalVariableType( i );
            int startOffset = ddp.getLocalVariableStartOffset( i );
            int endOffset = ddp.getLocalVariableEndOffset( i );
            if( regNum >= 0 ) {
                startOffset = startOffset*2 + (int)dexMethodHeadParser.getInstructionBase();
                endOffset = endOffset*2 + (int)dexMethodHeadParser.getInstructionBase();
                String startOffsetLabel = instructionParser.labelForAddress( startOffset );
                String endOffsetLabel = instructionParser.labelForAddress( endOffset );
                instructionParser.placeLabel( startOffset, startOffsetLabel );
                instructionParser.placeLabel( endOffset, endOffsetLabel );
                writeLocalVariable( regNum,
                                    variableName,
                                    variableType,
                                    startOffsetLabel,
                                    endOffsetLabel );
            }
        }
    }

    private void initializeRegTrace( 
                ArrayList<RegisterTraces> regTraces, 
                DexDebugInfoParser debugInfoParser,
                DexMethodHeadParser dexMethodHeadParser ) {
        for( int i = 0 ; i < debugInfoParser.getLocalVariables() ; ++i ) {
            int regNum = debugInfoParser.getLocalVariableRegNum( i );
            String variableType = debugInfoParser.getLocalVariableType( i );
            int startOffset = debugInfoParser.getLocalVariableStartOffset( i );
            int endOffset = debugInfoParser.getLocalVariableEndOffset( i );
            if( regNum >= 0 ) {
                startOffset = startOffset*2 + (int)dexMethodHeadParser.getInstructionBase();
                endOffset = endOffset*2 + (int)dexMethodHeadParser.getInstructionBase();
                RegisterTraces t = new RegisterTraces( 
                                        startOffset,
                                        endOffset, 
                                        variableType,
                                        regNum );
                regTraces.add( t );
            }
        }
    }

    private void dumpMethodName( String methodName ) throws IOException {
        if( dump != null ) {
            dump.println( "****************************" );
            dump.println( "Method: "+methodName );
        }
    }

    private String getVisibility( int visibility ) {
        String returnValue = "";
        switch( visibility ) {
            case DexAnnotationParser.VISIBILITY_BUILD:
                returnValue = "buildVisibility";
                break;

            case DexAnnotationParser.VISIBILITY_RUNTIME:
                returnValue = "runtimeVisibility";
                break;

            case DexAnnotationParser.VISIBILITY_SYSTEM:
                returnValue = "systemVisibility";
                break;
        }
        return returnValue;
    }

    private void addFieldAnnotation( PrintStream ps, 
                                    String fieldShortName, 
                                    int classidx, 
                                    int i ) {
        DexAnnotationParser dap = dexClassDefsBlock.getDexAnnotationParser( classidx );
        if( dap != null ) {
            int annotationIdx = dap.getBlockIndexFromAsset( 
                DexAnnotationParser.AnnotationType.FIELD, fieldShortName );
            if( annotationIdx >= 0 ) {
                for( int n = 0 ; 
                        n < dap.getAnnotationsSize( 
                            DexAnnotationParser.AnnotationType.FIELD, annotationIdx ) ;
                        ++n ) {
                    int visibility = dap.getAnnotationVisibilityFlag( 
                        DexAnnotationParser.AnnotationType.FIELD,
                        annotationIdx, n );
                    String type = dap.getAnnotationType( 
                                    DexAnnotationParser.AnnotationType.FIELD,
                                    annotationIdx, n );
                    ps.println( "  .annotation "+
                        getVisibility( visibility )+
                        " "+
                        type );
                    for( int k = 0 ; k < dap.getAnnotationElementsSize( 
                        DexAnnotationParser.AnnotationType.FIELD,
                                    annotationIdx,n ) ; ++k ) {
                        String parmName = dap.getAnnotationElementName( 
                            DexAnnotationParser.AnnotationType.FIELD,
                                    annotationIdx,n,k );
                        Object o = dap.getAnnotationElementValue( 
                            DexAnnotationParser.AnnotationType.FIELD,
                                    annotationIdx,n,k );
                        ps.println( "    "+
                                parmName +
                                " "+
                                DexEncodedArrayParser.getTypeString( o )+
                                " = "+
                                o.toString() );
                    }
                    ps.println( "  .end annotation" );
                }
                ps.println( ".end field" );
            }
        }
    }

    private void addMethodAnnotation( PrintStream ps, 
                                    String methodShortName, 
                                    int classidx, 
                                    int i ) {
        DexAnnotationParser dap = dexClassDefsBlock.getDexAnnotationParser( classidx );
        if( dap != null ) {
            int annotationIdx = dap.getBlockIndexFromAsset( 
                DexAnnotationParser.AnnotationType.METHOD, methodShortName );
            if( annotationIdx >= 0 ) {

// Handling .throws

                int throwsIdx = dap.searchAnnotationType( 
                    DexAnnotationParser.AnnotationType.METHOD, 
                    annotationIdx, 
                    "Ldalvik/annotation/Throws;" );
                if( throwsIdx >= 0 )
                    printThrows( ps, dap, annotationIdx, throwsIdx );

// Method annotations

                for( int n = 0 ; 
                        n < dap.getAnnotationsSize( 
                            DexAnnotationParser.AnnotationType.METHOD, annotationIdx ) ;
                        ++n ) {
                    int visibility = dap.getAnnotationVisibilityFlag( 
                        DexAnnotationParser.AnnotationType.METHOD,
                        annotationIdx, n );
                    String type = dap.getAnnotationType( 
                                    DexAnnotationParser.AnnotationType.METHOD,
                                    annotationIdx, n );
                    if( !DexAnnotationParser.isSystemAnnotation( type ) ) {
                        ps.println( ".annotation "+
                            getVisibility( visibility )+
                            " "+
                            type );
                        printElements( ps,
                                dap,
                                DexAnnotationParser.AnnotationType.METHOD,
                                annotationIdx,
                                n );
                        ps.println( ".end annotation" );
                    }
                }

// parameter annotations

                for( int n = 0 ; 
                        n < dap.getAnnotationsSize( 
                            DexAnnotationParser.AnnotationType.PARAMETER, annotationIdx ) ;
                        ++n ) {
                    int visibility = dap.getAnnotationVisibilityFlag( 
                        DexAnnotationParser.AnnotationType.PARAMETER,
                        annotationIdx, n );
                    String type = dap.getAnnotationType( 
                                    DexAnnotationParser.AnnotationType.PARAMETER,
                                    annotationIdx, n );
                    int paramNumber = dap.getAnnotationParameterIndex( 
                                    DexAnnotationParser.AnnotationType.PARAMETER,
                                    annotationIdx,
                                    n ) + 1;
                    ps.println( ".annotation "+
                            getVisibility( visibility )+
                            " param "+
                            paramNumber+
                            " "+
                            type );
                    printElements( ps,
                                dap,
                                DexAnnotationParser.AnnotationType.PARAMETER,
                                annotationIdx,
                                n );
                    ps.println( ".end annotation" );
                }
            }
        }
    }

    private void printElements( PrintStream ps, 
                            DexAnnotationParser dap,
                            DexAnnotationParser.AnnotationType type,
                            int annotationIdx,
                            int n ) {
                    for( int k = 0 ; k < dap.getAnnotationElementsSize( 
                                    type,
                                    annotationIdx,n ) ; ++k ) {
                            String parmName = dap.getAnnotationElementName( 
                                    type,
                                    annotationIdx,n,k );
                            Object o = dap.getAnnotationElementValue( 
                                    type,
                                    annotationIdx,n,k );
                            ps.println( "    "+
                                parmName +
                                " "+
                                DexEncodedArrayParser.getTypeString( o )+
                                " = "+
                                o.toString() );
                        }
    }

    private void printThrows( PrintStream ps, 
                        DexAnnotationParser dap, 
                        int annotationIdx, 
                        int throwsIdx ) {
        int elementsSize = dap.getAnnotationElementsSize( 
                                    DexAnnotationParser.AnnotationType.METHOD,
                                    annotationIdx, throwsIdx );
// In reality, there is only one "value" element. The loop is a pure paranoia
        for( int i = 0 ; i < elementsSize ; ++i ) {
            String elementName = dap.getAnnotationElementName( 
                                    DexAnnotationParser.AnnotationType.METHOD,
                                    annotationIdx, throwsIdx, i );
            if( "value".equals( elementName ) ) {
                Object o = dap.getAnnotationElementValue( 
                                    DexAnnotationParser.AnnotationType.METHOD,
                                    annotationIdx, 
                                    throwsIdx,
                                    i );
                if( o instanceof StaticArray ) {
                    StaticArray array = (StaticArray)o;
                    for( int n = 0 ; n < array.length() ; ++n )
                        ps.println( ".throws "+array.get( n ) );
                }
            }
        }
    }

    private void printMemberClassesAnnotation( PrintStream ps, 
                                            int classIdx,
                                            DexAnnotationParser dap, 
                                            int i, 
                                            int n ) {
        int elementsSize = dap.getAnnotationElementsSize( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i,n );
// In reality, there is only one "value" element. The loop is a pure paranoia
        for( int k = 0 ; k < elementsSize ; ++k ) {
            String elementName = dap.getAnnotationElementName( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i,n,k );
            if( "value".equals( elementName ) ) {
                Object o = dap.getAnnotationElementValue( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i, 
                                    n,
                                    k );
                if( o instanceof StaticArray ) {
                    String outerName = dexClassDefsBlock.getClassNameOnly( classIdx );
                    StaticArray array = (StaticArray)o;
                    for( int l = 0 ; l < array.length() ; ++l ) {
                        String memberClassName = (String)array.get( l );
                        String classNameOnly = DexClassDefsBlock.
                                getClassNameWithoutPackage( memberClassName );
                        String memberClassNameWithoutPrePostfix = 
                                DexClassDefsBlock.
                                    getClassNameWithoutPrePostfix( memberClassName );
                        ps.println( ".inner class "+
                                    classNameOnly+
                                    " inner "+
                                    memberClassNameWithoutPrePostfix+
                                    " outer "+
                                    outerName );
                    }
                }
            }
        }
    }

    private void printEnclosingClassesAnnotation( 
                                    PrintStream ps, 
                                    int classIdx, 
                                    DexAnnotationParser dap, 
                                    int i, 
                                    int n ) {
        int elementsSize = dap.getAnnotationElementsSize( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i,n );
// In reality, there is only one "value" element. The loop is a pure paranoia
        for( int k = 0 ; k < elementsSize ; ++k ) {
            String elementName = dap.getAnnotationElementName( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i,n,k );
            if( "value".equals( elementName ) ) {
                Object o = dap.getAnnotationElementValue( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i, 
                                    n,
                                    k );
                if( o instanceof String ) {
                    String innerClassFullName = dexClassDefsBlock.getClassNameOnly( classIdx );
                    String outerClassFullName = (String)o;
                    String innerClassNameWithoutPrePostfix = 
                                DexClassDefsBlock.
                                    getClassNameWithoutPrePostfix( innerClassFullName );
                    String innerClassShortName = 

                                DexClassDefsBlock.
                                    getClassNameWithoutPackage( innerClassFullName );
                    String outerClassNameWithoutPrePostfix = 
                                DexClassDefsBlock.
                                    getClassNameWithoutPrePostfix( outerClassFullName );
                    ps.println( ".inner class "+
                                    innerClassShortName+
                                    " inner "+
                                    innerClassNameWithoutPrePostfix+
                                    " outer "+
                                    outerClassNameWithoutPrePostfix );
                }
            }
        }
    }

    private void printEnclosingMethodAnnotation( 
                                    PrintStream ps, 
                                    int classIdx, 
                                    DexAnnotationParser dap, 
                                    int i, 
                                    int n ) {
        int elementsSize = dap.getAnnotationElementsSize( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i,n );
// In reality, there is only one "value" element. The loop is a pure paranoia
        for( int k = 0 ; k < elementsSize ; ++k ) {
            String elementName = dap.getAnnotationElementName( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i,n,k );
            if( "value".equals( elementName ) ) {
                Object o = dap.getAnnotationElementValue( 
                                    DexAnnotationParser.AnnotationType.CLASS,
                                    i, 
                                    n,
                                    k );
                if( o instanceof String ) {
                    String methodName = (String)o;
                    ps.println( ".enclosing method "+
                        methodName );
                }
            }
        }
    }

    private void saveRegTraceMap( DexInstructionParser instructionParser, 
                                HashMap<Long,String> regTraceMap ) throws IOException {
        StringBuilder sb = new StringBuilder();
        int affectedRegisters[] = instructionParser.getAffectedRegisters();
        for( int i = 0 ; i < affectedRegisters.length ; ++i ) {
            if( i > 0 )
                sb.append( " , " );
            int reg = affectedRegisters[i];
            sb.append( "v"+reg+" : " );
            HashMap<Integer,String> regMap = instructionParser.getRegisterMap();
            sb.append( regMap.get( new Integer( reg ) ) );
        }
        long pos = instructionParser.getFilePosition();
        String line = new String( sb );
        regTraceMap.put( new Long( pos ),line );
        if( DEBUG_REGTRACE )
            System.out.println( "regTrace: 0x"+Long.toHexString( pos ) +
                                "; saved regtrace: "+line );
    }

    private StringBuilder dumpRegMap( HashMap<Integer,String> regMap ) {
        StringBuilder b = new StringBuilder();
        for( Iterator<Integer> it = regMap.keySet().iterator() ; it.hasNext() ; ) {
                Integer i = it.next();
                String value = regMap.get( i );
                b.append( " v"+i+" : "+value );
        }
        return b;
    }

/**
  * Merges the old reg trace map with the new one and check for inconsistencies.
  * The rules are:
  * - Two reg trace maps are consistent if all the registers in the old reg trace
  *   maps are present in the new reg trace map with the same type.
  * - In addition, if the old reg trace maps contains single-length in a given
  *   register but the new reg trace map contains an object type (type starting
  *   with L or [) then the object type overwrites the single-length type. This
  *   handles the case when an reference variable was initialized with null and is
  *   assigned to an object reference only in some branches of the program. In this
  *   case, the two reg trace maps are consistent but the branch needs to be 
  *   analyzed one more time with the new reg trace map.
  * - If both the old and the new value are classes, then the youngest common 
  *   ancestor of the two classes must be found and if that ancestor is not equal
  *   to the old value, the branch needs to be analysed with that ancestor.
  * - In any other case, the reg trace maps are inconsistent.
  *   The method returns true if the branch needs to be revisited with the new
  *   reg trace map. In any other case, it returns false.
  *
  */
    private boolean mergeCheckRegTraceMaps( 
            HashMap<Integer,String> newRegTraceMap,
            HashMap<Integer,String> oldRegTraceMap ) {
        boolean revisit = false;
        for( Iterator<Integer> it = oldRegTraceMap.keySet().iterator() ;
            it.hasNext() ; ) {
            Integer key = it.next();
            String oldValue = oldRegTraceMap.get( key );
            String newValue = newRegTraceMap.get( key );
            if( DEBUG_MERGE )
                System.out.println( "Merging: key: "+
                                    key+
                                    "; oldValue: "+
                                    oldValue+
                                    "; newValue: "+
                                    newValue );
            if( newValue == null ) {
// The old set may be a superset of the new one.
                if( DEBUG_MERGE )
                    System.out.println( "Moving old value to new reg trace map" );
                newRegTraceMap.put( key,oldValue );
            } else
            if( oldValue == null )
                continue;
            else
            if( newValue.equals( oldValue ) )
                continue;
            else
            if( DexInstructionParser.TYPE_SINGLE_LENGTH.equals( oldValue ) &&
                isClass( newValue ) ) {
                if( DEBUG_MERGE )
                    System.out.println( "single-length->class: revisit" );
                revisit = true;
            } else
            if( isClass( newValue ) && isClass( oldValue ) ) {
// newValue and oldValue are both classes, we should find out the youngest common
// ancestor of these two classes. This is, however, not possible if the disassembler
// is not processing ODEX files because otherwise the disassembler is only aware
// of the classes in the DEX file which it currently processes. As this affects
// only the registers displayed with the -r switch for non-ODEX files, we sweep
// the issue under the carpet and we don't do ancestor searching in this case but
// replace the class with java/lang/Object, the common ancestor of every class
                if( dexOffsetResolver != null ) {
                    if( DEBUG_MERGE )
                        System.out.println( "finding ancestor for: oldValue: "+
                                            oldValue+
                                            "; newValue: "+
                                            newValue );
                    String ancestor = 
                        dexOffsetResolver.findCommonAncestor( newValue, oldValue );
                    ancestor = "L"+ancestor+";";
                    if( DEBUG_MERGE )
                        System.out.println( "ancestor: "+ancestor );
                    if( !newValue.equals( ancestor ) &&
                        !oldValue.equals( ancestor ) ) {
                        if( DEBUG_MERGE )
                            System.out.println( 
                                "Moving ancestor to new reg trace map (key: "+
                                key+
                                "; value: "+
                                ancestor+")" );
                        newRegTraceMap.put( key,ancestor );
                        revisit = true;
                    } 
                } else
                if( !newValue.equals( oldValue ) && 
                    !oldValue.equals( "Ljava/lang/Object;" ) ) {
                    if( DEBUG_MERGE )
                        System.out.println( "Replacing key "+key+" with java/lang/Object" );
                    newRegTraceMap.put( key,"Ljava/lang/Object;" );
                    revisit = true;
                }
            } else
            if( !newValue.equals( oldValue ) ) {
                revisit = false;
                break;
            } 
        }
        return revisit;
    }

/**
  * Merges the current register map with the register map associated to the
  * exception handler.
  * Rules:
  * - If the current map contains a register with a certain number but the
  *   exception handler map does not contain it, add the register and its value
  *   to the exception handler map.
  * - If the exception handler map contains a register with single-length value
  *   but the current map contains it with an object value, write over the exception
  *   handler map with the value in the current map.
  * Otherwise there is no change.
  */
    private void mergeExcpRegMaps(
            HashMap<Integer,String> exceptionMap,
            HashMap<Integer,String> currentMap ) {
        for( Iterator<Integer> it = currentMap.keySet().iterator() ;
            it.hasNext() ; ) {
            Integer key = it.next();
            String excpValue = exceptionMap.get( key );
            String currentValue = currentMap.get( key );
            if( excpValue == null ) {
                exceptionMap.put( key,currentValue );
            } else
            if( currentValue == null )
                continue;
            else
            if( DexInstructionParser.TYPE_SINGLE_LENGTH.equals( excpValue ) &&
                ( currentValue.startsWith( "[" ) || currentValue.startsWith( "L" ) ) ) {
                exceptionMap.put( key,currentValue );
            }
        }
    }

    private boolean isClass( String className ) {
        return className.startsWith( "[" ) || className.startsWith( "L" );
    }

// Visit stack entry. Stores the location to return to and the register map at that location
    class VisitStackEntry {
        public VisitStackEntry( long location, HashMap<Integer,String> regMap ) {
            this.location = location;
            if( regMap == null )
                this.regMap = new HashMap<Integer,String>();
            else
                this.regMap = 
                    (HashMap<Integer,String>)regMap.clone();   // regMap will change as the analysis continues, hence we save a clone
        }

        public long getLocation() {
            return location;
        }

        public HashMap<Integer,String> getRegMap() {
            return regMap;
        }

        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append( "VisitStackEntry: 0x"+Long.toHexString( location ) );
            b.append( " {" );
            b.append( dumpRegMap( regMap ) );
            b.append( "}" );
            return new String( b );
        }

        private long location;
        private HashMap<Integer,String> regMap;
    }

    class ExceptionHandlerMapEntry {
        public ExceptionHandlerMapEntry( 
                    long start, 
                    long end, 
                    long handler, 
                    String exceptionType,
                    HashMap<Integer,String> regMap ) {
            this.start = start;
            this.end = end;
            this.handler = handler;
            this.exceptionType = exceptionType;
            this.regMap = regMap;
        }

        public boolean withinRange( long pos ) {
            return ( pos >= start ) && ( pos < end );
        }

        public boolean atHandler( long pos ) {
            return pos == handler;
        }

        public HashMap<Integer,String> getRegMap() {
            return regMap;
        }

        public String getExceptionType() {
            return exceptionType;
        }

        public long getHandler() {
            return handler;
        }

        public String toString() {
            return "ExceptionHandlerMapEntry: start: 0x"+
                    Long.toHexString( start )+
                    "; end: 0x"+
                    Long.toHexString( end )+
                    "; handler: "+
                    Long.toHexString( handler )+
                    "; exceptionType: "+exceptionType;
        }

        private long start;
        private long end;
        private long handler;
        private String exceptionType;
        private HashMap<Integer,String> regMap;
    }

/**
  * Check if there was an overrun at a certain location. 
  * The register analyser may fall into endless loop if the regmap
  * solution does not converge. We use an arbitrary limit of 5 iterations and
  * interrupt the analyser if a certain location is visited too many times.
  * Returns true if there is no overrun, false otherwise
  */
	private boolean overrunCheck( Long posObj, HashMap<Long,Integer> overrunCounter ) {
		if( overrunCounter == null )
			return true;
		Integer ctr = overrunCounter.get( posObj );
		if( ctr == null ) {
			ctr = new Integer( 1 );
			overrunCounter.put( posObj, ctr );
			return true;
		}
		int ctrv = ctr.intValue() + 1;
		if( ctrv > REVISIT_LIMIT )
			return false;
		overrunCounter.put( posObj, new Integer( ctrv ) );
		return true;
	}

}

