package item;

public class FieldIdItem {

	public int classId;
	public int typeId;
	public long nameId;
	
	public FieldIdItem(int classId,	int typeId,	long nameId) {
		this.classId = classId;
		this.typeId = typeId;
		this.nameId = nameId;
	}
	
	public byte[] getOutput(boolean withSize) {
		byte[] ret = new byte[withSize ? 12 : 8];
		int start = withSize ? 4 : 0;
		ret[start] = (byte)((classId) & 0xFF);
		ret[start + 1] = (byte)((classId >> 8) & 0xFF);
		
		ret[start + 2] = (byte)((typeId) & 0xFF);
		ret[start + 3] = (byte)((typeId >> 8) & 0xFF);
		
		ret[start + 4] = (byte)((nameId) & 0xFF);
		ret[start + 5] = (byte)((nameId >> 8) & 0xFF);
		ret[start + 6] = (byte)((nameId >> 16) & 0xFF);
		ret[start + 7] = (byte)((nameId >> 32) & 0xFF);
		
		if (withSize) {
			ret[0] = 8;
			ret[1] = 0;
			ret[2] = 0;
			ret[3] = 0;
		}
		
		return ret;
	}
		
}
