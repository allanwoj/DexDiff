package item;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class CodeItem {

	public int registersSize;
	public int insSize;
	public int outsSize;
	public int triesSize;
	public int debugInfoIndex;
	public long debugInfoOffset;
	public long insnsSize;
	public int padding;
	public TryItem[] tries;
	public EncodedCatchHandlerList handlers;
	public int times;
	public Collection<ByteCode> byteCode;
	public byte[] instructions;

	public CodeItem(int registersSize, int insSize, int outsSize,
			int triesSize, int debugInfoIndex, long debugInfoOffset, long insnsSize,
			Collection<ByteCode> byteCode, byte[] instructions,
			int padding, TryItem[] tries, EncodedCatchHandlerList handlers,
			int times) {
		this.registersSize = registersSize;
		this.insSize = insSize;
		this.outsSize = outsSize;
		this.triesSize = triesSize;
		this.debugInfoIndex = debugInfoIndex;
		this.debugInfoOffset = debugInfoOffset;
		this.insnsSize = insnsSize;
		this.padding = padding;
		this.tries = tries;
		this.handlers = handlers;
		this.times = times;
		this.byteCode = byteCode;
		this.instructions = instructions;
	}
	
	public byte[] getNaiveOutput(long[] typeIndexMap, long[] debugInfoItemMap, long[] debugInfoPointerMap) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(write16bit(registersSize));
		l.addAll(write16bit(insSize));
		l.addAll(write16bit(outsSize));
		l.addAll(write16bit(triesSize));
		l.addAll(write32bit(debugInfoPointerMap[(int)debugInfoItemMap[debugInfoIndex]]));
		l.addAll(write32bit(insnsSize));
		for (int i = 0; i < instructions.length; ++i) {
			l.add(instructions[i]);
		}
		
		if (insnsSize % 2 == 1)
			l.addAll(write16bit(0));
		
		for (int j = 0; j < triesSize; ++j) {
			l.addAll(tries[j].getOutput());
		}
		
		if (triesSize > 0) {
			l.addAll(writeULeb128((int)handlers.size));
		
			for (int j = 0; j < handlers.size; ++j) {
				l.addAll(writeSLeb128((int)handlers.list[j].size));
				for (int k = 0; k < Math.abs(handlers.list[j].size); ++k) {
					l.addAll(writeULeb128((int)typeIndexMap[(int)handlers.list[j].handlers[k].type]));
					l.addAll(writeULeb128((int)handlers.list[j].handlers[k].addr));
				}
				if (handlers.list[j].size <= 0)
					l.addAll(writeULeb128((int)handlers.list[j].catchAllAddr));
			}
			
			for (int j = 0; j < times; ++j) {
				l.add((byte)0);
			}
		}
		
		byte[] ret = new byte[l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public byte[] getNaiveOutput(boolean withSize) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(write16bit(registersSize));
		l.addAll(write16bit(insSize));
		l.addAll(write16bit(outsSize));
		l.addAll(write16bit(triesSize));
		l.addAll(write32bit(debugInfoOffset));
		l.addAll(write32bit(insnsSize));
		for (int i = 0; i < instructions.length; ++i) {
			l.add(instructions[i]);
		}
		
		if (insnsSize % 2 == 1)
			l.addAll(write16bit(0));
		
		for (int j = 0; j < triesSize; ++j) {
			l.addAll(tries[j].getOutput());
		}
		
		if (triesSize > 0) {
			l.addAll(writeULeb128((int)handlers.size));
		
			for (int j = 0; j < handlers.size; ++j) {
				l.addAll(writeSLeb128((int)handlers.list[j].size));
				for (int k = 0; k < Math.abs(handlers.list[j].size); ++k) {
					l.addAll(writeULeb128((int)handlers.list[j].handlers[k].type));
					l.addAll(writeULeb128((int)handlers.list[j].handlers[k].addr));
				}
				if (handlers.list[j].size <= 0)
					l.addAll(writeULeb128((int)handlers.list[j].catchAllAddr));
			}
			
			for (int j = 0; j < times; ++j) {
				l.add((byte)0);
			}
		}
		
		byte[] ret = new byte[withSize ? 4 + l.size() : l.size()];
		Iterator<Byte> iter = l.iterator();
		int count = 0;
		if (withSize) {
			byte[] temp = write32bita(l.size());
			for (int i = 0; i < 4; ++i)
				ret[count++] = temp[i];
		}
		
		while (iter.hasNext()) {
			ret[count++] = iter.next();
		}
		
		return ret;
	}
	
	public byte[] getOutput(long[] stringIndexMap, long[] typeIndexMap, long[] methodIndexMap, long[] debugInfoPointerMap) {
		ArrayList<Byte> l = new ArrayList<Byte>();
		l.addAll(write16bit(registersSize));
		l.addAll(write16bit(insSize));
		l.addAll(write16bit(outsSize));
		l.addAll(write16bit(triesSize));
		l.addAll(write32bit(debugInfoPointerMap[debugInfoIndex]));
		l.addAll(write32bit(insnsSize));
		
		Iterator<ByteCode> it = byteCode.iterator();
		while (it.hasNext()) {
			l.addAll(it.next().getOutput(stringIndexMap, typeIndexMap, methodIndexMap));
		}
		
		if (insnsSize % 2 == 1)
			l.addAll(write16bit(0));
		
		for (int j = 0; j < triesSize; ++j) {
			l.addAll(tries[j].getOutput());
		}
		
		if (triesSize > 0) {
			l.addAll(writeULeb128((int)handlers.size));
		
			for (int j = 0; j < handlers.size; ++j) {
				l.addAll(writeSLeb128((int)handlers.list[j].size));
				for (int k = 0; k < Math.abs(handlers.list[j].size); ++k) {
					l.addAll(writeULeb128((int)typeIndexMap[(int)handlers.list[j].handlers[k].type]));
					l.addAll(writeULeb128((int)handlers.list[j].handlers[k].addr));
				}
				if (handlers.list[j].size <= 0)
					l.addAll(writeULeb128((int)handlers.list[j].catchAllAddr));
			}
			
			for (int j = 0; j < times; ++j) {
				l.add((byte)0);
			}
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
	
	public byte[] write32bita(long data) {
		byte[] output = new byte[4];
		
		for(int i = 0; i < 4; ++i) {
			output[i] = (byte)((data >> (i*8)) & 0xFF);
		}

		return output;
	}
	
}
