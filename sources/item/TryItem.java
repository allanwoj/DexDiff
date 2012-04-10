package item;

import java.util.ArrayList;
import java.util.Collection;

public class TryItem {

	public long startAddr;
	public int insnCount;
	public int handlerOffset;

	public TryItem(long startAddr, int insnCount, int handlerOffset) {
		this.startAddr = startAddr;
		this.insnCount = insnCount;
		this.handlerOffset = handlerOffset;
	}
	
	public boolean isEqual(TryItem update) {
		return startAddr == update.startAddr && insnCount == update.insnCount && handlerOffset == update.handlerOffset;
	}
	
	public Collection<Byte> getOutput() {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(write32bit(startAddr));
		l.addAll(write16bit(insnCount));
		l.addAll(write16bit(handlerOffset));
		return l;
	}
	
	Collection<Byte> write16bit(long data) {
		ArrayList<Byte> output = new ArrayList<Byte>();
		
		for(int i = 0; i < 2; ++i) {
			output.add((byte)((data >> (i*8)) & 0xFF));
		}

		return output;
	}
	
	Collection<Byte> write32bit(long data) {
		ArrayList<Byte> output = new ArrayList<Byte>();
		
		for(int i = 0; i < 4; ++i) {
			output.add((byte)((data >> (i*8)) & 0xFF));
		}

		return output;
	}
	
}
