/**
  * Interface implemented by code generators. It contains callbacks for different
  * code generation tasks.
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.util.ArrayList;

public interface CodeGenerator {

/**
  * Prints a label in code generator-specific format.
  * @param label The label to render.
  * @throws IOException in case of I/O error
  */
    public void renderLabel( String label ) throws IOException;

/**
  * Renders a line number in code generator-specific format.
  * @param lineNumber Line number to render.
  * @throws IOException in case of I/O error.
  */
    public void renderLineNumber( int lineNumber ) throws IOException;

/**
  * Prints a packed-switch statement.
  * @param reg Register argument of the packed-switch statement
  * @param low The lowest numbered entry in the table
  * @param defaultLabelName Name of the default label
  * @param labels List of label names in the label table.
  * @throws IOException in case of I/O error
  */
    public void renderPackedSwitch( 
        int reg, int low, String defaultLabelName, ArrayList<String> labels )
                    throws IOException;

/**
  * Prints a sparse-switch statement.
  * @param reg Register argument of the sparse-switch statement
  * @param defaultLabelName Name of the default label.
  * @param switchKeys Array of case keys for the statement.
  * @param switchValues Label strings for each case key.
  */
    public void renderSparseSwitch( 
        int reg, String defaultLabelName,
        String switchKeys[], String switchValues[] ) throws IOException;

/**
  * Starts the generation of a new array-data block
  * @param label Label for the array-data block
  */
    public void openDataArray( String label ) throws IOException;

/**
  * Prints one element of the array-data block.
  * @param elementIdx Index of the element.
  * @param element The element itself in preformatted format.
  */
    public void writeElement( long elementIdx, String element ) throws IOException;

/**
  * Closes the generation of an array-data block.
  * @param label Label of the block. This is the same label that was used to
  *             invoke openDataArray for this array-block.
  */
    public void closeDataArray( String label ) throws IOException;

/**
  * Emits a try-catch block declaration.
  * @param startLabel The label where the block starts
  * @param endLabel The label where the block ends
  * @param exception Name of the exception to catch
  * @param handlerLabel Label of the exception handler
  */
    public void writeTryCatchBlock( String startLabel, 
                                String endLabel, 
                                String exception,
                                String handlerLabel ) throws IOException;

/**
  * Emits a local variable declaration
  * @param regNum Register number where the variable is stored.
  * @param variableName Name of the variable.
  * @param variableType Type of the variable.
  * @param startOffsetLabel Name of the label from which the variable is alive.
  * @param endOffsetLabel Name of the label to which the variable is alive.
  */
    public void writeLocalVariable( int regNum,
                                String variableName,
                                String variableType,
                                String startOffsetLabel,
                                String endOffsetLabel );


/**
  * Emits a class annotation.
  * @param classIdx Index of the class in the class table to which the annotation refers to.
  * @param visibility Visibility of the annotation. See AnnotationParser's VISIBILITY_ values for 
  *                   explanation.
  * @param type Type of the annotation.
  * @param parmNames Array of parameter names.
  * @param parmValues Array of parameter values.
  */
    public void writeClassAnnotation( int classIdx, 
                                    int visibility, 
                                    String type, 
                                    ArrayList<String> parmNames, 
                                    ArrayList<Object> parmValues );

}