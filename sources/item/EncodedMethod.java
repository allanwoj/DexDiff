package item;

public class EncodedMethod {

	public long methodIdDiff;
	public long flags;
	public int codeItemIndex;
	public long codeItemOffset;

	public EncodedMethod(long methodIdDiff, long flags, int codeItemIndex, long codeItemOffset) {
		this.methodIdDiff = methodIdDiff;
		this.flags = flags;
		this.codeItemIndex = codeItemIndex;
		this.codeItemOffset = codeItemOffset;
	}
	
}
