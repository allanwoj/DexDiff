package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexInstructionParser
  */

import org.junit.*;
import java.io.*;
import java.util.*;
import hu.uw.pallergabor.dedexer.*;

public class DexInstructionParserTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexProtoIdsBlock    dpib;
    DexMethodIdsBlock    dmb;
    DexFieldIdsBlock    dfb;
    DexInstructionParser    dib;
    RandomAccessFile raf;
    String testBase;
    ByteArrayOutputStream testOutput;

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
        dib = new DexInstructionParser();
        dib.setRandomAccessFile( raf );
        dib.setDumpFile( System.out );
        dib.setDexStringIdsBlock( dsb );
        dib.setDexTypeIdsBlock( dtb );
        dib.setDexFieldIdsBlock( dfb );
        dib.setDexMethodIdsBlock( dmb );
    }

    @Test
    public void testInstrParser() throws IOException,UnknownInstructionException {
        String lineseparator = System.getProperty( "line.separator" );
        initOutputStream();
        dib.parse( 0x374L );
        assertEquals( "\tconst/4\tv1,0"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse();
        assertEquals( "\tconst-string\tv0,\"hello.txt\""+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse();
        assertEquals( 
            "\tinvoke-direct\t{v2,v0},java/io/File/<init>\t; <init>(Ljava/lang/String;)V"+lineseparator,
            getOutputStream() );

        initOutputStream();
        dib.parse( 0x382L );
        assertEquals( "\tnew-array\tv0,v0,[I"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse();
        assertEquals( "\tfill-array-data\tv0,l3a0"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse();
        assertEquals( "\tiput-object\tv0,v2,Test3.array [I"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x39eL );
        assertEquals( "\treturn-void\t"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x3c6L );
        assertEquals( "\tconst/16\tv0,10"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x3ceL );
        assertEquals( "\taput-boolean\tv1,v0,v1"+lineseparator,getOutputStream() );

/*
        initOutputStream();
        dib.parse( 0x418L );
        assertEquals( "\tpacked-switch\tv2\r\n",getOutputStream() );
*/

        initOutputStream();
        dib.parse( 0x420L );
        assertEquals( "\treturn\tv0"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x424L );
        assertEquals( "\tgoto\tl420"+lineseparator,getOutputStream() );

/*
        initOutputStream();
        dib.parse( 0x454L );
        assertEquals( "\tsparse-switch\tv2\r\n",getOutputStream() );
*/

        initOutputStream();
        dib.parse( 0x49cL );
        assertEquals( "\tinstance-of\tv0,v4,Test3"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x4a0L );
        assertEquals( "\tif-nez\tv0,l4aa"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse();
        assertEquals( "\tmove\tv0,v2"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x4aaL );
        assertEquals( "\tcheck-cast\tv4,Test3"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x4b6L );
        assertEquals( "\tif-ne\tv0,v1,l4be"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x4ecL );
        assertEquals( "\tnew-instance\tv0,java/io/IOException"+lineseparator,
                    getOutputStream() );

        initOutputStream();
        dib.parse( 0x51cL );
        assertEquals( "\tadd-int/lit8\tv0,v0,1"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x56cL );
        assertEquals( "\tconst-class\tv0,Test3"+lineseparator,getOutputStream() );

        initOutputStream();
        dib.parse( 0x588L );
        assertEquals( "\tsget\tv0,Test3.is1 I"+lineseparator,getOutputStream() );
    }


    @After
    public void teardown() throws IOException {
        raf.close();
    }

    private void initOutputStream() {
        testOutput = new ByteArrayOutputStream();
        dib.setDumpFile( new PrintStream( testOutput ) );
        dib.setDumpOff();
    }

    private String getOutputStream() {
        String result = testOutput.toString();
        System.out.print( result );
        return result;
    }

    private void dumpStringAsHex( String string ) {
        for( int i = 0 ; i < string.length() ; ++i ) {
            char c = string.charAt( i );
            System.out.print( Integer.toHexString( (int)c ) );
            System.out.print( " " );
        }
        System.out.println();
    }
}
