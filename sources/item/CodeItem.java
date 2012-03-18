package item;

public class CodeItem {

	public int registersSize;
	public int insSize;
	public int outsSize;
	public int triesSize;
	public int debugInfoIndex;
	public long insnsSize;
	public int[] insns;
	public int padding;
	public TryItem[] tries;
	public EncodedCatchHandlerList handlers;
	public int times;

	public CodeItem(int registersSize, int insSize, int outsSize,
			int triesSize, int debugInfoIndex, long insnsSize, int[] insns,
			int padding, TryItem[] tries, EncodedCatchHandlerList handlers,
			int times) {
		this.registersSize = registersSize;
		this.insSize = insSize;
		this.outsSize = outsSize;
		this.triesSize = triesSize;
		this.debugInfoIndex = debugInfoIndex;
		this.insnsSize = insnsSize;
		this.insns = insns;
		this.padding = padding;
		this.tries = tries;
		this.handlers = handlers;
		this.times = times;
	}
	
	
}
