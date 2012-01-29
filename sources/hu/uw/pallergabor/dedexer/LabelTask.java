/**
  * Stores a label task.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class LabelTask extends DedexerTask {

    public LabelTask( DexInstructionParser instrParser, String label ) {
        super( instrParser, 0L, 0L );
        this.label = label;
    }

    public void doTask( boolean isSecondPass ) {
    }

    public void renderTask( long position ) throws IOException {
        instrParser.getCodeGenerator().renderLabel( label );
    }

    public String toString() {
        return label;
    }

    public int getPriority() {
        return DedexerTask.MIN_PRIORITY+100;        // always first
    }

    private String label;
}

