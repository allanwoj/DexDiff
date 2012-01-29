package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexEncodedArrayParser
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexEncodedArrayParserTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexEncodedArrayParser deap;
    DexProtoIdsBlock    dpib;
    DexFieldIdsBlock    dfb;
    DexMethodIdsBlock   dmb;

    RandomAccessFile raf;
    String testBase;

    @Before
    public void setup() throws IOException,UnknownInstructionException {
        testBase = System.getProperty( DedexerSuite.PN_TESTBASE );
        assertNotNull( testBase );
        raf = new RandomAccessFile( testBase+
                            File.separator+
                            "constants.dex","r" );
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
        dfb = new DexFieldIdsBlock();
        dfb.setRandomAccessFile( raf );
        dfb.setDumpFile( System.out );
        dfb.setDexPointerBlock( dpb );
        dfb.setDexStringIdsBlock( dsb );
        dfb.setDexTypeIdsBlock( dtb );
        dfb.parse();
        dmb = new DexMethodIdsBlock();
        dmb.setRandomAccessFile( raf );
        dmb.setDumpFile( System.out );
        dmb.setDexPointerBlock( dpb );
        dmb.setDexStringIdsBlock( dsb );
        dmb.setDexTypeIdsBlock( dtb );
        dmb.setDexProtoIdsBlock( dpib );
        dmb.parse();
    }

    @Test
    public void testEncodedArrayParser() 
                    throws IOException,UnknownInstructionException {
        deap = new DexEncodedArrayParser();
        deap.setRandomAccessFile( raf );
        deap.setDumpFile( System.out );
        deap.setDexStringIdsBlock( dsb );
        deap.setDexTypeIdsBlock( dtb );
        deap.setDexFieldIdsBlock( dfb );
        deap.setDexMethodIdsBlock( dmb );
        deap.parse( 0x23BL );
        assertEquals( deap.getArraySize(),10 );

        Object element = deap.getArrayElement( 0 );
        assertTrue( element instanceof Integer );
        assertEquals( "0", element.toString() );

        element = deap.getArrayElement( 1 );
        assertTrue( element instanceof Integer );
        assertEquals( "2", element.toString() );

        element = deap.getArrayElement( 2 );
        assertEquals( "\"Hello\"", element.toString() );

        element = deap.getArrayElement( 3 );
        assertTrue( element instanceof Boolean );
        assertEquals( "true", element.toString() );

        element = deap.getArrayElement( 4 );
        assertTrue( element instanceof Double );
        assertEquals( "0.1", element.toString() );

        element = deap.getArrayElement( 5 );
        assertTrue( element instanceof Integer );
        assertEquals( "-2", element.toString() );

        element = deap.getArrayElement( 6 );
        assertEquals( "'a'", element.toString() );

        element = deap.getArrayElement( 7 );
        assertTrue( element instanceof Byte );
        assertEquals( "23", element.toString() );

        element = deap.getArrayElement( 8 );
        assertTrue( element instanceof Short );
        assertEquals( "-1000", element.toString() );

        element = deap.getArrayElement( 9 );
        assertTrue( element instanceof Float );
        assertEquals( "-10.24", element.toString() );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
