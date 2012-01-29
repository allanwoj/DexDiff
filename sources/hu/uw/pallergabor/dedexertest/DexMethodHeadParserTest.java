package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexMethodHeadParser
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexMethodHeadParserTest extends Assert {
    DexMethodHeadParser dmh;
    DexSignatureBlock   dsigb;
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
    }

    @Test
    public void testMethodHeadParser() throws IOException,UnknownInstructionException {
        dmh = new DexMethodHeadParser();
        dmh.setRandomAccessFile( raf );
        dmh.setDexSignatureBlock( dsigb );
        dmh.setDumpOff();
        dmh.parse( 0x5f4L );
        long fpos = raf.getFilePointer();
        assertEquals( 0x604L,fpos );
        assertEquals( 2,dmh.getRegistersSize() );
        assertEquals( 1,dmh.getInputParameters() );
        assertEquals( 0,dmh.getOutputParameters() );
        assertEquals( 0,dmh.getTriesSize() );
        assertEquals( 0x93CL, dmh.getDebugOffset() );
        assertEquals( 0x604L, dmh.getInstructionBase() );
        assertEquals( 0x628L, dmh.getInstructionEnd() );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
