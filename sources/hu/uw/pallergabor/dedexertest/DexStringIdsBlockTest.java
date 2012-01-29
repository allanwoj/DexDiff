package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexStringIdsBlock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexStringIdsBlockTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    RandomAccessFile raf;
    String testBase;

    @Before
    public void setup() throws IOException,UnknownInstructionException {
        testBase = System.getProperty( DedexerSuite.PN_TESTBASE );
        assertNotNull( testBase );
        raf = new RandomAccessFile( testBase+
                            File.separator+
                            "test1.dex","r" );
        dsigb = new DexSignatureBlock();
        dsigb.setRandomAccessFile( raf );
        dsigb.setDumpFile( System.out );
        dsigb.parse();
        dpb = new DexPointerBlock();
        dpb.setRandomAccessFile( raf );
        dpb.setDumpFile( System.out );
        dpb.setDexSignatureBlock( dsigb );
        dpb.parse( 0x20L );
        dsb = new DexStringIdsBlock();
        dsb.setRandomAccessFile( raf );
        dsb.setDumpFile( System.out );
        dsb.setDexPointerBlock( dpb );
        dsb.setDexSignatureBlock( dsigb );
    }

    @Test
    public void testStringIdsParser() throws IOException {
        dsb.parse();
        assertEquals( 59,dsb.getStringsSize() );
        assertEquals( "<init>",dsb.getString( 0 ) );
        assertEquals( "byteArray",dsb.getString( 31 ) );
        assertEquals( "value",dsb.getString( 58 ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
