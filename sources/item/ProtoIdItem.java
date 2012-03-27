package item;

public class ProtoIdItem {

	public long shorty;
	public long type;
	public int typeListIndex;
	public long typeListOffset;
	
	public ProtoIdItem(long shorty, long type, int typeListIndex, long typeListOffset) {
		this.shorty = shorty;
		this.type = type;
		this.typeListIndex = typeListIndex;
		this.typeListOffset = typeListOffset;
	}
}
