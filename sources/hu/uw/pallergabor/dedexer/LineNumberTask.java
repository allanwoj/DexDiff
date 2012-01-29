/**
  * Stores a line number task.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class LineNumberTask extends DedexerTask {

    public LineNumberTask( DexInstructionParser instrParser, int lineNumber ) {
        super( instrParser, 0L, 0L );
        this.lineNumber = lineNumber;
    }

    public boolean equals( DedexerTask o ) {
        if( !(o instanceof LineNumberTask ) )
            return false;
        LineNumberTask otherLineNumberTask = (LineNumberTask)o;
        return lineNumber == otherLineNumberTask.getLineNumber();
    }


    public void doTask( boolean isSecondPass ) {
    }

    public void renderTask( long position ) throws IOException {
        instrParser.getCodeGenerator().renderLineNumber( lineNumber );
    }

    public String toString() {
        return Integer.toString( lineNumber );
    }

    public int getPriority() {
        return DedexerTask.MIN_PRIORITY+80;        // second after label
    }

    public int getLineNumber() {
        return lineNumber;
    }

    private int lineNumber;
}

