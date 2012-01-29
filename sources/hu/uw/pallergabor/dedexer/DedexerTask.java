/**
  * Stores an dedexer task for later execution. These are data areas related
  * to switch, fill-array, etc. instructions that need to be analysed between the
  * the two passes. This is an abstract base class that is specialized for the
  * different tasks. 
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public abstract class DedexerTask {

    public static final int MIN_PRIORITY = 0;

    public DedexerTask( DexInstructionParser instrParser, 
                        long base, 
                        long offset ) {
        this.instrParser = instrParser;
        this.base = base;
        this.offset = offset;
    }

    public long getBase() {
        return base;
    }

    public long getOffset() {
        return offset;
    }

/**
  * Checks whether its parameter equals to the String content of this
  * task. 
  * @param The String to check for equality.
  * @return true if the parameter is equal to the String content of this task.
  */
    public boolean equals( String str ) {
        return toString().equals( str );
    }

/**
  * Checks whether this DedexerTask is equals to another DedexerTask
  * @param Another DedexerTask to check for equality
  * @return true if this DedexerTask is equal to the other DedexerTask
  */
    public boolean equals( DedexerTask otherTask ) {
        if( otherTask == null )
            return false;
	long otherOffset = otherTask.getOffset();
	long otherBase = otherTask.getBase();
	if( ( offset == 0L && base == 0L ) ||
	    ( otherOffset == 0L && otherBase == 0L ) )
		return toString().equals( otherTask.toString() );
	else
        	return ( offset == otherOffset ) &&
                	( base == otherBase );
    }

/**
  * This method is specialized in child classes to execute the task.
  * @param isSecondPass true if the second pass is executing.
  * @throws IOException in case of file I/O error.
  */
    public abstract void doTask( boolean isSecondPass ) throws IOException;

/**
  * This method is specialized in child classes to emit output at the 
  * instructions's location once the instruction is reachedin the second pass.
  * @param position Position in the code where the renderTask was called
  */
    public abstract void renderTask( long position ) throws IOException;

    public String toString() {
        return getClass().getName()+"; base: 0x"+
                Long.toHexString( base )+
                "; offset: 0x"+
                Long.toHexString( offset );
    }

/**
  * Retrieves the priority for this task. Tasks are executed by decreasing 
  * priority if there are more than one tasks at a location.
  * @return The priority of the task.
  */
    public int getPriority() {
        return MIN_PRIORITY;
    }

/**
  * Returns a boolean flag whether the task parses code when its renderTask is
  * invoked during the second pass. This information is used by the second
  * pass of the disassembler: if this method returns true, the instruction parser
  * is not invoked after the task's renderTask was invoked because it is assumed
  * that the task itself parsed a piece of code.
  * @param position The position in the source file.
  * @return The parse flag for the task
  */
    public boolean getParseFlag( long position ) {
        return false;
    }

    protected DexInstructionParser instrParser;
    protected long offset;
    protected long base;
}
