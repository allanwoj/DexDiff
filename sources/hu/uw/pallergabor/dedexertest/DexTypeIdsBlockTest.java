package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexTypeIdsBlock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexTypeIdsBlockTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
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
    }

    @Test
    public void testTypeIdsParser() throws IOException {
        dtb.parse();
        assertEquals( 18,dtb.getTypesSize() );
        assertEquals( "B",dtb.getType( 0 ) );
        assertEquals( "Ljava/lang/String;",dtb.getType( 10 ) );
        assertEquals( "[Z",dtb.getType( 17 ) );
    }

    @Test
    public void testClassNameConversion() throws IOException {
        dtb.parse();
        assertEquals( "java/lang/String", dtb.getClassName( 10 ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
