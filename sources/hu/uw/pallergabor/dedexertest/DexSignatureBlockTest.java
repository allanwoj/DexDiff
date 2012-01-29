package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexSignatureBlock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexSignatureBlockTest extends Assert {
    DexSignatureBlock dsb;
    RandomAccessFile raf;
    String testBase;
    int expectedSignature[] = {
        0x11, 0x4b, 0x77, 0x0a, 0x0f, 0xff,
        0xa5, 0xc5, 0x0b, 0x09, 0x53, 0x9b,
        0xf5, 0x17, 0x39, 0xd6, 0xea, 0x24,
        0x06, 0x9f
    };


    @Before
    public void setup() throws IOException {
        testBase = System.getProperty( DedexerSuite.PN_TESTBASE );
        assertNotNull( testBase );
        dsb = new DexSignatureBlock();
        raf = new RandomAccessFile( testBase+
                            File.separator+
                            "test1.dex","r" );
        dsb.setRandomAccessFile( raf );
        dsb.setDumpFile( System.out );
    }

    @Test
    public void testSignatureParser() throws IOException {
        dsb.parse();
        long fpos = raf.getFilePointer();
        assertEquals( 0x20L,fpos );
        long checksum = dsb.getChecksum();
        assertEquals( 0xc9f63cc0L,checksum );
        int signature[] = dsb.getSignature();
        assertArrayEquals( "signature does not match",
            expectedSignature,
            signature );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
