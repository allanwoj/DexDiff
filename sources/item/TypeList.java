package item;

import java.io.IOException;
import java.util.Collection;

public class TypeList {

	public long size;
	public int[] types;
	
	public TypeList(long size, int[] types) {
		this.size = size;
		this.types = types;
	}
	
	public byte[] getOutput(boolean withSize) {
		int length = 4 + 2 * (int)size;
		if (size % 2 == 1) {
			length += 2;
		}
		byte[] ret = new byte[withSize ? 4 + length : length];
		int start = withSize ? 4 : 0;
		if (withSize) {
			byte[] temp = write32bit((long)length);
			for (int i = 0; i < 4; ++i)
				ret[i] = temp[i];
		}
		
		byte[] temp = write32bit((long)size);
		for (int i = 0; i < 4; ++i)
			ret[start + i] = temp[i];
		
		int count = start + 4;
		for (int i = 0; i < size; ++i) {
			temp = write16bit(types[i]);
			for (int j = 0 ; j < 2; ++j) {
				ret[count++] = temp[j];
			}
		}
		
		if (size % 2 == 1) {
			ret[count++] = 0;
			ret[count] = 0;
		}
		
		return ret;
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
