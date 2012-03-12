package item;

public class EncodedMethod {

	public long diff;
	public long flags;
	public long codeOffset;

	public EncodedMethod(long diff, long flags, long codeOffset) {
		this.diff = diff;
		this.flags = flags;
		this.codeOffset = codeOffset;
	}
	
}
