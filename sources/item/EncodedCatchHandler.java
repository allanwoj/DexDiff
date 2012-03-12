package item;

public class EncodedCatchHandler {

	public long size;
	public EncodedTypeAddrPair[] handlers;
	public long catchAllAddr;
	
	public EncodedCatchHandler(long size, EncodedTypeAddrPair[] handlers,
			long catchAllAddr) {
		this.size = size;
		this.handlers = handlers;
		this.catchAllAddr = catchAllAddr;
	}
	
	
}
