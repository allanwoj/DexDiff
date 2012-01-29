/**
  * Utility class used between JasminStyleCodeGenerator and DexInstructionParser
  * to pass the local variable debug information to DexInstructionParser.
  */

package hu.uw.pallergabor.dedexer;

class RegisterTraces {
        long traceBegin = -1L;
        long traceEnd = -1L;
        String type = null;
        int regNo = -1;

        public RegisterTraces( long traceBegin,
                            long traceEnd,
                            String type,
                            int regNo ) {
            this.traceBegin = traceBegin;
            this.traceEnd = traceEnd;
            this.type = type;
            this.regNo = regNo;
        }

        public boolean isAtTraceEnd( long pos ) {
            return pos >= traceEnd;
        }

        public boolean isInTraceRange( long pos ) {
            return ( pos >= traceBegin ) &&
                    ( pos < traceEnd );
        }
}
