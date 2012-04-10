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
	
	public byte[] getOutput(boolean withSize) {
		byte[] ret = new byte[withSize ? 16 : 12];
		
		int start = withSize ? 4 : 0;
		if (withSize) {
			byte[] temp = write32bit(12L);
			for (int i = 0; i < 4; ++i)
				ret[i] = temp[i];
		}
		
		byte[] temp = write32bit(shorty);
		for (int i = 0; i < 4; ++i)
			ret[start++] = temp[i];
		
		temp = write32bit(type);
		for (int i = 0; i < 4; ++i)
			ret[start++] = temp[i];
		
		temp = write32bit(typeListOffset);
		for (int i = 0; i < 4; ++i)
			ret[start++] = temp[i];
		
		
		return ret;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
}
