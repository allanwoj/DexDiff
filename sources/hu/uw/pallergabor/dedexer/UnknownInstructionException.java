/**
  * Thrown when the parser encounters an unknown instruction.
  */
package hu.uw.pallergabor.dedexer;

public class UnknownInstructionException extends Exception {
    public UnknownInstructionException() {
        super();
    }

    public UnknownInstructionException( String message ) {
        super( message );
    }

    public UnknownInstructionException( String message, Throwable excp ) {
        super( message, excp );
    }
}
