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
	
	public boolean isEqual(EncodedCatchHandler update, long[] typeIndexMap) {
		if (size != update.size || catchAllAddr != update.catchAllAddr)
			return false;
		
		for (int i = 0; i < size; ++i) {
			if (!handlers[i].isEqual(update.handlers[i], typeIndexMap)) {
				return false;
			}
		}
		
		
		return true;
	}
	
	
}
