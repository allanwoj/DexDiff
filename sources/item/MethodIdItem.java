package item;

import java.util.ArrayList;
import java.util.List;

import patch.MapManager;

public class MethodIdItem extends DexItem<MethodIdItem> {

	public long classId;
	public long proto;
	public long name;
	
	public MethodIdItem(long classId, long proto, long name) {
		this.classId = classId;
		this.proto = proto;
		this.name = name;
	}
	
	public List<Byte> getModifiedData(MapManager mm) {
		return null;
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		
		byte[] temp = write32bit(8L);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		temp = write16bit(classId);
		for (int i = 0; i < 2; ++i)
			l.add(temp[i]);
		
		temp = write16bit(proto);
		for (int i = 0; i < 2; ++i)
			l.add(temp[i]);
		
		temp = write32bit(name);
		for (int i = 0; i < 4; ++i)
			l.add(temp[i]);
		
		return l;
	}
	
	public boolean isEqual(MethodIdItem other, MapManager mm) {
		return (mm.typeIndexMap[(int)classId] == other.classId &&
				mm.protoIndexMap[(int)proto] == other.proto &&
				mm.stringIndexMap[(int)name] == other.name);
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
