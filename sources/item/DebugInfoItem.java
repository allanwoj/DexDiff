package item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
	
	public byte[] getByteCode() {
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
		
		byte[] ret = new byte[l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
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
	
	
}
