package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexDebugInfoParser
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import hu.uw.pallergabor.dedexer.*;

public class DexDebugInfoParserTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexDebugInfoParser ddp;
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
    }

    @Test
    public void testDebugInfoParser() 
                    throws IOException,UnknownInstructionException {
        ddp = new DexDebugInfoParser();
        ddp.setRandomAccessFile( raf );
        ddp.setDumpFile( System.out );
        ddp.setDexStringIdsBlock( dsb );
        ddp.setDexTypeIdsBlock( dtb );
        ddp.parse( 0x93CL );
        assertEquals( 5,ddp.getLineNumbers() );

        assertEquals( 162,ddp.getLineNumber( 0 ) );
        assertEquals( 0,ddp.getLineNumberAddress( 0 ) );

        assertEquals( 163,ddp.getLineNumber( 1 ) );
        assertEquals( 0x04,ddp.getLineNumberAddress( 1 ) );
    }

    @Test
    public void testDebugInfoParser2() 
                    throws IOException,UnknownInstructionException {
        ddp = new DexDebugInfoParser();
        ddp.setRandomAccessFile( raf );
        ddp.setDumpFile( System.out );
        ddp.setDexStringIdsBlock( dsb );
        ddp.setDexTypeIdsBlock( dtb );
        ddp.parse( 0x8C8L );
        assertEquals( 11,ddp.getLineNumbers() );

        assertEquals( 41,ddp.getLineNumber( 0 ) );
        assertEquals( 0,ddp.getLineNumberAddress( 0 ) );
        assertEquals( 56,ddp.getLineNumber( 2 ) );
        assertEquals( 0x03,ddp.getLineNumberAddress( 2 ) );
        assertEquals( 44,ddp.getLineNumber( 4 ) );
        assertEquals( 0x05,ddp.getLineNumberAddress( 4 ) );
        assertEquals( 42,ddp.getLineNumber( 10 ) );
        assertEquals( 0x0B,ddp.getLineNumberAddress( 10 ) );

        assertEquals( 1,ddp.getLocalVariables() );
        assertEquals( "unnamed0",ddp.getLocalVariableName( 0 ) );
        assertEquals( null,ddp.getLocalVariableType( 0 ) );
        assertEquals( -1,ddp.getLocalVariableRegNum( 0 ) );
        assertEquals( -1,ddp.getLocalVariableStartOffset( 0 ) );
        assertEquals( -1,ddp.getLocalVariableEndOffset( 0 ) );
    }


    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
