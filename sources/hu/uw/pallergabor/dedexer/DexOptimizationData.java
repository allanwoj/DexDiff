/**
  * Holder class for optimization data. Instance of this class is commonly shared
  * between different parsers.
  */
package hu.uw.pallergabor.dedexer;

public class DexOptimizationData {

    public void setDexOffset( long dexOffset ) {
        this.dexOffset = dexOffset;
    }

    public long getDexOffset() {
        return dexOffset;
    }

    public void setDexLength( long dexLength ) {
        this.dexLength = dexLength;
    }

    public long getDexLength() {
        return dexLength;
    }

    public void setDepsOffset( long depsOffset ) {
        this.depsOffset = depsOffset;
    }

    public long getDepsOffset() {
        return depsOffset;
    }


    public void setDepsLength( long depsLength ) {
        this.depsLength = depsLength;
    }

    public long getDepsLength() {
        return depsLength;
    }

    public void setAuxOffset( long depsOffset ) {
        this.auxOffset = auxOffset;
    }

    public long getAuxOffset() {
        return auxOffset;
    }


    public void setAuxLength( long auxLength ) {
        this.auxLength = auxLength;
    }

    public long getAuxLength() {
        return auxLength;
    }

    public boolean isOptimized() {
        return dexOffset != 0L;
    }

    private long dexOffset = 0L;
    private long dexLength = 0L;
    private long depsOffset = 0L;
    private long depsLength = 0L;
    private long auxOffset = 0L;
    private long auxLength = 0L;
}
