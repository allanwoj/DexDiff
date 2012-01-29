package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexTryCatchBlockParser
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexTryCatchBlockParserTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexMethodHeadParser dmh;
    DexTryCatchBlockParser dtcb;
    RandomAccessFile raf;
    String testBase;

    @Before
    public void setup() throws IOException,UnknownInstructionException {
        testBase = System.getProperty( DedexerSuite.PN_TESTBASE );
        assertNotNull( testBase );
        raf = new RandomAccessFile( testBase+
                            File.separator+
                            "exceptiontest.dex","r" );
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
        dsb.parse();
        dtb = new DexTypeIdsBlock();
        dtb.setRandomAccessFile( raf );
        dtb.setDumpFile( System.out );
        dtb.setDexPointerBlock( dpb );
        dtb.setDexStringIdsBlock( dsb );
        dtb.parse();
    }

    @Test
    public void testTryCatchBlockParser() 
                    throws IOException,UnknownInstructionException {
        dmh = new DexMethodHeadParser();
        dmh.setRandomAccessFile( raf );
        dmh.setDumpOff();
        dmh.setDexSignatureBlock( dsigb );
        dmh.parse( 0x408L );
        dtcb = new DexTryCatchBlockParser();
        dtcb.setRandomAccessFile( raf );
        dtcb.setDexMethodHeadParser( dmh );
        dtcb.setDexTypeIdsBlock( dtb );
        dtcb.setDumpFile( System.out );
        assertEquals( 0x468L, dmh.getNextBlockOffset() );
        dtcb.parse();
        assertEquals( 3,dtcb.getTriesSize() );
        assertEquals( 0x418L,dtcb.getTryStartOffset( 0 ) );
        assertEquals( 0x426L,dtcb.getTryEndOffset( 0 ) );
        assertEquals( 0x430L,dtcb.getTryStartOffset( 2 ) );
        assertEquals( 0x452L,dtcb.getTryEndOffset( 2 ) );
        assertEquals( 2,dtcb.getTryHandlersSize( 1 ) );
        assertEquals( "java/io/IOException", dtcb.getTryHandlerType( 1,1 ) );
        assertEquals( 0x452L, dtcb.getTryHandlerOffset( 1,1 ) );
        assertEquals( "java/io/FileNotFoundException", dtcb.getTryHandlerType( 1,0 ) );
        assertEquals( 0x440L, dtcb.getTryHandlerOffset( 1,0 ) );
    }

    @Test
    public void testAnyCatchBlockParser() 
                    throws IOException,UnknownInstructionException {
        dmh = new DexMethodHeadParser();
        dmh.setRandomAccessFile( raf );
        dmh.setDumpOff();
        dmh.setDexSignatureBlock( dsigb );
        dmh.parse( 0x280L );
        dtcb = new DexTryCatchBlockParser();
        dtcb.setRandomAccessFile( raf );
        dtcb.setDexMethodHeadParser( dmh );
        dtcb.setDexTypeIdsBlock( dtb );
        dtcb.setDumpFile( System.out );
        assertEquals( 0x2C0L, dmh.getNextBlockOffset() );
        dtcb.parse();
        assertEquals( 1,dtcb.getTryHandlersSize( 0 ) );
        assertEquals( "java/lang/Exception",dtcb.getTryHandlerType( 0,0 ) );
        assertEquals( 0x2baL, dtcb.getTryHandlerOffset( 0,0 ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
