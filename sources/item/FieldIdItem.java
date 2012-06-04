package item;

import java.util.ArrayList;
import java.util.List;

import patch.MapManager;

public class FieldIdItem extends DexItem<FieldIdItem> {

	public int classId;
	public int typeId;
	public long nameId;
	
	public FieldIdItem(int classId,	int typeId,	long nameId) {
		this.classId = classId;
		this.typeId = typeId;
		this.nameId = nameId;
	}
	
	public boolean isEqual(FieldIdItem other, MapManager mm) {
		return (mm.stringIndexMap[(int) nameId] == other.nameId
        		&& mm.typeIndexMap[classId] == other.classId
        		&& mm.typeIndexMap[typeId] == other.typeId);
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.add((byte) 8);
		l.add((byte) 0);
		l.add((byte) 0);
		l.add((byte) 0);
		
		l.add((byte)((classId) & 0xFF));
		l.add((byte)((classId >> 8) & 0xFF));
		
		l.add((byte)((typeId) & 0xFF));
		l.add((byte)((typeId >> 8) & 0xFF));
		
		l.add((byte)((nameId) & 0xFF));
		l.add((byte)((nameId >> 8) & 0xFF));
		l.add((byte)((nameId >> 16) & 0xFF));
		l.add((byte)((nameId >> 32) & 0xFF));
		
		return l;
	}
	
	public List<Byte> getModifiedData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		return l;
	}
		
}
