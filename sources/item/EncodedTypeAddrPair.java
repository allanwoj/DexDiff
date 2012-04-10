package item;

public class EncodedTypeAddrPair {

	public long type;
	public long addr;
	
	public EncodedTypeAddrPair(long type, long addr) {
		this.type = type;
		this.addr = addr;
	}
	
	public boolean isEqual(EncodedTypeAddrPair update, long[] typeIndexMap) {
		return typeIndexMap[(int)type] == update.type && addr == update.addr;
	}
	
}
