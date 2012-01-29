package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexPointerBlock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexPointerBlockTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    RandomAccessFile raf;
    String testBase;

    @Before
    public void setup() throws IOException {
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
    }

    @Test
    public void testPointerParser() throws IOException,UnknownInstructionException {
        dpb.parse( 0x20L );
        long fpos = raf.getFilePointer();
        assertEquals( 0x70L,fpos );
        assertEquals( 0xaacL, dpb.getFileSize() );
        assertEquals( 0x70L, dpb.getHeaderSize() );
        assertEquals( 0L, dpb.getLinkSize() );
        assertEquals( 0L, dpb.getLinkOffset() );
        assertEquals( 0x9e8L, dpb.getMapOffset() );
        assertEquals( 0x3bL, dpb.getStringIdsSize() );
        assertEquals( 0x70L, dpb.getStringIdsOffset() );
        assertEquals( 0x12L, dpb.getTypeIdsSize() );
        assertEquals( 0x15cL, dpb.getTypeIdsOffset() );
        assertEquals( 0x6L, dpb.getProtoIdsSize() );
        assertEquals( 0x1a4L, dpb.getProtoIdsOffset() );
        assertEquals( 0x11L, dpb.getFieldIdsSize() );
        assertEquals( 0x1ecL, dpb.getFieldIdsOffset() );
        assertEquals( 0x15L, dpb.getMethodIdsSize() );
        assertEquals( 0x274L, dpb.getMethodIdsOffset() );
        assertEquals( 0x2L, dpb.getClassDefsSize() );
        assertEquals( 0x31cL, dpb.getClassDefsOffset() );
        assertEquals( 0x750L, dpb.getDataSize() );
        assertEquals( 0x35cL, dpb.getDataOffset() );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
