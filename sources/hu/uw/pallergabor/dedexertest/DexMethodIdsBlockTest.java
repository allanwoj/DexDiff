package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexMethodIdsBlock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexMethodIdsBlockTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexProtoIdsBlock    dpib;
    DexMethodIdsBlock    dmb;
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
        dpib = new DexProtoIdsBlock();
        dpib.setRandomAccessFile( raf );
        dpib.setDumpFile( System.out );
        dpib.setDexPointerBlock( dpb );
        dpib.setDexStringIdsBlock( dsb );
        dpib.setDexTypeIdsBlock( dtb );
        dpib.setDexSignatureBlock( dsigb );
        dpib.parse();
        dmb = new DexMethodIdsBlock();
        dmb.setRandomAccessFile( raf );
        dmb.setDumpFile( System.out );
        dmb.setDexPointerBlock( dpb );
        dmb.setDexStringIdsBlock( dsb );
        dmb.setDexTypeIdsBlock( dtb );
        dmb.setDexProtoIdsBlock( dpib );
    }

    @Test
    public void testMethodIdsParser() throws IOException {
        dmb.parse();
        assertEquals( 21,dmb.getMethodsSize() );
        assertEquals( "Test3/<init>",dmb.getMethod( 0 ) );
        assertEquals( "<init>()V",dmb.getProto( 0 ) );
        assertEquals( "Test3/equals",dmb.getMethod( 5 ) );
        assertEquals( "equals(Ljava/lang/Object;)Z",dmb.getProto( 5 ) );
        assertEquals( "java/lang/Object/<init>",dmb.getMethod( 20 ) );
        assertEquals( "<init>()V",dmb.getProto( 20 ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
