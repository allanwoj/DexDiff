package item;

import java.util.ArrayList;
import java.util.List;

import patch.MapManager;

public class StringDataItem extends DexItem<StringDataItem> {

	public String data;

	public StringDataItem(String data) {
		this.data = data;
	}

	public boolean isEqual(StringDataItem other, MapManager mm) {
		return data.equals(other.data);
	}

	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
    	int size = data.length() - 1;
    	for (int i = 0; i < 4; ++i)
    		l.add((byte)(size >> i*8));
    	byte[] temp = new byte[size];
    	data.getBytes(1, size + 1, temp, 0);
    	for (int i = 0; i < temp.length; ++i)
    		l.add(temp[i]);
    	return l;
	}

	public List<Byte> getModifiedData(MapManager mm) {
		return null;
	}
}
