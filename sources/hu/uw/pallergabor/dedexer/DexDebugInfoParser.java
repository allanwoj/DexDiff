/**
  * Parses a debug info block of a method
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DexDebugInfoParser extends DexParser {

    public void parse() throws IOException {
        variableList = new ArrayList<LocalVariableHolder>();
        lineNumberList = new ArrayList<LineNumberHolder>();
        int lineRegister = 0;
        int addressRegister = 0;
        HashMap<String,LocalVariableHolder> registers = new HashMap<String,LocalVariableHolder>();
// Read the line register
        lineRegister = (int)readVLN();
        dump( "Starting line: "+lineRegister );
// Read the parameter size
        int parmSize = (int)readVLN();
        dump( "Parameter number: "+parmSize );
        for( int i = 0 ; i < parmSize ; ++i ) {
            int parameter = (int)readVLN() - 1; // uleb128p1
            LocalVariableHolder lvh = new LocalVariableHolder();
            lvh.variableName = parameter < 0 ? 
                                "unnamed"+Integer.toString( i ) : 
                                dexStringIdsBlock.getString( parameter );
            dump( "reg#"+
                    lvh.regNum+" : "+
                    lvh.variableName );
            variableList.add( lvh );
        }
// Bytecode interpreter starts
l1:     while( true ) {
            int b = read8Bit();
            switch( b ) {
                case DBG_END_SEQUENCE:
                    dump( "DBG_END_SEQUENCE" );
                    break l1;

                case DBG_ADVANCE_PC: {
                    int offset = (int)readVLN();
                    addressRegister += offset;
                    dump( "DBG_ADVANCE_PC: by "+
                            offset+
                            " bytes (new address is 0x"+
                            Integer.toHexString( addressRegister )+
                            ")" );
                }
                break;

                case DBG_ADVANCE_LINE: {
                    int offset = (int)readSignedVLN();
                    lineRegister += offset;
                    dump( "DBG_ADVANCE_LINE: by "+
                            offset+
                            " lines (new line counter: "+
                            lineRegister+")" );
                }
                break;

                case DBG_START_LOCAL: {
                    int regNum = (int)readVLN();
                    int nameIdx = (int)readVLN() - 1;
                    int typeIdx = (int)readVLN() - 1;
                    if( ( nameIdx >= 0 ) && ( typeIdx >= 0 ) ) {
                        LocalVariableHolder lvh = new LocalVariableHolder();
                        lvh.regNum = regNum;
                        lvh.variableName = dexStringIdsBlock.getString( nameIdx );
                        lvh.typeName = dexTypeIdsBlock.getType( typeIdx );
                        lvh.startOffset = addressRegister;
                        registers.put( Integer.toString( regNum ),lvh );
                        dump( "DBG_START_LOCAL: v"+
                                regNum+
                                "; name: "+
                                lvh.variableName+
                                "; type: "+
                                lvh.typeName+
                                "; address: 0x"+
                                Integer.toHexString( addressRegister ) );
                    } else
                        dump( "DBG_START_LOCAL: no name local variable allocated to v"+
                                regNum );
                }
                break;

                case DBG_START_LOCAL_EXTENDED: {
                    int regNum = (int)readVLN();
                    int nameIdx = (int)readVLN() - 1;
                    int typeIdx = (int)readVLN() - 1;
                    int sigIdx = (int)readVLN() - 1;
                    if( ( nameIdx >= 0 ) && ( typeIdx >= 0 ) ) {
                        LocalVariableHolder lvh = new LocalVariableHolder();
                        lvh.regNum = regNum;
                        lvh.variableName = dexStringIdsBlock.getString( nameIdx );
                        lvh.typeName = dexTypeIdsBlock.getType( typeIdx );
                        lvh.startOffset = addressRegister;
                        registers.put( Integer.toString( regNum ),lvh );
                        dump( "DBG_START_LOCAL_EXTENDED: v"+
                                regNum+
                                "; name: "+
                                lvh.variableName+
                                "; type: "+
                                lvh.typeName+
                                "; address: 0x"+
                                Integer.toHexString( addressRegister )+
                                "; signature index: "+
                                sigIdx );
                    } else
                        dump( "DBG_START_LOCAL_EXTENDED: no name local variable allocated to v"+
                                regNum );
                }
                break;

                case DBG_END_LOCAL: {
                    int regNum = (int)readVLN();
                    LocalVariableHolder lvh = registers.get( 
                        Integer.toString( regNum ) );
                    if( lvh == null )
                        dump( "DBG_END_LOCAL: v"+regNum+" has no associated DBB_START_LOCAL" );
                    else {
                        lvh.endOffset = addressRegister;
                        variableList.add( lvh );
                        dump( "DBG_END_LOCAL: v"+
                                    regNum+
                                    " (0x"+
                                    Integer.toHexString( lvh.startOffset )+
                                    " - 0x"+
                                    Integer.toHexString( lvh.endOffset )+
                                    ")" );
                    }
                }
                break;

                case DBG_RESTART_LOCAL: {
                    int regNum = (int)readVLN();
                    String regString = Integer.toString( regNum );
                    LocalVariableHolder lvh = registers.get( regString );
                    if( lvh == null )
                        dump( "DBG_RESTART_LOCAL: v"+regNum+" has no associated DBB_START_LOCAL" );
                    else {
                        LocalVariableHolder lvh2 = lvh.clone();
                        registers.put( regString,lvh2 );
                        lvh2.startOffset = addressRegister;
                        dump( "DBG_RESTART_LOCAL: v"+
                                    regNum+
                                    "; name: "+
                                    lvh2.variableName+
                                    "; type: "+
                                    lvh2.typeName+
                                    " restarted at 0x"+
                                    Integer.toHexString( lvh2.startOffset ) );
                    }
                }
                break;

                case DBG_SET_PROLOGUE_END: {
                    dump( "DBG_SET_PROLOGUE_END" );
                }
                break;

                case DBG_SET_EPILOGUE_BEGIN: {
                    dump( "DBG_SET_EPILOGUE_BEGIN" );
                }
                break;

                case DBG_SET_FILE: {
                    int sourceFileIdx = (int)readVLN() - 1;
                    if( sourceFileIdx < 0 )
                        dump( "DBG_SET_FILE: unspecified file name" );
                    else {
                        String s = dexStringIdsBlock.getString( sourceFileIdx );
                        dump( "DBG_SET_FILE: source file name set: "+
                                s+
                                " at 0x"+
                                Integer.toHexString( addressRegister ) );
                    }
                }
                break;

                default: {
                    int adjustedOpcode = b - DBG_FIRST_SPECIAL;
                    lineRegister += DBG_LINE_BASE + ( adjustedOpcode % DBG_LINE_RANGE );
                    addressRegister += ( adjustedOpcode / DBG_LINE_RANGE );
                    LineNumberHolder lnh = new LineNumberHolder();
                    lnh.lineNumber = lineRegister;
                    lnh.address = addressRegister;
                    lineNumberList.add( lnh );
                    dump( "Line register: "+
                            lineRegister+
                            "; address register: 0x"+
                            Integer.toHexString( addressRegister ) );
                }
            }
        }
    }

    public void setDexStringIdsBlock( DexStringIdsBlock dexStringIdsBlock ) {
        this.dexStringIdsBlock = dexStringIdsBlock;
    }

    public void setDexTypeIdsBlock( DexTypeIdsBlock dexTypeIdsBlock ) {
        this.dexTypeIdsBlock = dexTypeIdsBlock;
    }

    public int getLineNumbers() {
        return lineNumberList.size();
    }

    public int getLineNumber( int idx ) {
        return lineNumberList.get( idx ).lineNumber;
    }

    public int getLineNumberAddress( int idx ) {
        return lineNumberList.get( idx ).address;
    }

    public int getLocalVariables() {
        return variableList.size();
    }

    public int getLocalVariableRegNum( int idx ) {
        return variableList.get( idx ).regNum;
    }

    public String getLocalVariableName( int idx ) {
        return variableList.get( idx ).variableName;
    }

    public String getLocalVariableType( int idx ) {
        return variableList.get( idx ).typeName;
    }

    public int getLocalVariableStartOffset( int idx ) {
        return variableList.get( idx ).startOffset;
    }

    public int getLocalVariableEndOffset( int idx ) {
        return variableList.get( idx ).endOffset;
    }

    private static final int DBG_END_SEQUENCE = 0x00;
    private static final int DBG_ADVANCE_PC = 0x01;
    private static final int DBG_ADVANCE_LINE = 0x02;
    private static final int DBG_START_LOCAL = 0x03;
    private static final int DBG_START_LOCAL_EXTENDED = 0x04;
    private static final int DBG_END_LOCAL = 0x05;
    private static final int DBG_RESTART_LOCAL = 0x06;
    private static final int DBG_SET_PROLOGUE_END = 0x07;
    private static final int DBG_SET_EPILOGUE_BEGIN = 0x08;
    private static final int DBG_SET_FILE = 0x09;
    private static final int DBG_FIRST_SPECIAL = 0x0A;
    private static final int DBG_LINE_BASE = -4;
    private static final int DBG_LINE_RANGE  = 15;

    private DexStringIdsBlock dexStringIdsBlock;
    private DexTypeIdsBlock dexTypeIdsBlock;
    private ArrayList<LocalVariableHolder> variableList;
    private ArrayList<LineNumberHolder> lineNumberList;

    class LineNumberHolder {
        int lineNumber;
        int address;
    }

    class LocalVariableHolder {
        int regNum = -1;
        String variableName = null;
        String typeName = null;
        int startOffset = -1;
        int endOffset = -1;

        public LocalVariableHolder clone() {
            LocalVariableHolder result = new LocalVariableHolder();
            result.regNum = regNum;
            result.variableName = variableName;
            result.typeName = typeName;
            result.startOffset = startOffset;
            result.endOffset = endOffset;
            return result;
        }
    }
}
