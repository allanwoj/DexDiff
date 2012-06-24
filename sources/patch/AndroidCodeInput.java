package patch;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import android.BaseCodeCursor;
import android.CodeInput;

public class AndroidCodeInput extends BaseCodeCursor
implements CodeInput {
	
	DexOriginalFile file;
	
	public AndroidCodeInput(DexOriginalFile file) {
		this.file = file;
	}

	public boolean hasMore() {
		// TODO Auto-generated method stub
		return true;
	}

	public Collection<Byte> read() throws EOFException {
		ArrayList<Byte> l = new ArrayList<Byte>();
		try {
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}

	public long read16Bit() throws EOFException {
		long result = 0;
		try {
			result = file.read8Bit();
			result |= ( file.read8Bit() << 8 );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	    return result;
	}

	public Collection<Byte> readInt() throws EOFException {
		ArrayList<Byte> l = new ArrayList<Byte>();
		try {
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}

	public Collection<Byte> readLong() throws EOFException {
		ArrayList<Byte> l = new ArrayList<Byte>();
		try {
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
			l.add((byte)file.read8Bit());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return l;
	}

}
