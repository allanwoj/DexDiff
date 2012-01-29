package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexFieldIdsBlock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexFieldIdsBlockTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexFieldIdsBlock    dfb;
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
        dsb.parse();
        dtb = new DexTypeIdsBlock();
        dtb.setRandomAccessFile( raf );
        dtb.setDumpFile( System.out );
        dtb.setDexPointerBlock( dpb );
        dtb.setDexStringIdsBlock( dsb );
        dtb.parse();
        dfb = new DexFieldIdsBlock();
        dfb.setRandomAccessFile( raf );
        dfb.setDumpFile( System.out );
        dfb.setDexPointerBlock( dpb );
        dfb.setDexStringIdsBlock( dsb );
        dfb.setDexTypeIdsBlock( dtb );
    }

    @Test
    public void testFieldIdsParser() throws IOException {
        dfb.parse();
        assertEquals( 17,dfb.getFieldsSize() );
        assertEquals( "Test3.array [I",dfb.getField( 0 ) );
        assertEquals( "array [I",dfb.getFieldShortName( 0 ) );
        assertEquals( "Test3.cs1 C", dfb.getField( 7 ) );
        assertEquals( "cs1 C", dfb.getFieldShortName( 7 ) );
        assertEquals( "Test3.value I", dfb.getField( 16 ) );
        assertEquals( "value I", dfb.getFieldShortName( 16 ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
