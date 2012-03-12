package item;

public class TryItem {

	public long startAddr;
	public int insnCount;
	public int handlerOffset;

	public TryItem(long startAddr, int insnCount, int handlerOffset) {
		this.startAddr = startAddr;
		this.insnCount = insnCount;
		this.handlerOffset = handlerOffset;
	}
	
	
}
