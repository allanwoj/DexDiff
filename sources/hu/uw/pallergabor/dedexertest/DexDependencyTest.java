package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexDependencyParser
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexDependencyTest extends Assert {
    DexSignatureBlock dsb;
    DexDependencyParser ddp;
    RandomAccessFile raf;
    String testBase;


    @Before
    public void setup() throws IOException {
        testBase = System.getProperty( DedexerSuite.PN_TESTBASE );
        assertNotNull( testBase );
        dsb = new DexSignatureBlock();
        raf = new RandomAccessFile( testBase+
                            File.separator+
                            "input.odex","r" );
        dsb.setRandomAccessFile( raf );
        dsb.setDumpFile( System.out );
        ddp = new DexDependencyParser();
        ddp.setRandomAccessFile( raf );
        ddp.setDumpFile( System.out );
        ddp.setDexSignatureBlock( dsb );
    }

    @Test
    public void testSignatureParser() throws IOException {
        dsb.parse();
        DexOptimizationData optData = dsb.getDexOptimizationData();
        assertNotNull( optData );
        assertTrue( optData.isOptimized() );
        ddp.setDexOptimizationData( optData );
        ddp.parse();
        assertEquals( ddp.getDependencySize(), 5 );
        assertEquals( 
            "/data/dalvik-cache/system@framework@core.jar@classes.dex",
            ddp.getDependencyElement( 0 ) );
        assertEquals(
            "/data/dalvik-cache/system@framework@services.jar@classes.dex",
            ddp.getDependencyElement( 4 ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
