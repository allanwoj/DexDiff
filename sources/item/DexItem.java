package item;

import java.util.List;
import patch.MapManager;

public abstract class DexItem <ItemType> {
	public abstract boolean isEqual(ItemType other, MapManager mm);
	public abstract List<Byte> getRawData();
	public abstract List<Byte> getModifiedData(MapManager mm);
}
