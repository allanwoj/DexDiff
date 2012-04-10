package item;

public class MethodIdItem {

	public long classId;
	public long proto;
	public long name;
	
	public MethodIdItem(long classId, long proto, long name) {
		this.classId = classId;
		this.proto = proto;
		this.name = name;
	}
	
	public byte[] getOutput(boolean withSize) {
		byte[] ret = new byte[withSize ? 12 : 8];
		
		int start = withSize ? 4 : 0;
		if (withSize) {
			byte[] temp = write32bit(8L);
			for (int i = 0; i < 4; ++i)
				ret[i] = temp[i];
		}
		
		byte[] temp = write16bit(classId);
		for (int i = 0; i < 2; ++i)
			ret[start++] = temp[i];
		
		temp = write16bit(proto);
		for (int i = 0; i < 2; ++i)
			ret[start++] = temp[i];
		
		temp = write32bit(name);
		for (int i = 0; i < 4; ++i)
			ret[start++] = temp[i];
		
		
		return ret;
	}
	
	public byte[] write16bit(long data) {
		byte[] output = new byte[2];
		
		for(int i = 0; i < 2; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
}
