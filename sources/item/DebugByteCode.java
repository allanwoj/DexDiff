package item;

public class DebugByteCode {

	public int value;
	public long addrDiff;
	public long lineDiff;
	public long name;
	public long type;
	public long sig;
	public int registerNum;

	public DebugByteCode(int value, long addrDiff, long lineDiff, long name,
			long type, long sig, int registerNum) {
		this.value = value;
		this.addrDiff = addrDiff;
		this.lineDiff = lineDiff;
		this.name = name;
		this.type = type;
		this.sig = sig;
		this.registerNum = registerNum;
	}
	
	
}
