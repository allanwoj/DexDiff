package item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import patch.MapManager;

public class DebugByteCode extends DexItem<DebugByteCode> {

	public int value;
	public long addrDiff;
	public long lineDiff;
	public long name;
	public long type;
	public long sig;
	public int registerNum;

	public DebugByteCode(int value, long addrDiff, long lineDiff, long name,
			long type, long sig, int registerNum) {
		this.value = value;
		this.addrDiff = addrDiff;
		this.lineDiff = lineDiff;
		this.name = name;
		this.type = type;
		this.sig = sig;
		this.registerNum = registerNum;
	}
	
	public boolean isEqual(DebugByteCode b, MapManager mm) {
		
		if (value != b.value) {
			return false;
		}

		if (value == 1) {
			if (addrDiff != b.addrDiff)
				return false;
		} else if (value == 2) {
			if (lineDiff != b.lineDiff)
				return false;
		} else if (value == 3) {
			if (mm.stringIndexMap[(int)name] != b.name ||
					mm.typeIndexMap[(int)type] != b.type ||
					registerNum != b.registerNum) {
				return false;
			}
			
		} else if (value == 4) {
			if (mm.stringIndexMap[(int)name] != b.name ||
					mm.stringIndexMap[(int)sig] != b.sig ||
					mm.typeIndexMap[(int)type] != b.type ||
					registerNum != b.registerNum) {
				return false;
			}
		} else if (value == 5 || value == 6) {
			if (registerNum != b.registerNum) {
				return false;
			}
		} else if (value == 9) {
			if (mm.stringIndexMap[(int)name] != b.name) {
				return false;
			}
		}
		
		return true;
	}
	
	public List<Byte> getRawData() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.add(write(value));
		if (value == 1) {
			l.addAll(writeULeb128((int)addrDiff));
		} else if (value == 2) {
			l.addAll(writeSLeb128((int)lineDiff));
		} else if (value == 3) {
			l.addAll(writeULeb128((int)registerNum));
			l.addAll(writeULeb128(1 + (int)name));
			l.addAll(writeULeb128(1 + (int)type));
		} else if (value == 4) {
			l.addAll(writeULeb128((int)registerNum));
			l.addAll(writeULeb128(1 + (int)name));
			l.addAll(writeULeb128(1 + (int)type));
			l.addAll(writeULeb128(1 + (int)sig));
		} else if (value == 5 || value == 6) {
			l.addAll(writeULeb128((int)registerNum));
		} else if (value == 9) {
			l.addAll(writeULeb128(1 + (int)name));
		}
		return l;
	}
	
	public List<Byte> getModifiedData(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.add(write(value));
		if (value == 1) {
			l.addAll(writeULeb128((int)addrDiff));
		} else if (value == 2) {
			l.addAll(writeSLeb128((int)lineDiff));
		} else if (value == 3) {
			l.addAll(writeULeb128((int)registerNum));
			l.addAll(writeULeb128(1 + (int)mm.stringIndexMap[(int)name]));
			l.addAll(writeULeb128(1 + (int)mm.typeIndexMap[(int)type]));
		} else if (value == 4) {
			l.addAll(writeULeb128((int)registerNum));
			l.addAll(writeULeb128(1 + (int)mm.stringIndexMap[(int)name]));
			l.addAll(writeULeb128(1 + (int)mm.typeIndexMap[(int)type]));
			l.addAll(writeULeb128(1 + (int)mm.stringIndexMap[(int)sig]));
		} else if (value == 5 || value == 6) {
			l.addAll(writeULeb128((int)registerNum));
		} else if (value == 9) {
			l.addAll(writeULeb128(1 + (int)mm.stringIndexMap[(int)name]));
		}
		return l;
	}
	
	Byte write(int value) {
		return (Byte)(byte)value;
	}
	
	Collection<Byte> writeULeb128(int value) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		long remaining = (value & 0xFFFFFFFFL) >> 7;
        long lValue = value;
        int count = 0;

        while (remaining != 0) {

        	l.add((byte)((int)(lValue & 0x7f) | 0x80));
            lValue = remaining;
            remaining >>= 7;
            count++;
        }

			l.add((byte)((int)(lValue & 0x7f)));
        return l;
	}
	
	Collection<Byte> writeSLeb128(int value) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		int remaining = value >> 7;
        int count = 0;
        boolean hasMore = true;
        int end = ((value & Integer.MIN_VALUE) == 0) ? 0 : -1;

        while (hasMore) {
            hasMore = (remaining != end)
                || ((remaining & 1) != ((value >> 6) & 1));

            l.add((byte) ((int)((value & 0x7f) | (hasMore ? 0x80 : 0))));
            value = remaining;
            remaining >>= 7;
            count++;
        }

        return l;
	}
	
}
