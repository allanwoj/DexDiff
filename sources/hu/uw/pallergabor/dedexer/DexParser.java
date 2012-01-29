/**
  * Abstract class acting as a base class for all parsers.
  */
package hu.uw.pallergabor.dedexer;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public abstract class DexParser {
    public static long odexOffset = 0x28L;

    RandomAccessFile file;
    protected PrintStream dump;
// To store the words for the current dump line
    private ArrayList<Integer> dumpStorage = new ArrayList<Integer>( 8 );
// The base file offset for the current dump line
    private long dumpBaseOffset = -1L;
    private boolean dumpOn = true;
    private DexOptimizationData dexOptimizationData = null;

    public void setRandomAccessFile( RandomAccessFile file ) {
        this.file = file;
    }

    public void setFilePosition( long position ) throws IOException {
        file.seek( position ); 
    }

    public long getFilePosition() throws IOException {
        return file.getFilePointer();
    }

    public DexOptimizationData getDexOptimizationData() {
        return dexOptimizationData;
    }

    public void setDexOptimizationData( DexOptimizationData dexOptimizationData ) {
        this.dexOptimizationData = dexOptimizationData;
    }

    public void setDumpFile( PrintStream dump ) {
        this.dump = dump;
    }

    public void setDumpOn() {
        dumpOn = true;
    }

    public void setDumpOff() {
        dumpOn = false;
    }

/**
  * Reads one 8 bit byte from the input file and returns it.
  * @return the byte read
  */
    public int read8Bit() throws IOException {
        if( dumpBaseOffset < 0 )
            dumpBaseOffset = file.getFilePointer();
        int result = file.read();
        if( dumpOn )
            dumpStorage.add( new Integer( result ) );
        return result;
    }

/**
  * Reads one 8 bit byte from the input file and returns it.
  * @return the byte read, sign-extended
  */
    public int readSigned8Bit() throws IOException {
		int result = read8Bit();
		if( ( result & 0x80 ) != 0 )
			result -= 256;
        return result;
    }


/**
  * Reads one 16 bit word from the input file and returns it.
  * @return the word read
  */
    public int read16Bit() throws IOException {
        int result = read8Bit();
        result |= ( read8Bit() << 8 );
        return result;
    }

/**
  * Reads a 32-bit word from the input file and returns it. Long is
  * used to avoid troubles with sign.
  * @return the 32-bit word
  */
    public long read32Bit() throws IOException {
        long result = (long)read16Bit();
        result |= ((long)read16Bit()) << 16;
        return result;
    }

/**
  * Reads a file offset.
  * @return the file offset
  */
    public long readFileOffset() throws IOException {
        long r = read32Bit();
// 0 remains 0 as it is used as "block missing" flag
        return r == 0L ? 0L : r+getOdexOffset();
    }

/**
  * Reads a 32-bit word and returns the signed value.
  * @return the 32-bit signed word.
  */
    public int readSigned32Bit() throws IOException {
        int result = read16Bit();
        result |= read16Bit() << 16;
        return result;
    }

/**
  * Reads a string from the input file and returns it. The file
  * pointer is positioned to the beginning of the string. The first byte
  * of the string is the length of the string, followed by the characters 
  * of the string.
  * @return The string read
  * @throws IOException in case of I/O error.
  */
    public String readString() throws IOException {
        int size = read8Bit();
        StringBuilder b = new StringBuilder( size );
        for( int i = 0 ; i < size ; ++i )
            b.append( (char)read8Bit() );
        return new String( b );
    }

/**
  * Reads a variable-length number and returns it. If the current byte of the VLN
  * has bit 7 set, there will be an additional byte.
  * @return the VLN as a long
  * @throws IOException in case of I/O error
  */
    public long readVLN() throws IOException {
        int bitpos = 0;
        long vln = 0L;
        do {
            int inp = read8Bit();
            vln |= ((long)( inp & 0x7F )) << bitpos;
            if( ( inp & 0x80 ) == 0 )
                break;
            bitpos += 7;
        } while( true );
        return vln;
    }

/**
  * Reads a variable-length number with given length. Sign-extension is
  * optionally provided.
  * @param bytes Number of bytes to read.
  * @param signed True if sign-extension is needed.
  * @return the number as a sign-extended long
  * @throws IOException in case of I/O error
  */
	public long readVLNWithLength( int bytes, boolean signed ) 
											throws IOException {
		long result = 0L;
		int b = 0;
		int bitpos = 0;
		for( int i = 0 ; i < bytes ; ++i,bitpos += 8 ) {
			b = read8Bit();
			result |= b << bitpos;
		}
		if( signed && ( ( b & 0x80 ) != 0 ) ) {
			for( int i = bytes*8 ; i < 64 ; i += 8 )
				result |= 0xFFL << i;
		}
		return result;
	}

/**
  * Reads a variable-length floating-point number with given length. The
  * number is zero-extended to the right. The IEEE 754 floating-point number
  * will be packed into the long returned from the highest bits of the long.
  * @param bytes Number of bytes to read.
  * @return The number packed from the highest bytes of a long number
  * @throws IOException in case of I/O error
  */
	public long readFloatingPointVLNWithLength( int bytes ) 
										throws IOException {
		long result = 0L;
		int bitpos = 0;
		for( int i = 0 ; i < bytes ; ++i, bitpos += 8 ) {
			int b = read8Bit();
			result |= (long)b << bitpos;
		}
        result <<= ( 8 - bytes ) * 8;
		return result;
	}
	

/**
  * Reads a variable-length signed number and returns it. If the current byte of the VLN
  * has bit 7 set, there will be an additional byte.
  * @return the VLN as a long
  * @throws IOException in case of I/O error
  */
    public long readSignedVLN() throws IOException {
        int bitpos = 0;
        long vln = 0L;
        do {
            int inp = read8Bit();
            vln |= ((long)( inp & 0x7F )) << bitpos;
            bitpos += 7;
            if( ( inp & 0x80 ) == 0 )
                break;
        } while( true );
        if( ( ( 1L << ( bitpos - 1 ) ) & vln ) != 0 )
            vln -= ( 1L << bitpos );
        return vln;
    }

/**
  * Reads a variable-length file offset.
  * @return the file offset
  */
    public long readFileOffsetVLN() throws IOException {
        long r = readVLN();
        return r == 0L ? 0L : r+getOdexOffset();
    }


/**
  * Writes the dump message into the file. If there the dump base
  * offset is valid, the offset will also be dumped along with the words
  * in the dump buffer.
  * @param messageToDump message to write into the dump file.
  */
    public void dump( String messageToDump ) {
        if( dump == null )
            return;
        if( dumpOn && ( dumpBaseOffset >= 0L ) )
            dump.print( dumpLong( dumpBaseOffset )+" " );
        if( dumpOn && ( dumpStorage.size() > 0 ) ) {
            dump.print( ":\t" );
            for( int i = 0 ; i < dumpStorage.size() ; ++i ) {
                dump.print( dumpByte( 
                        dumpStorage.get( i ).intValue() ) );
                dump.print( " " );
                if( ( i % 4 ) == 3 )
                    dump.print( "\n\t\t" );
            }
            dumpStorage.clear();
            dumpBaseOffset = -1L;
        }
        dump.println( messageToDump );
    }

    public void parse( long pos ) throws IOException,UnknownInstructionException {
        file.seek( pos );
        parse();
    }

    public abstract void parse() 
            throws IOException,UnknownInstructionException;

/**
  * Read a byte, compare with the expected value and throw an exception if
  * the value read does not match with the expected value.
  * @param expectedValue Expected value
  * @throws IOException In case of I/O failure or if the value does not match.
  */
    public int parseExpected8Bit( int expectedValue ) throws IOException {
        int w = read8Bit();
        if( w != expectedValue )
            throw new IOException( 
                    "Value read: 0x"+
                    dumpByte( w )+
                    "; value expected: 0x"+
                    dumpByte( expectedValue )+
                    "; file offset: 0x"+
                    dumpLong( file.getFilePointer() - 1L ) );
        return w;
    }

/**
  * Read a byte, compare with a list of expected values and throw an exception if
  * the value read does not match with the expected value.
  * @param expectedValue Array of multiple expected values
  * @return The index of the element in the expectedValue array that finally matched.
  * @throws IOException In case of I/O failure or if the value does not match.
  */
    public int parseExpected8BitValues( int expectedValue[] ) throws IOException {
        int w = read8Bit();
        for( int i = 0 ; i < expectedValue.length ; ++i )
            if( w == expectedValue[i] )
                return i;
        throw new IOException( 
                    "Value read: 0x"+
                    dumpByte( w )+
                    "; value expected: "+
                    dumpBytes( expectedValue )+
                    "; file offset: 0x"+
                    dumpLong( file.getFilePointer() - 1L ) );
    }

/**
  * Read a word, compare with the expected value and throw an exception if
  * the value read does not match with the expected value.
  * @param expectedValue Expected value
  * @throws IOException In case of I/O failure or if the value does not match.
  */
    public void parseExpected16Bit( int expectedValue ) throws IOException {
        int w = read16Bit();
        if( w != expectedValue )
            throw new IOException( 
                    "Value read: 0x"+
                    dumpWord( w )+
                    "; value expected: 0x"+
                    dumpWord( expectedValue )+
                    "; file offset: 0x"+
                    dumpLong( file.getFilePointer() - 2L ) );
    }

/**
  * Read a 32 bit valua, compare with the expected value and throw an exception if
  * the value read does not match with the expected value.
  * @param expectedValue Expected value
  * @throws IOException In case of I/O failure or if the value does not match.
  */
    public void parseExpected32Bit( long expectedValue ) throws IOException {
        long w = read32Bit();
        if( w != expectedValue )
            throw new IOException( 
                    "Value read: 0x"+
                    dumpLong( w )+
                    "; value expected: 0x"+
                    dumpLong( expectedValue )+
                    "; file offset: 0x"+
                    dumpLong( file.getFilePointer() - 4L ) );
    }

// ------------------ private methods -----------------------------

// This method assumes that the long is actually a 32 bit integer.
// The reason is that we use this for offset dumping and we assume that
// the file is shorter than 4Gbyte.
    public String dumpLong( long l ) {
        StringBuilder b = new StringBuilder();
        to8DigitHexNumber( b,(int)( l & 0xFFFFFFFFL ) );
        return new String( b );
    }

// Convenience method for displaying a word (lower 16 bit of the
// parameter.
    public String dumpWord( int word ) {
        StringBuilder b = new StringBuilder();
        to4DigitHexNumber( b,word );
        return new String( b );
    }

// Convenience method for displaying a byte (lower 8 bit of the parameter
    public String dumpByte( int b ) {
        StringBuilder s = new StringBuilder();
        to2DigitHexNumber( s,b );
        return new String( s );
    }

// Convenience method for displaying an array of bytes
    public String dumpBytes( int b[] ) {
        StringBuilder s = new StringBuilder();
        s.append( "[" );
        for( int i = 0 ; i < b.length ; ++i ) {
            if( i > 0 )
                s.append( "," );
            s.append( "0x" );
            to2DigitHexNumber( s,b[i] );
        }
        s.append( "]" );
        return new String( s );
    }

    private void to8DigitHexNumber( StringBuilder buffer, int number ) {
        to4DigitHexNumber( buffer, number >> 16 );
        to4DigitHexNumber( buffer, number );
    }

    private void to4DigitHexNumber( StringBuilder buffer, int number ) {
        to2DigitHexNumber( buffer, number >> 8 );
        to2DigitHexNumber( buffer, number );
    }

    private void to2DigitHexNumber( StringBuilder buffer, int number ) {
        buffer.append( to1DigitHexNumber( number >> 4 ) );
        buffer.append( to1DigitHexNumber( number ) );
    }

    private char to1DigitHexNumber( int number ) {
        int offset = number & 0xF;
        return hexChars.charAt( offset );
    }

    private long getOdexOffset() {
        if( dexOptimizationData == null )
            return 0L;
        return dexOptimizationData.getDexOffset();
    }

    private static final String hexChars = "0123456789ABCDEF";


}
