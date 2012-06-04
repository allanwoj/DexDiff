package item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import patch.MapManager;

public class DebugInfoItem {

	public long lineStart;
	public long parametersSize;
	public long[] parameterNames;
	public Collection<DebugByteCode> debugByteCode;

	public DebugInfoItem(long lineStart, long parametersSize,
			long[] parameterNames, Collection<DebugByteCode> debugByteCode) {
		this.lineStart = lineStart;
		this.parametersSize = parametersSize;
		this.parameterNames = parameterNames;
		this.debugByteCode = debugByteCode;
	}
	
	public byte[] getByteCode(boolean withSize) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(writeULeb128((int)lineStart));
		l.addAll(writeULeb128((int)parametersSize));
		for (int j = 0; j < parametersSize; ++j) {
			l.addAll(writeULeb128(1 + (int)parameterNames[j]));
		}
		
		Iterator<DebugByteCode> it = debugByteCode.iterator();
		while (it.hasNext()) {
			l.addAll(it.next().getBytecode());
		}
		l.add((byte)0);
		
		byte[] ret = new byte[withSize ? 4 + l.size() : l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		if (withSize) {
			byte[] temp = write32bit(l.size());
			for (int i = 0; i < 4; ++i)
				ret[count++] = temp[i];
		}
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	
	
	public byte[] getByteCode(MapManager mm) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(writeULeb128((int)lineStart));
		l.addAll(writeULeb128((int)parametersSize));
		for (int j = 0; j < parametersSize; ++j) {
			if (parameterNames[j] == -1) {
				l.addAll(writeULeb128(0));
			} else {
				l.addAll(writeULeb128(1 + (int)mm.stringIndexMap[(int)parameterNames[j]]));
			}
		}
		
		Iterator<DebugByteCode> it = debugByteCode.iterator();
		while (it.hasNext()) {
			l.addAll(it.next().getBytecode(mm));
		}
		l.add((byte)0);
		
		byte[] ret = new byte[l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public boolean isEqual(DebugInfoItem other, MapManager mm) {
		if (lineStart != other.lineStart || parametersSize != other.parametersSize ||
    			debugByteCode.size() != other.debugByteCode.size()) {
    		return false;
    	}

		for (int k = 0; k < parametersSize; ++k) {
			if (parameterNames[k] != -1 && other.parameterNames[k] != -1) {
    			if (mm.stringIndexMap[(int)parameterNames[k]] != other.parameterNames[k]) {
    				return false;
    			}
			}	
		}
		
		Iterator<DebugByteCode> it1 = debugByteCode.iterator();
		Iterator<DebugByteCode> it2 = other.debugByteCode.iterator();
			
		while (it1.hasNext()) {
			DebugByteCode b1 = it1.next();
			DebugByteCode b2 = it2.next();
			
			if (!b1.isEqual(b2, mm.typeIndexMap, mm.stringIndexMap)) {
				return false;
			}
		}
		
		return true;
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
	
	public byte[] write32bit(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
}
