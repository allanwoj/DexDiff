package item;

public class EncodedCatchHandlerList {

	public long size;
	public EncodedCatchHandler[] list;
	
	public EncodedCatchHandlerList(long size, EncodedCatchHandler[] list) {
		this.size = size;
		this.list = list;
	}
	
	public boolean isEqual(EncodedCatchHandlerList update, long[] typeIndexMap) {
		if (size != update.size) {
			return false;
		}
		
		for (int i = 0; i < size; ++i) {
			if (!list[i].isEqual(update.list[i], typeIndexMap)) {
				return false;
			}
		}
		return true;
	}
	
}
