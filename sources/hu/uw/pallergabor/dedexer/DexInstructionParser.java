/**
  * Parses DEX instruction blocks
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.*;

public class DexInstructionParser extends DexParser {
    public enum ForkStatus {
        CONTINUE,
        FORK_UNCONDITIONALLY,
        FORK_AND_CONTINUE,
        TERMINATE
    }

/**
  * Key of the method invocation result value in the register map
  */
    public static final Integer REGMAP_RESULT_KEY = new Integer( -1 );

    public static final String TYPE_SINGLE_LENGTH = "single-length";
    public static final String TYPE_DOUBLE_LENGTH = "double-length";
    public static final String TYPE_UNKNOWN = "unknown";

    public void initializePass( boolean secondPass ) {
        this.secondPass = secondPass;
        tasks.clear();
    }

    public void parse() throws 
                        IOException,UnknownInstructionException {
        long instrBase = file.getFilePointer();
        int instrCode = read8Bit();
        InstructionType instrType = instructionTypes[ instrCode ];
        StringBuilder instrText = new StringBuilder();
        instrText.append( instructionNames[ instrCode ] );
        instrText.append( "\t" );
        forkStatus = initialForkStatus( instrCode );
        forkData = null;
        affectedRegisters = null;
        boolean generateText = true;
        switch( instrType ) {
            case UNKNOWN_INSTRUCTION:
                throw new UnknownInstructionException(
                    "Unknown instruction 0x"+
                        dumpByte( instrCode )+
                        " at offset "+
                        dumpLong( file.getFilePointer() - 1L )
                    );

// The instruction is followed by one byte, the lower 4 bit of the byte stores
// a register code, the higher 4 bit is a 4-bit constant. E.g. const/4 vx,lit4
            case REGCONST4: {
                int b1 = read8Bit();
                int reg = b1 & 0x0F;
                int constant = ( b1 & 0xF0 ) >> 4;
                instrText.append( "v"+reg );
                instrText.append( "," );
                instrText.append( constant );
// Moves integer to reg
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),TYPE_SINGLE_LENGTH );
            }
            break;

// The instruction is followed by a register index byte and a 16-bit index
// to the string constant table
            case REGSTRINGCONST: {
                int reg = read8Bit();
                int stringidx = read16Bit();
                instrText.append( "v"+reg );
                instrText.append( "," );
                instrText.append( "\""+
                                dexStringIdsBlock.getString( stringidx )+
                                "\"" );
// Move String type to reg
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),"Ljava/lang/String;" );
            }
            break;

// Basically the same as REGSTRINGCONST but with a 32-bit index
            case REGSTRINGCONST_JUMBO: {
                int reg = read8Bit();
                int stringidx = (int)read32Bit();
                instrText.append( "v"+reg );
                instrText.append( "," );
                instrText.append( "\""+
                                dexStringIdsBlock.getString( stringidx )+
                                "\"" );
// Move String type to reg
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),"Ljava/lang/String;" );
            }
            break;

// The instruction is followed by one byte whose higher 4 bits store the number of
// registers to pass to the method invoked. Then a 16-bit method index comes. This is
// followed by the bytes storing the 4-bit indexes for the invocation 
// registers. This block is always 16-bit
// word aligned (e.g. if there are 3 parameters, 4 bytes follow the method index
// word, the last byte is discarded.
            case METHODINVOKE: {
                int b2 = read8Bit();
                int regno = (  b2 & 0xF0 ) >> 4;
                int origRegNo = regno;
// If invocation regno % 4 == 1 and regno > 4, the last invocation register 
// index is encoded on the lowest 4 bit of the regno byte
                int lastreg = -1;
                if( ( regno > 4 ) && ( regno % 4 ) == 1 ) {
                    --regno;
                    lastreg = b2 & 0x0F;
                }
                int methodidx = read16Bit();
                String proto = dexMethodIdsBlock.getProto( methodidx );
                String resultType = DexMethodIdsBlock.getResultType( proto );
                if( "V".equals( resultType ) )
                    regMap.remove( REGMAP_RESULT_KEY );
                else
                    regMap.put( REGMAP_RESULT_KEY,
                        convertJavaTypeToInternal( resultType ) );
                instrText.append( "{" );
                int regByte = 0;
                int byteCounter = 0;
                ArrayList<Integer> registerList = new ArrayList<Integer>();
                for( int i = 0 ; i < regno ; ++i ) {
                    if( i > 0 )
                        instrText.append( "," );
                    int reg = 0;
                    if( ( i % 2 ) == 0 ) {
                        regByte = read8Bit();
                        ++byteCounter;
                        reg = regByte & 0x0F;
                    } else
                        reg = ( regByte & 0xF0 ) >> 4;
                    instrText.append( "v"+reg );
                    registerList.add( new Integer( reg ) );
                }
                if( lastreg >= 0 ) {
                    instrText.append( ",v"+lastreg );
                    registerList.add( new Integer( lastreg ) );
                }
                if( ( byteCounter % 2 ) != 0 )
                    read8Bit();         // Align to 16 bit
                instrText.append( "}," );
                instrText.append( dexMethodIdsBlock.getMethod( methodidx ) );
                instrText.append( "\t; "+proto );
                affectedRegisters = getAffectedRegistersForRegList( 
                            registerList,
                            proto,
                            1 );
            }
            break;

            case METHODINVOKE_STATIC: {
                int b2 = read8Bit();
                int regno = (  b2 & 0xF0 ) >> 4;
                int origRegNo = regno;
// If invocation regno % 4 == 1 and regno > 4, the last invocation register 
// index is encoded on the lowest 4 bit of the regno byte
                int lastreg = -1;
                if( ( regno > 4 ) && ( regno % 4 ) == 1 ) {
                    --regno;
                    lastreg = b2 & 0x0F;
                }
                int methodidx = read16Bit();
                String proto = dexMethodIdsBlock.getProto( methodidx );
                String resultType = DexMethodIdsBlock.getResultType( proto );
                if( "V".equals( resultType ) )
                    regMap.remove( REGMAP_RESULT_KEY );
                else
                    regMap.put( REGMAP_RESULT_KEY,
                        convertJavaTypeToInternal( resultType ) );
                instrText.append( "{" );
                int regByte = 0;
                int byteCounter = 0;
                ArrayList<Integer> registerList = new ArrayList<Integer>();
                for( int i = 0 ; i < regno ; ++i ) {
                    if( i > 0 )
                        instrText.append( "," );
                    int reg = 0;
                    if( ( i % 2 ) == 0 ) {
                        regByte = read8Bit();
                        ++byteCounter;
                        reg = regByte & 0x0F;
                    } else
                        reg = ( regByte & 0xF0 ) >> 4;
                    instrText.append( "v"+reg );
                    registerList.add( new Integer( reg ) );
                }
                if( lastreg >= 0 ) {
                    instrText.append( ",v"+lastreg );
                    registerList.add( new Integer( lastreg ) );
                }
                if( ( byteCounter % 2 ) != 0 )
                    read8Bit();         // Align to 16 bit
                instrText.append( "}," );
                instrText.append( dexMethodIdsBlock.getMethod( methodidx ) );
                instrText.append( "\t; "+proto );
                affectedRegisters = getAffectedRegistersForRegList( 
                            registerList,
                            proto,
                            0 );
            }
            break;

            case QUICKMETHODINVOKE: {
                int b2 = read8Bit();
                int regno = (  b2 & 0xF0 ) >> 4;
                int origRegNo = regno;
// If invocation regno % 4 == 1 and regno > 4, the last invocation register 
// index is encoded on the lowest 4 bit of the regno byte
                int lastreg = -1;
                if( ( regno > 4 ) && ( regno % 4 ) == 1 ) {
                    --regno;
                    lastreg = b2 & 0x0F;
                }
                int vtableOffset = read16Bit();
                instrText.append( "{" );
                int regByte = 0;
                int byteCounter = 0;
                String baseClass = null;
                ArrayList<Integer> registerList = new ArrayList<Integer>();
                for( int i = 0 ; i < regno ; ++i ) {
                    if( i > 0 )
                        instrText.append( "," );
                    int reg = 0;
                    if( ( i % 2 ) == 0 ) {
                        regByte = read8Bit();
                        ++byteCounter;
                        reg = regByte & 0x0F;
                    } else
                        reg = ( regByte & 0xF0 ) >> 4;
                    instrText.append( "v"+reg );
                    registerList.add( new Integer( reg ) );
// fetch the base class whose method will be invoked. This is needed
// for vtable offset resolution.
                    if( ( !secondPass && 
                          ( dexOffsetResolver != null ) ) && 
                        ( i == 0 ) ) {
                        baseClass = regMap.get( new Integer( reg ) );
                        if( ( baseClass == null ) ||
                            ( TYPE_SINGLE_LENGTH.equals( baseClass ) ) ||
                            ( TYPE_DOUBLE_LENGTH.equals( baseClass ) ) )
                            baseClass = getLocalVariableType( instrBase,reg );
                        if( baseClass != null )
                            baseClass = DexTypeIdsBlock.LTypeToJava( baseClass );
                    }
                }
                if( lastreg >= 0 ) {
                    instrText.append( "v"+lastreg );
                    registerList.add( new Integer( lastreg ) );
                }
                if( ( byteCounter % 2 ) != 0 )
                    read8Bit();         // Align to 16 bit
                instrText.append( "}," );
                boolean offsetResolved = false;
// If in the first pass, we try to resolve the vtable offset and store the result
// in quickParameterMap. In the second pass, we use the resolved parameters to
// finally parse the instruction.
                if( dexOffsetResolver != null ) {
                    if( secondPass ) {
                        Long key = new Long( file.getFilePointer() );
                        String parameter = quickParameterMap.get( key );
                        if( parameter != null ) {
                            instrText.append( parameter );
                            offsetResolved = true;
                        }
                    } else {
// First pass. Try to resolve the vtable offset and store it if successful for the
// second pass.
// The base class register was tracked - we may even be able to resolve
// the vtable offset 
                        if( baseClass != null ) {
                            String methodProto = 
                                dexOffsetResolver.getMethodNameFromOffset( 
                                    baseClass, vtableOffset );
                            if( methodProto != null ) {
                                String proto = "";
                                int idx = methodProto.indexOf( ',' );
                                if( idx >= 0 ) {
                                    proto = methodProto.substring( idx+1 );
                                    methodProto = methodProto.substring( 0,idx );
                                }
                                String parameter = 
                                    methodProto+
                                    "\t; "+proto+
                                    " , vtable #0x"+
                                    Integer.toHexString( vtableOffset );
                                Long key = new Long( file.getFilePointer() );
                                quickParameterMap.put( key,parameter );
                                instrText.append( parameter );
                                String resultType = 
                                    DexMethodIdsBlock.getResultType( proto );
                                if( "V".equals( resultType ) )
                                    regMap.remove( REGMAP_RESULT_KEY );
                                else
                                    regMap.put( REGMAP_RESULT_KEY,
                                        convertJavaTypeToInternal( resultType ) );
                                affectedRegisters = getAffectedRegistersForRegList( 
                                    registerList,
                                    proto,
                                    1 );
                                offsetResolved = true;
                            } 
                        }
                    }
                }
                if( !offsetResolved ) {
                    instrText.append( "vtable #0x"+
                            Integer.toHexString( vtableOffset ) );
                    affectedRegisters = new int[ registerList.size() ];
                    for( int i = 0 ; i < registerList.size() ; ++i )
                        affectedRegisters[i] = 
                                    registerList.get( i ).intValue();
                }
            }
            break;

            case INLINEMETHODINVOKE: {
                int b2 = read8Bit();
                int regno = (  b2 & 0xF0 ) >> 4;
                int origRegNo = regno;
// If invocation regno % 4 == 1 and regno > 4, the last invocation register 
// index is encoded on the lowest 4 bit of the regno byte
                int lastreg = -1;
                ArrayList<Integer> registerList = new ArrayList<Integer>();
                if( ( regno > 4 ) && ( regno % 4 ) == 1 ) {
                    --regno;
                    lastreg = b2 & 0x0F;
                }
                int inlineOffset = read16Bit();
                instrText.append( "{" );
                int regByte = 0;
                int byteCounter = 0;
                for( int i = 0 ; i < regno ; ++i ) {
                    if( i > 0 )
                        instrText.append( "," );
                    int reg = 0;
                    if( ( i % 2 ) == 0 ) {
                        regByte = read8Bit();
                        ++byteCounter;
                        reg = regByte & 0x0F;
                    } else
                        reg = ( regByte & 0xF0 ) >> 4;
                    instrText.append( "v"+reg );
                    registerList.add( new Integer( reg ) );
                }
                if( lastreg >= 0 ) {
                    instrText.append( "v"+lastreg );
                    registerList.add( new Integer( lastreg ) );
                }
                if( ( byteCounter % 2 ) != 0 )
                    read8Bit();         // Align to 16 bit
                instrText.append( "}," );
                boolean offsetResolved = false;
                if( secondPass ) {
                        Long key = new Long( file.getFilePointer() );
                        String parameter = quickParameterMap.get( key );
                        if( parameter != null ) {
                            instrText.append( parameter );
                            offsetResolved = true;
                        }
                } else {
                        String methodProto = 
                            DexOffsetResolver
				.getInlineMethodNameFromIndex( 
					inlineOffset,
					dexSignatureBlock.getOptVersion() );
                        if( methodProto != null ) {
                            String proto = "";
                            int idx = methodProto.indexOf( ',' );
                            if( idx >= 0 ) {
                                proto = methodProto.substring( idx+1 );
                                methodProto = methodProto.substring( 0,idx );
                            }
                            String parameter = 
                                methodProto+"\t; "+proto+
                                " , inline #0x"+
                                Integer.toHexString( inlineOffset );
                            Long key = new Long( file.getFilePointer() );
                            quickParameterMap.put( key,parameter );
                            instrText.append( parameter );
                            String resultType = 
                                DexMethodIdsBlock.getResultType( proto );
                            if( "V".equals( resultType ) )
                                regMap.remove( REGMAP_RESULT_KEY );
                            else
                                regMap.put( REGMAP_RESULT_KEY,
                                    convertJavaTypeToInternal( resultType ) );
                            affectedRegisters = getAffectedRegistersForRegList( 
                                registerList,
                                proto,
                                1 );
                            offsetResolved = true;
                        }
                }
                if( !offsetResolved )
                    instrText.append( 
                            "inline #0x"+
                            Integer.toHexString( inlineOffset ) );
            }
            break;

            case FILLEDARRAY: {
                int b2 = read8Bit();
                int regno = (  b2 & 0xF0 ) >> 4;
// If invocation regno % 4 == 1 and regno > 4, the last invocation register 
// index is encoded on the lowest 4 bit of the regno byte
                int lastreg = -1;
                if( ( regno > 4 ) && ( regno % 4 ) == 1 ) {
                    --regno;
                    lastreg = b2 & 0x0F;
                }
                int typeidx = read16Bit();
                instrText.append( "{" );
                int regByte = 0;
                int byteCounter = 0;
                affectedRegisters = new int[ regno ];
                for( int i = 0 ; i < regno ; ++i ) {
                    if( i > 0 )
                        instrText.append( "," );
                    int reg = 0;
                    if( ( i % 2 ) == 0 ) {
                        regByte = read8Bit();
                        ++byteCounter;
                        reg = regByte & 0x0F;
                    } else
                        reg = ( regByte & 0xF0 ) >> 4;
                    instrText.append( "v"+reg );
                    affectedRegisters[ i ] = reg;
                }
                if( lastreg >= 0 ) {
                    instrText.append( "v"+lastreg );
                    affectedRegisters[ regno - 1 ] = lastreg;
                }
                if( ( byteCounter % 2 ) != 0 )
                    read8Bit();         // Align to 16 bit
                instrText.append( "}," );
                String arrayType = dexTypeIdsBlock.getType( typeidx );
                instrText.append( arrayType );
                regMap.put( REGMAP_RESULT_KEY,
                        convertJavaTypeToInternal( arrayType ) );
            }
            break;


// The instruction is followed by the number of registers to pass, encoded as
// one byte. Then comes the method index as a 16-bit word which is followed
// by the first register in the range as a 16-bit word
            case METHODINVOKE_RANGE: {
                int regno = read8Bit();
                int methodidx = read16Bit();
                int rangestart = read16Bit();
                int rangeend = rangestart + regno - 1;
                String proto = dexMethodIdsBlock.getProto( methodidx );
                String resultType = DexMethodIdsBlock.getResultType( proto );
                if( "V".equals( resultType ) )
                    regMap.remove( REGMAP_RESULT_KEY );
                else
                    regMap.put( REGMAP_RESULT_KEY,
                        convertJavaTypeToInternal( resultType ) );
                if( regno == 1 )
                    instrText.append( "{v"+
                                rangestart+
                                "}" );
                else
                    instrText.append( "{v"+
                                    rangestart+
                                    "..v"+
                                    rangeend+
                                    "}" );
                instrText.append( ","+
                                    dexMethodIdsBlock.getMethod( methodidx )+
                                    ";\t"+
                                    proto );
                affectedRegisters = 
                    getAffectedRegistersForRange( 
                        proto, rangestart,1 );
            }
            break;

            case METHODINVOKE_RANGE_STATIC: {
                int regno = read8Bit();
                int methodidx = read16Bit();
                int rangestart = read16Bit();
                int rangeend = rangestart + regno - 1;
                String proto = dexMethodIdsBlock.getProto( methodidx );
                String resultType = DexMethodIdsBlock.getResultType( proto );
                if( "V".equals( resultType ) )
                    regMap.remove( REGMAP_RESULT_KEY );
                else
                    regMap.put( REGMAP_RESULT_KEY,
                        convertJavaTypeToInternal( resultType ) );
                if( regno == 1 )
                    instrText.append( "{v"+
                                rangestart+
                                "}" );
                else
                    instrText.append( "{v"+
                                    rangestart+
                                    "..v"+
                                    rangeend+
                                    "}" );
                instrText.append( ","+
                                    dexMethodIdsBlock.getMethod( methodidx )+
                                    ";\t"+
                                    proto );
                affectedRegisters = 
                    getAffectedRegistersForRange( 
                        proto, rangestart,0 );
            }
            break;

            case QUICKMETHODINVOKE_RANGE: {
                int regno = read8Bit();
                int vtableOffset = read16Bit();
                int rangestart = read16Bit();
                int rangeend = rangestart + regno - 1;
                if( regno == 1 )
                    instrText.append( "{v"+
                                rangestart+
                                "}" );
                else
                    instrText.append( "{v"+
                                    rangestart+
                                    "..v"+
                                    rangeend+
                                    "}" );
                boolean offsetResolved = false;
// In the first pass, we resolve the parameter and save it into quickParameterMap. 
// In the second pass, we use the saved parameter to parse the instruction.
                if( dexOffsetResolver != null ) {
                    if( secondPass ) {
                        Long key = new Long( file.getFilePointer() );
                        String parameter = quickParameterMap.get( key );
                        if( parameter != null ) {
                            instrText.append( parameter );
                            offsetResolved = true;
                        }
                    } else {
                        String baseClass = null;
                        if( dexOffsetResolver != null ) {
                            baseClass = regMap.get( new Integer( rangestart ) );
                            if( baseClass != null ) {
                                baseClass = DexTypeIdsBlock.LTypeToJava( baseClass );
                                String methodProto = 
                                    dexOffsetResolver.getMethodNameFromOffset( 
                                        baseClass, vtableOffset );
                                if( methodProto != null ) {
                                    String proto = "";
                                    int idx = methodProto.indexOf( ',' );
                                    if( idx >= 0 ) {
                                        proto = methodProto.substring( idx+1 );
                                        methodProto = methodProto.substring( 0,idx );
                                    }
                                    String parameter = 
                                        baseClass+
                                        "/"+methodProto+
                                        "\t; "+proto+
                                        " , vtable #0x"+
                                        Integer.toHexString( vtableOffset );
                                    instrText.append( parameter );
                                    Long key = new Long( file.getFilePointer() );
                                    quickParameterMap.put( key,parameter );
                                    String resultType = 
                                        DexMethodIdsBlock.getResultType( proto );
                                    if( "V".equals( resultType ) )
                                        regMap.remove( REGMAP_RESULT_KEY );
                                    else
                                        regMap.put( REGMAP_RESULT_KEY,
                                            convertJavaTypeToInternal( resultType ) );
                                    affectedRegisters = 
                                        getAffectedRegistersForRange( 
                                            proto, rangestart,1 );
                                    offsetResolved = true;
                                } else {
// if all symbolic resolution is successful, this inital estimation of
// affected registers will be overwritten by another set derived from
// the method prototype. This is rather a debug measure - there is not much
// point tracing registers if the invoke-quick result types cannot be calculated.
                                    affectedRegisters = new int[ regno ];
                                    for( int i = 0 ; i < regno ; ++i )
                                        affectedRegisters[i] = rangestart+i;
                                }
                            }
                        }
                    }
                }
                if( !offsetResolved )
                    instrText.append( ",vtable #0x"+
                            Integer.toHexString( vtableOffset ) );
            }
            break;

            case INLINEMETHODINVOKE_RANGE: {
                int regno = read8Bit();
                int inlineOffset = read16Bit();
                int rangestart = read16Bit();
                int rangeend = rangestart + regno - 1;

                if( regno == 1 )
                    instrText.append( "{v"+
                                rangestart+
                                "}," );
                else
                    instrText.append( "{v"+
                                    rangestart+
                                    "..v"+
                                    rangeend+
                                    "}," );
                boolean offsetResolved = false;
                if( secondPass ) {
                        Long key = new Long( file.getFilePointer() );
                        String parameter = quickParameterMap.get( key );
                        if( parameter != null ) {
                            instrText.append( parameter );
                            offsetResolved = true;
                        }
                } else {
                        String methodProto = 
                           DexOffsetResolver
				.getInlineMethodNameFromIndex( 
					inlineOffset,
					dexSignatureBlock.getOptVersion() );
                        if( methodProto != null ) {
                            String proto = "";
                            int idx = methodProto.indexOf( ',' );
                            if( idx >= 0 ) {
                                proto = methodProto.substring( idx+1 );
                                methodProto = methodProto.substring( 0,idx );
                            }
                            String parameter = 
                                methodProto+"\t; "+proto+
                                " , inline #0x"+
                                Integer.toHexString( inlineOffset );
                            Long key = new Long( file.getFilePointer() );
                            quickParameterMap.put( key,parameter );
                            instrText.append( parameter );
                            String resultType = 
                                DexMethodIdsBlock.getResultType( proto );
                            if( "V".equals( resultType ) )
                                regMap.remove( REGMAP_RESULT_KEY );
                            else
                                regMap.put( REGMAP_RESULT_KEY,
                                    convertJavaTypeToInternal( resultType ) );
                	    affectedRegisters = 
                    		getAffectedRegistersForRange( 
                        		proto, rangestart,1 );
                            offsetResolved = true;
                        }
                }
                if( !offsetResolved )
                    instrText.append( 
                            "inline #0x"+
                            Integer.toHexString( inlineOffset ) );
            }
            break;

            case FILLEDARRAY_RANGE: {
                int regno = read8Bit();
                int typeidx = read16Bit();
                int rangestart = read16Bit();
                int rangeend = rangestart + regno - 1;
                if( regno == 1 )
                    instrText.append( "{v"+
                                rangestart+
                                "}" );
                else
                    instrText.append( "{v"+
                                    rangestart+
                                    "..v"+
                                    rangeend+
                                    "}" );
                affectedRegisters = new int[ regno ];
                for( int i = 0 ; i < regno ; ++i )
                    affectedRegisters[i] = rangestart + i;
                String arrayType = dexTypeIdsBlock.getType( typeidx );
                instrText.append( ","+arrayType );
                regMap.put( REGMAP_RESULT_KEY,
                        convertJavaTypeToInternal( arrayType ) );
            }
            break;

// The instruction is followed by one byte storing the target and size registers
// in lower and higher 4 bits then a 16-bit value is the type index
            case NEWARRAY: {
                int regs = read8Bit();
                int typeidx = read16Bit();
                int targetreg = regs & 0xF;
                int sizereg = ( regs & 0xF0 ) >> 4;
                String arrayType = dexTypeIdsBlock.getType( typeidx );
                regMap.put( new Integer( targetreg ),arrayType );
                affectedRegisters = new int[2];
                affectedRegisters[0] = targetreg;
                affectedRegisters[1] = sizereg;
                instrText.append( "v"+
                            targetreg+
                            ",v"+
                            sizereg+
                            ","+
                            arrayType );
            }
            break;

// The instruction is followed by a register and a 32-bit signed offset that
// points to the static array data used to fill the array
            case FILLARRAYDATA: {
                int reg = read8Bit();
                int offset = readSigned32Bit();
                long target = instrBase + ( (long)offset * 2L );
                instrText.append( "v"+
                        reg+
                        ","+
                        "l"+Long.toHexString( target ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                if( !secondPass ) {
                    FillArrayTask fillArrayTask = 
                        new FillArrayTask( this, instrBase, target );
                    tasks.add( fillArrayTask );
                }
                updateLowestDataBlock( target );
            }
            break;

// The instruction is followed by one byte storing a register index and a 
// field id index as a 16-bit value. The instruction reads that field into
// a single-length register
            case ONEREGFIELD_READ: {
                int reg = read8Bit();
                int fieldidx = read16Bit();
                instrText.append( "v"+reg+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),TYPE_SINGLE_LENGTH );
            }
            break;

// The instruction is followed by one byte storing a register index and a 
// field id index as a 16-bit value. The instruction reads that field into
// a double-length register
            case ONEREGFIELD_READ_WIDE: {
                int reg = read8Bit();
                int fieldidx = read16Bit();
                instrText.append( "v"+reg+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),TYPE_DOUBLE_LENGTH );
            }
            break;

// The instruction is followed by one byte storing a register index and a 
// field id index as a 16-bit value. The instruction reads that field into
// an object register
            case ONEREGFIELD_READ_OBJECT: {
                int reg = read8Bit();
                int fieldidx = read16Bit();
                instrText.append( "v"+reg+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),
                        dexFieldIdsBlock.getFieldType( fieldidx ) );
            }
            break;

// The instruction is followed by one byte storing a register index and a 
// field id index as a 16-bit value. The instruction writes that field from a
// register
            case ONEREGFIELD_WRITE: {
                int reg = read8Bit();
                int fieldidx = read16Bit();
                instrText.append( "v"+reg+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),
                        dexFieldIdsBlock.getFieldType( fieldidx ) );
            }
            break;

// The instruction is followed by one byte, storing two register indexes on
// the low and high 4 bits and a field id index as a 16-bit value. The instruction
// reads the value into a single-length register.
            case TWOREGSFIELD_READ: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                int fieldidx = read16Bit();
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                instrText.append( "v"+reg1+",v"+reg2+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
            }
            break;

// The instruction is followed by one byte, storing two register indexes on
// the low and high 4 bits and a field id index as a 16-bit value. The instruction
// reads the value into a double-length register.
            case TWOREGSFIELD_READ_WIDE: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                int fieldidx = read16Bit();
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                instrText.append( "v"+reg1+",v"+reg2+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                regMap.put( new Integer( reg1 ),TYPE_DOUBLE_LENGTH );
            }
            break;

// The instruction is followed by one byte, storing two register indexes on
// the low and high 4 bits and a field id index as a 16-bit value. The instruction
// reads the value into an object register.
            case TWOREGSFIELD_READ_OBJECT: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                int fieldidx = read16Bit();
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                instrText.append( "v"+reg1+",v"+reg2+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
                regMap.put( new Integer( reg1 ),
                                dexFieldIdsBlock.getFieldType( fieldidx ) );
            }
            break;

// The instruction is followed by one byte, storing two register indexes on
// the low and high 4 bits and a field id index as a 16-bit value. The instruction
// writes to a field from any type of register.
            case TWOREGSFIELD_WRITE: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                int fieldidx = read16Bit();
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                instrText.append( "v"+reg1+",v"+reg2+","+
                            dexFieldIdsBlock.getField( fieldidx ) );
            }
            break;


// The instruction is followed by a single byte to make it word-aligned.
            case NOPARAMETER: {
                int b = read8Bit();
            }
            break;

// The instruction is followed by 1 register index and a 16 bit constant. The instruction puts
// the single-length value into a register
            case REGCONST16: {
                int targetreg = read8Bit();
                int constant = read16Bit();
                affectedRegisters = new int[1];
                affectedRegisters[0] = targetreg;
                instrText.append( "v"+targetreg+","+
                                Integer.toString( constant ) );
                regMap.put( new Integer( targetreg ),TYPE_SINGLE_LENGTH );
            }
            break;

// The instruction is followed by 1 register index and a 16 bit constant. The instruction puts
// the double-length value into a register
            case REGCONST16_WIDE: {
                int targetreg = read8Bit();
                int constant = read16Bit();
                affectedRegisters = new int[1];
                affectedRegisters[0] = targetreg;
                instrText.append( "v"+targetreg+","+
                                Integer.toString( constant ) );
                regMap.put( new Integer( targetreg ),TYPE_DOUBLE_LENGTH );
            }
            break;


// The instruction is followed by 3 register indexes on 3 bytes
            case THREEREGS: {
                int reg1 = read8Bit();
                int reg2 = read8Bit();
                int reg3 = read8Bit();
                instrText.append( "v"+reg1+",v"+reg2+",v"+reg3 );
                affectedRegisters = new int[3];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                affectedRegisters[2] = reg3;
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
            }
            break;

// The instruction is followed by 3 register indexes on 3 bytes. The result is double-length
            case THREEREGS_WIDE: {
                int reg1 = read8Bit();
                int reg2 = read8Bit();
                int reg3 = read8Bit();
                instrText.append( "v"+reg1+",v"+reg2+",v"+reg3 );
                affectedRegisters = new int[3];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                affectedRegisters[2] = reg3;
                regMap.put( new Integer( reg1 ),TYPE_DOUBLE_LENGTH );
            }
            break;

// The instruction is followed by 3 register indexes on 3 bytes.  The second register is supposed
// to hold a reference to an array. The first register is updated with an element of an array
            case AGET: {
                int reg1 = read8Bit();
                int reg2 = read8Bit();
                int reg3 = read8Bit();
                instrText.append( "v"+reg1+",v"+reg2+",v"+reg3 );
                affectedRegisters = new int[3];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                affectedRegisters[2] = reg3;
                String arrayType = regMap.get( new Integer( reg2 ) );
                String elementType = TYPE_UNKNOWN;
                if( ( arrayType != null ) && arrayType.startsWith( "[" ) )
                    elementType = convertJavaTypeToInternal( arrayType.substring( 1 ) );
                regMap.put( new Integer( reg1 ),elementType );
            }
            break;

// The instruction is followed by 3 register indexes on 3 bytes.  The second register is supposed
// to hold a reference to an array. The content of the first register is put into the array
            case APUT: {
                int reg1 = read8Bit();
                int reg2 = read8Bit();
                int reg3 = read8Bit();
                instrText.append( "v"+reg1+",v"+reg2+",v"+reg3 );
                affectedRegisters = new int[3];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                affectedRegisters[2] = reg3;
            }
            break;


// The instruction is followed by a register index and a 32 bit signed offset pointing
// to a packed-switch table
            case PACKEDSWITCH: {
                int reg = read8Bit();
                int offset = readSigned32Bit();
                long target = instrBase + ( (long)offset * 2L );
                instrText.append( "v"+reg );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                generateText = false;   // the text will be generated by the task
                if( !secondPass ) {
                    PackedSwitchTask packedSwitchTask = 
                        new PackedSwitchTask( this, instrBase, target );
                    packedSwitchTask.setReg( reg );
                    tasks.add( packedSwitchTask );
                    forkData = packedSwitchTask.readJumpTable();
                    forkStatus = ForkStatus.FORK_AND_CONTINUE;
                }
                updateLowestDataBlock( target );
            }
            break;

// The instruction is followed by a register index and a 32 bit signed offset pointing
// to a sparse-switch table
            case SPARSESWITCH: {
                int reg = read8Bit();
                int offset = readSigned32Bit();
                long target = instrBase + ( (long)offset * 2L );
                instrText.append( "v"+reg );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                generateText = false;   // the text will be generated by the task
                if( !secondPass ) {
                    SparseSwitchTask sparseSwitchTask = 
                        new SparseSwitchTask( this, instrBase, target );
                    sparseSwitchTask.setReg( reg );
                    tasks.add( sparseSwitchTask );
                    forkData = sparseSwitchTask.readJumpTable();
                    forkStatus = ForkStatus.FORK_AND_CONTINUE;
                }
                updateLowestDataBlock( target );
            }
            break;


// The instruction is followed by one register index and moves the result into that
// one register
            case MOVERESULT: {
                int reg = read8Bit();
                instrText.append( "v"+reg );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),regMap.get( REGMAP_RESULT_KEY ) );
            }
            break;


// The instruction is followed by one register index
            case ONEREG: {
                int reg = read8Bit();
                instrText.append( "v"+reg );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
            }
            break;

// The instruction is followed by a 8-bit signed offset
            case OFFSET8: {
                long target = calculateTarget( instrBase );
                instrText.append( labelForAddress( target ) );
                forkData = new long[1];
                forkData[0] = target;
                forkStatus = ForkStatus.FORK_UNCONDITIONALLY;
            }
            break;

// Checks whether a reference in a certain register can be casted to a certain
// type. As a side effect, the type of the value in the register will be changed
// to that of the check cast type.
            case CHECKCAST: {
                int reg = read8Bit();
                int typeidx = read16Bit();
                String castType = dexTypeIdsBlock.getClassName( typeidx );
                instrText.append( "v"+reg+
                                ","+castType );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                if( !castType.startsWith( "[" ) )
                    castType = "L" + castType + ";";
                regMap.put( new Integer( reg ),castType );
            }
            break;


// The instruction is followed by one register index byte, then a 
// 16 bit type index follows. The register is associated with that type
            case NEWINSTANCE: {
                int reg = read8Bit();
                int typeidx = read16Bit();
                String type = dexTypeIdsBlock.getClassName( typeidx );
                instrText.append( "v"+reg+
                                ","+type );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),"L"+type+";" );
            }
            break;

// The instruction is followed by one byte with two register indexes on the
// high and low 4-bits. Then a 16 bit type index follows.
            case TWOREGSTYPE: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                int typeidx = read16Bit();
                instrText.append( "v"+reg1+
                                ",v"+reg2+
                                ","+dexTypeIdsBlock.getClassName( typeidx ) );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
            }
            break;

// The instruction is followed by one byte with register index and one signed
// 16 bit offset
            case REGOFFSET16: {
                int reg = read8Bit();
                long target = calculateTarget16Bit( instrBase );
                instrText.append( "v"+reg+
                    ","+labelForAddress( target ) );
                forkData = new long[1];
                forkData[0] = target;
                forkStatus = ForkStatus.FORK_AND_CONTINUE;
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
            }
            break;

// The instruction is followed by one padding byte and one signed
// 16 bit offset
            case OFFSET16: {
                int padding = read8Bit();
                long target = calculateTarget16Bit( instrBase );
                instrText.append( labelForAddress( target ) );
                forkData = new long[1];
                forkData[0] = target;
                forkStatus = ForkStatus.FORK_UNCONDITIONALLY;
            }
            break;


// The instruction is followed by one byte with two register indexes on the high and low
// 4 bits and one signed 16 bit offset
            case TWOREGSOFFSET16: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                long target = calculateTarget16Bit( instrBase );
                instrText.append( "v"+reg1+",v"+reg2+
                    ","+labelForAddress( target ) );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                forkData = new long[1];
                forkData[0] = target;
                forkStatus = ForkStatus.FORK_AND_CONTINUE;
            }
            break;

// One byte follows the instruction, two register indexes on the high and low 4 bits. The second
// register overwrites the first
            case MOVE: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                instrText.append( "v"+reg1+",v"+reg2 );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),regMap.get( new Integer( reg2 ) ) );
            }
            break;

// One byte follows the instruction, two register indexes on the high and low 4 bits. The second
// register overwrites the first
            case MOVE_OBJECT: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                instrText.append( "v"+reg1+",v"+reg2 );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),getRegType( instrBase,reg2 ) );
            }
            break;


// One byte follows the instruction, two register indexes on the high and low 4 bits. The
// first register will hold a single-length value
            case TWOREGSPACKED_SINGLE: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                instrText.append( "v"+reg1+",v"+reg2 );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
            }
            break;


// One byte follows the instruction, two register indexes on the high and low 4 bits.
            case TWOREGSPACKED_DOUBLE: {
                int b1 = read8Bit();
                int reg1 = b1 & 0xF;
                int reg2 = ( b1 & 0xF0 ) >> 4;
                instrText.append( "v"+reg1+",v"+reg2 );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_DOUBLE_LENGTH );
            }
            break;

// The instruction is followed by two 8-bit register indexes and one 8-bit
// literal constant.
            case TWOREGSCONST8: {
                int reg1 = read8Bit();
                int reg2 = read8Bit();
                int constant = read8Bit();
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
                instrText.append( "v"+reg1+",v"+reg2+","+constant );
            }
            break;

            case REGCLASSCONST: {
                int reg = read8Bit();
                int typeidx = read16Bit();
                String type = dexTypeIdsBlock.getClassName( typeidx );
                instrText.append( "v"+
                                    reg+
                                    ","+
                                    type );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),"Ljava/lang/Class;" );
            }
            break;

            case REGCONST32: {
                int reg = read8Bit();
                long constant = read32Bit();
                instrText.append( "v"+
                                    reg+
                                    ","+
                                    constant+
                                    "\t; 0x"+
                                    Long.toHexString( constant ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),TYPE_SINGLE_LENGTH );
            }
            break;

            case REGCONST32_WIDE: {
                int reg = read8Bit();
                long constant = read32Bit();
                instrText.append( "v"+
                                    reg+
                                    ","+
                                    constant+
                                    "\t; 0x"+
                                    Long.toHexString( constant ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),TYPE_DOUBLE_LENGTH );
            }
            break;

            case REGCONST64: {
                int reg = read8Bit();
                long const1 = read32Bit();
                long const2 = read32Bit();
                long constant = const2 << 32 | const1;
                instrText.append( "v"+
                                    reg+
                                    ","+
                                    constant+
                                    "\t; 0x"+
                                    Long.toHexString( constant ) );
                affectedRegisters = new int[1];
                affectedRegisters[0] = reg;
                regMap.put( new Integer( reg ),TYPE_DOUBLE_LENGTH );
            }
            break;

            case REG8REG16: {
                int reg1 = read8Bit();
                int reg2 = read16Bit();
                instrText.append( "v"+
                                    reg1+
                                    ",v"+
                                    reg2 );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),regMap.get( new Integer( reg2 ) ) );
            }
            break;

            case REG8REG16_OBJECT: {
                int reg1 = read8Bit();
                int reg2 = read16Bit();
                instrText.append( "v"+
                                    reg1+
                                    ",v"+
                                    reg2 );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),getRegType( instrBase,reg2 ) );
            }
            break;


            case TWOREGSPACKEDCONST16: {
                int reg = read8Bit();
                int reg1 = reg & 0xF;
                int reg2 = ( reg & 0xF0 ) >> 4;
                int constant = read16Bit();
                instrText.append( "v"+reg1+
                                    ",v"+reg2+
                                    ","+constant );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
            }
            break;

// Reads a single-length field into register using quick access
            case TWOREGSQUICKOFFSET: {
                int reg = read8Bit();
                int reg1 = reg & 0xF;
                int reg2 = ( reg & 0xF0 ) >> 4;
                int constant = read16Bit();
                String baseClass = null;
                if( dexOffsetResolver != null )
                    baseClass = regMap.get( new Integer( reg2 ) );
                if( baseClass != null )
                    baseClass = DexTypeIdsBlock.LTypeToJava( baseClass );
                instrText.append( "v"+reg1+
                                    ",v"+reg2+
                                    "," );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_SINGLE_LENGTH );
                boolean offsetResolved = false;
// If in the first pass, we try to resolve the vtable offset and store the result
// in quickParameterMap. In the second pass, we use the resolved parameters to
// finally parse the instruction.
                if( secondPass ) {
                    Long key = new Long( file.getFilePointer() );
                    String parameter = quickParameterMap.get( key );
                    if( parameter != null ) {
                        instrText.append( parameter );
                        offsetResolved = true;
                    }
                } else {
// First pass. Try to resolve the field offset and store it if successful for the
// second pass.
// The base class register was tracked - we may even be able to resolve
// the vtable offset 
                    if( baseClass != null ) {
                        String fieldName = 
                            dexOffsetResolver.getFieldNameFromOffset( 
                                baseClass, constant );
                        if( fieldName != null ) {
                            Long key = new Long( file.getFilePointer() );
                            fieldName += "\t;[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]";
                            quickParameterMap.put( key,fieldName );
                            instrText.append( fieldName );
                            offsetResolved = true;
                        }
                    }
                }
                if( !offsetResolved )
                    instrText.append( "[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]" );
            }
            break;

// Reads a double-length field into register using quick access
            case TWOREGSQUICKOFFSET_WIDE: {
                int reg = read8Bit();
                int reg1 = reg & 0xF;
                int reg2 = ( reg & 0xF0 ) >> 4;
                int constant = read16Bit();
                String baseClass = null;
                if( dexOffsetResolver != null )
                    baseClass = regMap.get( new Integer( reg2 ) );
                if( baseClass != null )
                    baseClass = DexTypeIdsBlock.LTypeToJava( baseClass );
                instrText.append( "v"+reg1+
                                    ",v"+reg2+
                                    "," );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),TYPE_DOUBLE_LENGTH );
                boolean offsetResolved = false;
// If in the first pass, we try to resolve the vtable offset and store the result
// in quickParameterMap. In the second pass, we use the resolved parameters to
// finally parse the instruction.
                if( secondPass ) {
                    Long key = new Long( file.getFilePointer() );
                    String parameter = quickParameterMap.get( key );
                    if( parameter != null ) {
                        instrText.append( parameter );
                        offsetResolved = true;
                    }
                } else {
// First pass. Try to resolve the field offset and store it if successful for the
// second pass.
// The base class register was tracked - we may even be able to resolve
// the vtable offset 
                    if( baseClass != null ) {
                        String fieldName = 
                            dexOffsetResolver.getFieldNameFromOffset( 
                                baseClass, constant );
                        if( fieldName != null ) {
                            Long key = new Long( file.getFilePointer() );
                            fieldName += "\t;[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]";
                            quickParameterMap.put( key,fieldName );
                            instrText.append( fieldName );
                            offsetResolved = true;
                        }
                    }
                }
                if( !offsetResolved )
                    instrText.append( "[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]" );
            }
            break;

// Writes an object field into register using quick access
            case TWOREGSQUICKOFFSET_OBJECT: {
                int reg = read8Bit();
                int reg1 = reg & 0xF;
                int reg2 = ( reg & 0xF0 ) >> 4;
                int constant = read16Bit();
                String baseClass = null;
                if( dexOffsetResolver != null )
                    baseClass = regMap.get( new Integer( reg2 ) );
                if( baseClass != null )
                    baseClass = DexTypeIdsBlock.LTypeToJava( baseClass );
                instrText.append( "v"+reg1+
                                    ",v"+reg2+
                                    "," );
                boolean offsetResolved = false;
                String resultType = "L<unknown>;";
// If in the first pass, we try to resolve the vtable offset and store the result
// in quickParameterMap. In the second pass, we use the resolved parameters to
// finally parse the instruction.
                if( secondPass ) {
                    Long key = new Long( file.getFilePointer() );
                    String parameter = quickParameterMap.get( key );
                    if( parameter != null ) {
                        instrText.append( parameter );
                        offsetResolved = true;
                    }
                } else {
// First pass. Try to resolve the field offset and store it if successful for the
// second pass.
// The base class register was tracked - we may even be able to resolve
// the vtable offset 
                    if( baseClass != null ) {
                        String fieldName = 
                            dexOffsetResolver.getFieldNameFromOffset( 
                                baseClass, constant );
                        if( fieldName != null ) {
                            int idx = fieldName.indexOf( ' ' );
                            if( idx >= 0 );
                                resultType = fieldName.substring( idx+1 );
                            fieldName += "\t;[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]";
                            Long key = new Long( file.getFilePointer() );
                            quickParameterMap.put( key,fieldName );
                            instrText.append( fieldName );
                            offsetResolved = true;
                        }
                    }
                }
                if( !offsetResolved )
                    instrText.append( "[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]" );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                regMap.put( new Integer( reg1 ),resultType );
            }
            break;

// Writes an object field from a register using quick access
            case TWOREGSQUICKOFFSET_WRITE: {
                int reg = read8Bit();
                int reg1 = reg & 0xF;
                int reg2 = ( reg & 0xF0 ) >> 4;
                int constant = read16Bit();
                String baseClass = null;
                if( dexOffsetResolver != null )
                    baseClass = regMap.get( new Integer( reg2 ) );
                if( baseClass != null )
                    baseClass = DexTypeIdsBlock.LTypeToJava( baseClass );
                instrText.append( "v"+reg1+
                                    ",v"+reg2+
                                    "," );
                affectedRegisters = new int[2];
                affectedRegisters[0] = reg1;
                affectedRegisters[1] = reg2;
                boolean offsetResolved = false;
// If in the first pass, we try to resolve the vtable offset and store the result
// in quickParameterMap. In the second pass, we use the resolved parameters to
// finally parse the instruction.
                if( secondPass ) {
                    Long key = new Long( file.getFilePointer() );
                    String parameter = quickParameterMap.get( key );
                    if( parameter != null ) {
                        instrText.append( parameter );
                        offsetResolved = true;
                    }
                } else {
// First pass. Try to resolve the field offset and store it if successful for the
// second pass.
// The base class register was tracked - we may even be able to resolve
// the vtable offset 
                    if( baseClass != null ) {
                        String fieldName = 
                            dexOffsetResolver.getFieldNameFromOffset( 
                                baseClass, constant );
                        if( fieldName != null ) {
                            Long key = new Long( file.getFilePointer() );
                            fieldName += "\t;[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]";
                            quickParameterMap.put( key,fieldName );
                            instrText.append( fieldName );
                            offsetResolved = true;
                        }
                    }
                }
                if( !offsetResolved )
                    instrText.append( "[obj+0x"+
                                    Integer.toHexString( constant )+
                                    "]" );
            }
            break;

        }
        if( generateText )
            dump( "\t"+instrText );
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

    public void setDexOffsetResolver( DexOffsetResolver dexOffsetResolver ) {
        this.dexOffsetResolver = dexOffsetResolver;
    }

    public void setDexMethodIdsBlock( DexMethodIdsBlock dexMethodIdsBlock ) {
        this.dexMethodIdsBlock = dexMethodIdsBlock;
    }

    public void setPass( boolean secondPass ) {
        this.secondPass = secondPass;
    }

    public void setRegTraces( ArrayList<RegisterTraces> regTraces ) {
        this.regTraces = regTraces;
    }

/**
  * Returns the task associated to the address or null if the address
  * has no associated tasks.
  * @param address The address to check for associated tasks.
  * @return The task associated to the address or null if there is no association.
  */
    public DedexerTask getTaskForAddress( long address ) {
        return labels.get( new Long( address ) );
    }

/**
  * Processes the task queue after the execution of a task
  */
    public void postPassProcessing( boolean secondPass ) throws IOException {
        for( int i = 0 ; i < tasks.size() ; ++i ) {
            DedexerTask task = tasks.get( i );
            task.doTask( secondPass );
        }
    }

/**
  * Sets the code generator used by code generation subtasks
  */
    public void setCodeGenerator( CodeGenerator codeGenerator ) {
        this.codeGenerator = codeGenerator;
    }

/**
  * Gets the code generator used by code generation subtasks
  */
    public CodeGenerator getCodeGenerator() {
        return codeGenerator;
    }

    public long getLowestDataBlock() {
        return lowestDataBlock;
    }

    public static String labelForAddress( long address ) {
        return "l"+Long.toHexString( address );
    }

/**
  * Places a task to a certain location. If the location has no 
  * associated task, the task is simply associated with the location. If, however,
  * the location has associated task, it is first turned into a TaskCollection or
  * if it is already a TaskCollection, the new task is added to the collection.
  */
    public void placeTask( long target, DedexerTask label ) {
        Long key = new Long( target );
        DedexerTask existingTask = labels.get( key );
        if( existingTask == null )
            labels.put( key,label );
        else {
            if( !( existingTask instanceof TaskCollection ) ) {
                existingTask = new TaskCollection( this, existingTask );
                labels.put( key,existingTask );
            }
            TaskCollection taskCollection = (TaskCollection)existingTask;
            taskCollection.addTask( label );
        }
    }

/**
  * Places a label at a certain location. Duplicate label filtering is provided.
  */
    public void placeLabel( long target, String labelName ) {
        if( secondPass )
            return;
        Long key = new Long( target );
        DedexerTask existingTask = labels.get( key );
        if( ( existingTask != null ) && existingTask.equals( labelName ) )
            return;
        LabelTask labelTask = new LabelTask( this, labelName );
        placeTask( target, labelTask );
    }

    public ForkStatus getForkStatus() {
        return forkStatus;
    }

    public long[] getForkData() {
        return forkData;
    }

/**
  * Returns the current register map. This maps register numbers to types in the registers.
  * @return the current register map
  */
    public HashMap<Integer,String> getRegisterMap() {
        return regMap;
    }

/**
  * Sets the register map. This is used to initialize/restore the map e.g. after branching.
  * @param regMap The register map to set.
  */
    public void setRegisterMap( HashMap<Integer,String> regMap ) {
        this.regMap = regMap;
    }

/**
  * Returns the registers that the last parsed instruction used/modified.
  * @return Array with the numbers of the registers in it or null if no registers were affected.
  */
    public int[] getAffectedRegisters() {
        return affectedRegisters;
    }

/**
  * Converts the Java types into an internal representation. The tracer does not follow whether 
  * a register is e.g. integer, short, float, it differentiates only between single-length, 
  * double-length values and (exact) object types.
  */
    public static String convertJavaTypeToInternal( String javaType ) {
        if( javaType.startsWith( "L" ) )
            return javaType;
        if( javaType.startsWith( "[" ) )
            return javaType;
        String internalType = "";
        switch( javaType.charAt( 0 ) ) {
            case 'J':
            case 'D':
                internalType = TYPE_DOUBLE_LENGTH;
                break;

            default:
                internalType = TYPE_SINGLE_LENGTH;
                break;
        }
        return internalType;
    }

    private DexSignatureBlock	dexSignatureBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexTypeIdsBlock     dexTypeIdsBlock = null;
    private DexFieldIdsBlock    dexFieldIdsBlock = null;
    private DexMethodIdsBlock   dexMethodIdsBlock = null;
    private DexOffsetResolver   dexOffsetResolver = null;

    private enum InstructionType {
            UNKNOWN_INSTRUCTION,
            REGCONST4,
            REGSTRINGCONST,
	    REGSTRINGCONST_JUMBO,
            METHODINVOKE,
            METHODINVOKE_STATIC,
            QUICKMETHODINVOKE,
            INLINEMETHODINVOKE,
            INLINEMETHODINVOKE_RANGE,
            NEWARRAY,
            FILLARRAYDATA,
            ONEREGFIELD_READ,
            ONEREGFIELD_READ_WIDE,
            ONEREGFIELD_READ_OBJECT,
            ONEREGFIELD_WRITE,
            TWOREGSFIELD_READ,
            TWOREGSFIELD_READ_WIDE,
            TWOREGSFIELD_READ_OBJECT,
            TWOREGSFIELD_WRITE,
            NOPARAMETER,
            REGCONST16,
            REGCONST16_WIDE,
            THREEREGS,
            THREEREGS_WIDE,
            AGET,
            APUT,
            PACKEDSWITCH,
            SPARSESWITCH,
            ONEREG,
            MOVERESULT,
            OFFSET8,
            NEWINSTANCE,
            TWOREGSTYPE,
            REGOFFSET16,
            OFFSET16,
            TWOREGSOFFSET16,
            MOVE,
            MOVE_OBJECT,
            TWOREGSPACKED_SINGLE,
            TWOREGSPACKED_DOUBLE,
            TWOREGSCONST8,
            REGCLASSCONST,
            REGCONST32,
            REGCONST32_WIDE,
            REGCONST64,
            REG8REG16,
            REG8REG16_OBJECT,
            TWOREGSPACKEDCONST16,
            METHODINVOKE_RANGE,
            METHODINVOKE_RANGE_STATIC,
            QUICKMETHODINVOKE_RANGE,
            FILLEDARRAY,
            FILLEDARRAY_RANGE,
            TWOREGSQUICKOFFSET,
            TWOREGSQUICKOFFSET_WIDE,
            TWOREGSQUICKOFFSET_OBJECT,
            TWOREGSQUICKOFFSET_WRITE,
            CHECKCAST
    }

    private InstructionType instructionTypes[] = {
			InstructionType.NOPARAMETER,        	// 0
			InstructionType.MOVE,       	        // 1
			InstructionType.REG8REG16,	            // 2
			InstructionType.UNKNOWN_INSTRUCTION,	// 3
			InstructionType.MOVE,       	        // 4
			InstructionType.REG8REG16,	            // 5
			InstructionType.UNKNOWN_INSTRUCTION,	// 6
			InstructionType.MOVE_OBJECT,	        // 7
			InstructionType.REG8REG16_OBJECT,	    // 8
			InstructionType.UNKNOWN_INSTRUCTION,	// 9
			InstructionType.MOVERESULT,             // a
			InstructionType.MOVERESULT,	            // b
			InstructionType.MOVERESULT,	            // c
			InstructionType.MOVERESULT,	            // d
			InstructionType.NOPARAMETER,	        // e
			InstructionType.ONEREG,	                // f
			InstructionType.ONEREG,               	// 10
			InstructionType.ONEREG,             	// 11
			InstructionType.REGCONST4,	            // 12
			InstructionType.REGCONST16,	            // 13
			InstructionType.REGCONST32,	            // 14
			InstructionType.REGCONST16,	            // 15
			InstructionType.REGCONST16_WIDE,	    // 16
			InstructionType.REGCONST32_WIDE,	    // 17
			InstructionType.REGCONST64,	            // 18
			InstructionType.REGCONST16_WIDE,        // 19
			InstructionType.REGSTRINGCONST,     	// 1a
			InstructionType.REGSTRINGCONST_JUMBO,	// 1b
			InstructionType.REGCLASSCONST,	        // 1c
			InstructionType.ONEREG,             	// 1d
			InstructionType.ONEREG,             	// 1e
			InstructionType.CHECKCAST,	            // 1f
			InstructionType.TWOREGSTYPE,        	// 20
			InstructionType.TWOREGSPACKED_SINGLE,   // 21
			InstructionType.NEWINSTANCE,	        // 22
			InstructionType.NEWARRAY,           	// 23
			InstructionType.FILLEDARRAY,	        // 24
			InstructionType.FILLEDARRAY_RANGE,	    // 25
			InstructionType.FILLARRAYDATA,	        // 26
			InstructionType.ONEREG,	                // 27
			InstructionType.OFFSET8,            	// 28
			InstructionType.OFFSET16,	            // 29
			InstructionType.UNKNOWN_INSTRUCTION,	// 2a
			InstructionType.PACKEDSWITCH,       	// 2b
			InstructionType.SPARSESWITCH,       	// 2c
			InstructionType.THREEREGS,	            // 2d
			InstructionType.THREEREGS,	            // 2e
			InstructionType.THREEREGS,	            // 2f
			InstructionType.THREEREGS,	            // 30
			InstructionType.THREEREGS,	            // 31
			InstructionType.TWOREGSOFFSET16,	    // 32
			InstructionType.TWOREGSOFFSET16,	    // 33
			InstructionType.TWOREGSOFFSET16,	    // 34
			InstructionType.TWOREGSOFFSET16,	    // 35
			InstructionType.TWOREGSOFFSET16,	    // 36
			InstructionType.TWOREGSOFFSET16,	    // 37
			InstructionType.REGOFFSET16,	// 38
			InstructionType.REGOFFSET16,	// 39
			InstructionType.REGOFFSET16,	// 3a
			InstructionType.REGOFFSET16,	// 3b
			InstructionType.REGOFFSET16,	// 3c
			InstructionType.REGOFFSET16,	// 3d
			InstructionType.UNKNOWN_INSTRUCTION,	// 3e
			InstructionType.UNKNOWN_INSTRUCTION,	// 3f
			InstructionType.UNKNOWN_INSTRUCTION,	// 40
			InstructionType.UNKNOWN_INSTRUCTION,	// 41
			InstructionType.UNKNOWN_INSTRUCTION,	// 42
			InstructionType.UNKNOWN_INSTRUCTION,	// 43
			InstructionType.AGET,      	// 44
			InstructionType.AGET,   	// 45
			InstructionType.AGET,   	// 46
			InstructionType.AGET,   	// 47
			InstructionType.AGET,   	// 48
			InstructionType.AGET,   	// 49
			InstructionType.AGET,   	// 4a
			InstructionType.APUT,   	// 4b
			InstructionType.APUT,   	// 4c
			InstructionType.APUT,   	// 4d
			InstructionType.APUT,       // 4e
			InstructionType.APUT,   	// 4f
			InstructionType.APUT,   	// 50
			InstructionType.APUT,   	// 51
			InstructionType.TWOREGSFIELD_READ,       	// 52
			InstructionType.TWOREGSFIELD_READ_WIDE,    // 53
			InstructionType.TWOREGSFIELD_READ_OBJECT,   // 54
			InstructionType.TWOREGSFIELD_READ,       	// 55
			InstructionType.TWOREGSFIELD_READ,       	// 56
			InstructionType.TWOREGSFIELD_READ,       	// 57
			InstructionType.TWOREGSFIELD_READ,       	// 58
			InstructionType.TWOREGSFIELD_WRITE,       	// 59
			InstructionType.TWOREGSFIELD_WRITE,       	// 5a
			InstructionType.TWOREGSFIELD_WRITE,	        // 5b
			InstructionType.TWOREGSFIELD_WRITE,       	// 5c
			InstructionType.TWOREGSFIELD_WRITE,       	// 5d
			InstructionType.TWOREGSFIELD_WRITE,       	// 5e
			InstructionType.TWOREGSFIELD_WRITE,	        // 5f
			InstructionType.ONEREGFIELD_READ,       // 60
			InstructionType.ONEREGFIELD_READ_WIDE,	// 61
			InstructionType.ONEREGFIELD_READ_OBJECT,	// 62
			InstructionType.ONEREGFIELD_READ,	// 63
			InstructionType.ONEREGFIELD_READ,	// 64
			InstructionType.ONEREGFIELD_READ,	// 65
			InstructionType.ONEREGFIELD_READ,	// 66
			InstructionType.ONEREGFIELD_WRITE,	// 67
			InstructionType.ONEREGFIELD_WRITE,	// 68
			InstructionType.ONEREGFIELD_WRITE,	// 69
			InstructionType.ONEREGFIELD_WRITE,	// 6a
			InstructionType.ONEREGFIELD_WRITE,	// 6b
			InstructionType.ONEREGFIELD_WRITE,	// 6c
			InstructionType.ONEREGFIELD_WRITE,	// 6d
			InstructionType.METHODINVOKE,   // 6e
			InstructionType.METHODINVOKE,   // 6f
			InstructionType.METHODINVOKE,	        // 70
			InstructionType.METHODINVOKE_STATIC,    // 71
			InstructionType.METHODINVOKE,	        // 72
			InstructionType.UNKNOWN_INSTRUCTION,	// 73
			InstructionType.METHODINVOKE_RANGE,	    // 74
			InstructionType.METHODINVOKE_RANGE,	    // 75
			InstructionType.METHODINVOKE_RANGE, 	// 76
			InstructionType.METHODINVOKE_RANGE_STATIC,	    // 77
			InstructionType.METHODINVOKE_RANGE,	    // 78
			InstructionType.UNKNOWN_INSTRUCTION,	// 79
			InstructionType.UNKNOWN_INSTRUCTION,	// 7a
			InstructionType.TWOREGSPACKED_SINGLE,   // 7b
			InstructionType.UNKNOWN_INSTRUCTION,	// 7c
			InstructionType.TWOREGSPACKED_DOUBLE,   // 7d
			InstructionType.UNKNOWN_INSTRUCTION,	// 7e
			InstructionType.TWOREGSPACKED_SINGLE,   // 7f
			InstructionType.TWOREGSPACKED_DOUBLE,   // 80
			InstructionType.TWOREGSPACKED_DOUBLE,   // 81
			InstructionType.TWOREGSPACKED_SINGLE,	// 82
			InstructionType.TWOREGSPACKED_DOUBLE,	// 83
			InstructionType.TWOREGSPACKED_SINGLE,	// 84
			InstructionType.TWOREGSPACKED_SINGLE,	// 85
			InstructionType.TWOREGSPACKED_DOUBLE,	// 86
			InstructionType.TWOREGSPACKED_SINGLE,	// 87
			InstructionType.TWOREGSPACKED_DOUBLE,	// 88
			InstructionType.TWOREGSPACKED_DOUBLE,	// 89
			InstructionType.TWOREGSPACKED_SINGLE,	// 8a
			InstructionType.TWOREGSPACKED_DOUBLE,	// 8b
			InstructionType.TWOREGSPACKED_SINGLE,	// 8c
			InstructionType.TWOREGSPACKED_SINGLE,	// 8d
			InstructionType.TWOREGSPACKED_SINGLE,	// 8e
			InstructionType.TWOREGSPACKED_SINGLE,	// 8f
			InstructionType.THREEREGS,	// 90
			InstructionType.THREEREGS,	// 91
			InstructionType.THREEREGS,	// 92
			InstructionType.THREEREGS,	// 93
			InstructionType.THREEREGS,	// 94
			InstructionType.THREEREGS,	// 95
			InstructionType.THREEREGS,	// 96
			InstructionType.THREEREGS,	// 97
			InstructionType.THREEREGS,	// 98
			InstructionType.THREEREGS,	// 99
			InstructionType.THREEREGS,	// 9a
			InstructionType.THREEREGS_WIDE,	// 9b
			InstructionType.THREEREGS_WIDE,	// 9c
			InstructionType.THREEREGS_WIDE,	// 9d
			InstructionType.THREEREGS_WIDE,	// 9e
			InstructionType.THREEREGS_WIDE,	// 9f
			InstructionType.THREEREGS_WIDE,	// a0
			InstructionType.THREEREGS_WIDE,	// a1
			InstructionType.THREEREGS_WIDE,	// a2
			InstructionType.THREEREGS_WIDE,	// a3
			InstructionType.THREEREGS_WIDE,	// a4
			InstructionType.THREEREGS_WIDE,	// a5
			InstructionType.THREEREGS,	// a6
			InstructionType.THREEREGS,	// a7
			InstructionType.THREEREGS,	// a8
			InstructionType.THREEREGS,	// a9
			InstructionType.THREEREGS,	// aa
			InstructionType.THREEREGS_WIDE,	// ab
			InstructionType.THREEREGS_WIDE,	// ac
			InstructionType.THREEREGS_WIDE,	// ad
			InstructionType.THREEREGS_WIDE,	// ae
			InstructionType.THREEREGS_WIDE,	// af
			InstructionType.TWOREGSPACKED_SINGLE,	// b0
			InstructionType.TWOREGSPACKED_SINGLE,	// b1
			InstructionType.TWOREGSPACKED_SINGLE,	// b2
			InstructionType.TWOREGSPACKED_SINGLE,	// b3
			InstructionType.TWOREGSPACKED_SINGLE,	// b4
			InstructionType.TWOREGSPACKED_SINGLE,	// b5
			InstructionType.TWOREGSPACKED_SINGLE,	// b6
			InstructionType.TWOREGSPACKED_SINGLE,	// b7
			InstructionType.TWOREGSPACKED_SINGLE,	// b8
			InstructionType.TWOREGSPACKED_SINGLE,	// b9
			InstructionType.TWOREGSPACKED_SINGLE,	// ba
			InstructionType.TWOREGSPACKED_DOUBLE,	// bb
			InstructionType.TWOREGSPACKED_DOUBLE,	// bc
			InstructionType.TWOREGSPACKED_DOUBLE,	// bd
			InstructionType.TWOREGSPACKED_DOUBLE,	// be
			InstructionType.TWOREGSPACKED_DOUBLE,	// bf
			InstructionType.TWOREGSPACKED_DOUBLE,	// c0
			InstructionType.TWOREGSPACKED_DOUBLE,	// c1
			InstructionType.TWOREGSPACKED_DOUBLE,	// c2
			InstructionType.TWOREGSPACKED_DOUBLE,	// c3
			InstructionType.TWOREGSPACKED_DOUBLE,	// c4
			InstructionType.TWOREGSPACKED_DOUBLE,	// c5
			InstructionType.TWOREGSPACKED_SINGLE,	// c6
			InstructionType.TWOREGSPACKED_SINGLE,	// c7
			InstructionType.TWOREGSPACKED_SINGLE,	// c8
			InstructionType.TWOREGSPACKED_SINGLE,	// c9
			InstructionType.TWOREGSPACKED_SINGLE,	// ca
			InstructionType.TWOREGSPACKED_DOUBLE,	// cb
			InstructionType.TWOREGSPACKED_DOUBLE,	// cc
			InstructionType.TWOREGSPACKED_DOUBLE,	// cd
			InstructionType.TWOREGSPACKED_DOUBLE,	// ce
			InstructionType.TWOREGSPACKED_DOUBLE,	// cf
			InstructionType.TWOREGSPACKEDCONST16,	// d0
			InstructionType.TWOREGSPACKEDCONST16,	// d1
			InstructionType.TWOREGSPACKEDCONST16,	// d2
			InstructionType.TWOREGSPACKEDCONST16,	// d3
			InstructionType.TWOREGSPACKEDCONST16,	// d4
			InstructionType.TWOREGSPACKEDCONST16,	// d5
			InstructionType.TWOREGSPACKEDCONST16,	// d6
			InstructionType.TWOREGSPACKEDCONST16,	// d7
			InstructionType.TWOREGSCONST8,	// d8
			InstructionType.TWOREGSCONST8,	// d9
			InstructionType.TWOREGSCONST8,	// da
			InstructionType.TWOREGSCONST8,	// db
			InstructionType.TWOREGSCONST8,	// dc
			InstructionType.TWOREGSCONST8,	// dd
			InstructionType.TWOREGSCONST8,	// de
			InstructionType.TWOREGSCONST8,	// df
			InstructionType.TWOREGSCONST8,	// e0
			InstructionType.TWOREGSCONST8,	// e1
			InstructionType.TWOREGSCONST8,	// e2
			InstructionType.TWOREGSFIELD_READ,	// e3
			InstructionType.TWOREGSFIELD_WRITE,	// e4
			InstructionType.ONEREGFIELD_READ,	// e5
			InstructionType.ONEREGFIELD_WRITE,	// e6
			InstructionType.TWOREGSFIELD_READ_OBJECT,	// e7
			InstructionType.TWOREGSFIELD_READ_WIDE,	// e8
			InstructionType.TWOREGSFIELD_WRITE,	// e9
			InstructionType.ONEREGFIELD_READ_WIDE,	// ea
			InstructionType.ONEREGFIELD_WRITE,	// eb
			InstructionType.UNKNOWN_INSTRUCTION,	// ec
			InstructionType.UNKNOWN_INSTRUCTION,	// ed
			InstructionType.INLINEMETHODINVOKE,	    // ee
			InstructionType.INLINEMETHODINVOKE_RANGE,	// ef
			InstructionType.METHODINVOKE,	        // f0
			InstructionType.UNKNOWN_INSTRUCTION,	// f1
			InstructionType.TWOREGSQUICKOFFSET,	    // f2
			InstructionType.TWOREGSQUICKOFFSET_WIDE,	    // f3
			InstructionType.TWOREGSQUICKOFFSET_OBJECT,	    // f4
			InstructionType.TWOREGSQUICKOFFSET_WRITE,	    // f5
			InstructionType.TWOREGSQUICKOFFSET_WRITE,	    // f6
			InstructionType.TWOREGSQUICKOFFSET_WRITE,	    // f7
			InstructionType.QUICKMETHODINVOKE,	    // f8
			InstructionType.QUICKMETHODINVOKE_RANGE,	// f9
			InstructionType.QUICKMETHODINVOKE,	    // fa
			InstructionType.QUICKMETHODINVOKE_RANGE,	// fb
			InstructionType.TWOREGSFIELD_WRITE,	// fc
			InstructionType.ONEREGFIELD_READ_OBJECT,	// fd
			InstructionType.ONEREGFIELD_WRITE,	// fe
			InstructionType.UNKNOWN_INSTRUCTION 	// ff
    };

    String instructionNames[] = {
		"nop",	// 0
		"move",	// 1
		"move/from16",	// 2
		"",	// 3
		"move-wide",	// 4
		"move-wide/from16",	// 5
		"",	// 6
		"move-object",	// 7
		"move-object/from16",	// 8
		"",	// 9
		"move-result",	// a
		"move-result-wide",	// b
		"move-result-object",	// c
		"move-exception",	// d
		"return-void",	// e
		"return",	// f
		"return-wide",	// 10
		"return-object",	// 11
		"const/4",	// 12
		"const/16",	// 13
		"const",	// 14
		"const/high16",	// 15
		"const-wide/16",	// 16
		"const-wide/32",	// 17
		"const-wide",	// 18
		"const-wide/high16",	// 19
		"const-string",	// 1a
		"const-string/jumbo",	// 1b
		"const-class",	// 1c
		"monitor-enter",	// 1d
		"monitor-exit", 	// 1e
		"check-cast",	// 1f
		"instance-of",      // 20
		"array-length",	// 21
		"new-instance",	// 22
		"new-array",	// 23
		"filled-new-array",	// 24
		"filled-new-array/range",	// 25
		"fill-array-data",	// 26
		"throw",	// 27
		"goto",	// 28
		"goto/16",	// 29
		"",	// 2a
		"packed-switch",	// 2b
		"sparse-switch",	// 2c
		"cmpl-float",	// 2d
		"cmpg-float",	// 2e
		"cmpl-double",	// 2f
		"cmpg-double",	// 30
		"cmp-long",	// 31
		"if-eq",	// 32
		"if-ne",	// 33
		"if-lt",	// 34
		"if-ge",	// 35
		"if-gt",	// 36
		"if-le",	// 37
		"if-eqz",	// 38
		"if-nez",	// 39
		"if-ltz",	// 3a
		"if-gez",	// 3b
		"if-gtz",	// 3c
		"if-lez",	// 3d
		"",	// 3e
		"",	// 3f
		"",	// 40
		"",	// 41
		"",	// 42
		"",	// 43
		"aget",	// 44
		"aget-wide",	// 45
		"aget-object",	// 46
		"aget-boolean",	// 47
		"aget-byte",	// 48
		"aget-char",	// 49
		"aget-short",	// 4a
		"aput",	// 4b
		"aput-wide",	// 4c
		"aput-object",	// 4d
		"aput-boolean",	// 4e
		"aput-byte",	// 4f
		"aput-char",	// 50
		"aput-short",	// 51
		"iget",	// 52
		"iget-wide",	// 53
		"iget-object",	// 54
		"iget-boolean",	// 55
		"iget-byte",	// 56
		"iget-char",	// 57
		"iget-short",	// 58
		"iput",	// 59
		"iput-wide",	// 5a
		"iput-object",	// 5b
		"iput-boolean",	// 5c
		"iput-byte",	// 5d
		"iput-char",	// 5e
		"iput-short",	// 5f
		"sget",	// 60
		"sget-wide",	// 61
		"sget-object",	// 62
		"sget-boolean",	// 63
		"sget-byte",	// 64
		"sget-char",	// 65
		"sget-short",	// 66
		"sput",	// 67
		"sput-wide",	// 68
		"sput-object",	// 69
		"sput-boolean",	// 6a
		"sput-byte",	// 6b
		"sput-char",	// 6c
		"sput-short",	// 6d
		"invoke-virtual",	// 6e
		"invoke-super",	// 6f
		"invoke-direct",	    // 70
		"invoke-static",	    // 71
		"invoke-interface",	    // 72
		"",	// 73
		"invoke-virtual/range",	// 74
		"invoke-super/range",	// 75
		"invoke-direct/range",	// 76
		"invoke-static/range",	// 77
		"invoke-interface/range",	// 78
		"",	// 79
		"",	// 7a
		"neg-int",	// 7b
		"",	// 7c
		"neg-long",	// 7d
		"",	// 7e
		"neg-float",	// 7f
		"neg-double",	// 80
		"int-to-long",	// 81
		"int-to-float",	// 82
		"int-to-double",	// 83
		"long-to-int",	// 84
		"long-to-float",	// 85
		"long-to-double",	// 86
		"float-to-int",	// 87
		"float-to-long",	// 88
		"float-to-double",	// 89
		"double-to-int",	// 8a
		"double-to-long",	// 8b
		"double-to-float",	// 8c
		"int-to-byte",	// 8d
		"int-to-char",	// 8e
		"int-to-short",	// 8f
		"add-int",	// 90
		"sub-int",	// 91
		"mul-int",	// 92
		"div-int",	// 93
		"rem-int",	// 94
		"and-int",	// 95
		"or-int",	// 96
		"xor-int",	// 97
		"shl-int",	// 98
		"shr-int",	// 99
		"ushr-int",	// 9a
		"add-long",	// 9b
		"sub-long",	// 9c
		"mul-long",	// 9d
		"div-long",	// 9e
		"rem-long",	// 9f
		"and-long",	// a0
		"or-long",	// a1
		"xor-long",	// a2
		"shl-long",	// a3
		"shr-long",	// a4
		"ushr-long",	// a5
		"add-float",	// a6
		"sub-float",	// a7
		"mul-float",	// a8
		"div-float",	// a9
		"rem-float",	// aa
		"add-double",	// ab
		"sub-double",	// ac
		"mul-double",	// ad
		"div-double",	// ae
		"rem-double",	// af
		"add-int/2addr",	// b0
		"sub-int/2addr",	// b1
		"mul-int/2addr",	// b2
		"div-int/2addr",	// b3
		"rem-int/2addr",	// b4
		"and-int/2addr",	// b5
		"or-int/2addr",	// b6
		"xor-int/2addr",	// b7
		"shl-int/2addr",	// b8
		"shr-int/2addr",	// b9
		"ushr-int/2addr",	// ba
		"add-long/2addr",	// bb
		"sub-long/2addr",	// bc
		"mul-long/2addr",	// bd
		"div-long/2addr",	// be
		"rem-long/2addr",	// bf
		"and-long/2addr",	// c0
		"or-long/2addr",	// c1
		"xor-long/2addr",	// c2
		"shl-long/2addr",	// c3
		"shr-long/2addr",	// c4
		"ushr-long/2addr",	// c5
		"add-float/2addr",	// c6
		"sub-float/2addr",	// c7
		"mul-float/2addr",	// c8
		"div-float/2addr",	// c9
		"rem-float/2addr",	// ca
		"add-double/2addr",	// cb
		"sub-double/2addr",	// cc
		"mul-double/2addr",	// cd
		"div-double/2addr",	// ce
		"rem-double/2addr",	// cf
		"add-int/lit16",	// d0
		"sub-int/lit16",	// d1
		"mul-int/lit16",	// d2
		"div-int/lit16",	// d3
		"rem-int/lit16",	// d4
		"and-int/lit16",	// d5
		"or-int/lit16",	// d6
		"xor-int/lit16",	// d7
		"add-int/lit8",	// d8
		"sub-int/lit8",	// d9
		"mul-int/lit-8",	// da
		"div-int/lit8",	// db
		"rem-int/lit8",	// dc
		"and-int/lit8",	// dd
		"or-int/lit8",	// de
		"xor-int/lit8",	// df
		"shl-int/lit8",	// e0
		"shr-int/lit8",	// e1
		"ushr-int/lit8",	// e2
		"iget-volatile",	// e3
		"iput-volatile",	// e4
		"sget-volatile",	// e5
		"sput-volatile",	// e6
		"iget-object-volatile",	// e7
		"iget-wide-volatile",	// e8
		"iput-wide-volatile",	// e9
		"sget-wide-volatile",	// ea
		"sput-wide-volatile",	// eb
		"",	// ec
		"",	// ed
		"execute-inline",	// ee
		"execute-inline/range",	// ef
		"invoke-direct-empty",	// f0
		"",	// f1
		"iget-quick",	// f2
		"iget-wide-quick",	// f3
		"iget-object-quick",	// f4
		"iput-quick",	// f5
		"iput-wide-quick",	// f6
		"iput-object-quick",	// f7
		"invoke-virtual-quick",	// f8
		"invoke-virtual-quick/range",	// f9
		"invoke-super-quick",	// fa
		"invoke-super-quick/range",	// fb
		"iput-object-volatile",	// fc
		"sget-object-volatile",	// fd
		"sput-object-volatile",	// fe
		""	// ff
    };

// Codes of instructions that terminate the call flow
    int terminateInstructions[] = {
        0x0E, 0x0F, 0x10, 0x11, 0x27
    };

    private int affectedRegisters[];
    private HashMap<Integer,String> regMap = new HashMap<Integer,String>();
    private HashMap<Long,DedexerTask> labels = new HashMap<Long,DedexerTask>();
    private ArrayList<DedexerTask> tasks = new ArrayList<DedexerTask>();
    private boolean secondPass = false;
    private CodeGenerator codeGenerator;
    private long lowestDataBlock = -1;
    private ForkStatus forkStatus;
    private long forkData[] = null;
    private HashMap<Long,String> quickParameterMap =
                    new HashMap<Long,String>();
    private static final boolean DEBUG_GETAFFECTEDREGSFORREGLIST = false;
    private ArrayList<RegisterTraces> regTraces = null;

    private long calculateTarget( long instrBase ) throws IOException {
        int offset = read8Bit();
        if( ( offset & 0x80 ) != 0 )
            offset -= 256;
        long target = instrBase + ( offset * 2 );
        placeLabel( target, labelForAddress( target ) );
        return target;
    }

    private long calculateTarget16Bit( long instrBase ) throws IOException {
        int offset = read16Bit();
        if( ( offset & 0x8000 ) != 0 )
            offset -= 65536;
        long target = instrBase + ( offset * 2 );
        placeLabel( target, labelForAddress( target ) );
        return target;
    }

    private void updateLowestDataBlock( long address ) {
        if( lowestDataBlock == -1L )
            lowestDataBlock = address;
        else
        if( address < lowestDataBlock )
            lowestDataBlock = address;
    }

    private ForkStatus initialForkStatus( int instrCode ) {
        for( int i = 0 ; i < terminateInstructions.length ; ++i ) {
            if( terminateInstructions[i] == instrCode )
                return ForkStatus.TERMINATE;
        }
        return ForkStatus.CONTINUE;
    }

    private int[] getAffectedRegistersForRange( String proto, 
                int baseReg,
                int thisCount ) {
        ArrayList regOffsets =
            DexClassDefsBlock.getMethodParameterOffsets( 
                    proto, 0 );
        int affectedRegsSize = ( regOffsets.size() / 2 ) + thisCount;
        int affectedRegisters[] = new int[ affectedRegsSize ];
        if( thisCount > 0 )
            affectedRegisters[0] = baseReg;
        int regOffset = -1;
        int regCount = thisCount;
        for( int i = 0 ; i < regOffsets.size() ; i += 2 ) {
            int regx = ((Integer)regOffsets.get( i )).intValue();
            if( regOffset == -1 )
                regOffset = -regx + thisCount + baseReg;
            affectedRegisters[ regCount++ ] = regx + regOffset;
        }
        return affectedRegisters;
    }

    private int[] getAffectedRegistersForRegList( 
                            ArrayList<Integer> registerList,
                            String proto,
                            int notParmReg ) {
        if( DEBUG_GETAFFECTEDREGSFORREGLIST )
            System.out.println( "getAffectedRegistersForRegList: proto: "+
                    proto+
                    "; notParmReg: "+
                    notParmReg+
                    " { "+
                    registerList+
                    " } " );
        ArrayList<Boolean> widthList = 
            DexClassDefsBlock.getMethodParameterWidth( proto );
        if( DEBUG_GETAFFECTEDREGSFORREGLIST )
            System.out.println( "widthList: "+widthList );
        int affectedRegisters[] = new int[ widthList.size()+notParmReg ];
        for( int i = 0 ; i < notParmReg ; ++i )
            affectedRegisters[ i ] = registerList.get( i ).intValue();
        int regCtr = notParmReg;
        for( int i = 0 ; i < widthList.size() ; ++i ) {
            if( DEBUG_GETAFFECTEDREGSFORREGLIST )
                System.out.println( "i: "+i+" ; regCtr: "+regCtr );
            if( regCtr >= registerList.size() ) {
                if( DEBUG_GETAFFECTEDREGSFORREGLIST )
                    System.out.println( "reglist/proto mismatch: reglist: "+
                        registerList+" ; proto: "+proto );
                break;
            }
            affectedRegisters[ i+notParmReg ] = registerList.get( regCtr++ ).intValue();
            if( widthList.get( i ).booleanValue() )
                ++regCtr;
        }
        return affectedRegisters;
    }

    private String getRegType(
                        long pos,
                        int regNo ) {
        String type = regMap.get( new Integer( regNo ) );
        if( ( type == null ) ||
            TYPE_SINGLE_LENGTH.equals( type ) ) {
            String newType = getLocalVariableType( pos,regNo );
            if( newType != null )
                type = newType;
        }
        return type;
    }

    private String getLocalVariableType( 
                    long pos,
                    int regNo ) {
        if( regTraces == null )
                return null;
        for( int i = 0 ; i < regTraces.size() ; ++i ) {
            RegisterTraces regTrace = regTraces.get( i );
// Create the variable in the register map whenever we are in the range. This
// is necessary as there may be many entry points to the range (e.g. multiple
// try-catch entry points into the middle of the range)
            if( regTrace.isInTraceRange( pos ) &&  ( regTrace.regNo == regNo ) )
                return regTrace.type;
        }
        return null;
    }

}

