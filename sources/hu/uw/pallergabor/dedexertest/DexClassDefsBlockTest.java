package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for the DexClassDefslock
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.util.*;
import hu.uw.pallergabor.dedexer.*;

public class DexClassDefsBlockTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexProtoIdsBlock    dpib;
    DexMethodIdsBlock    dmb;
    DexFieldIdsBlock    dfb;
    DexClassDefsBlock   dcb;
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
        dcb = new DexClassDefsBlock();
        dcb.setRandomAccessFile( raf );
        dcb.setDumpFile( System.out );
        dcb.setDexPointerBlock( dpb );
        dcb.setDexStringIdsBlock( dsb );
        dcb.setDexTypeIdsBlock( dtb );
        dcb.setDexFieldIdsBlock( dfb );
        dcb.setDexMethodIdsBlock( dmb );
        dcb.setDexSignatureBlock( dsigb );
    }

// Class indexes of two test classes in this DEX file
// Note: Test3 having index of 3 and Test4 having index of 4 is completely
// accidental.
    private static final int TEST3_IDX = 3;
    private static final int TEST4_IDX = 4;

    @Test
    public void testTest3Parser() throws IOException {
        dcb.parse();
        Iterator<Integer> it = dcb.getClassIterator();
        boolean test3Found = false;
        while( it.hasNext() ) {
            Integer i = it.next();
            if( i.intValue() == TEST3_IDX ) {
                test3Found = true;
                break;
            }
        }
        assertTrue( test3Found );
        assertFalse( dcb.isInterface( TEST3_IDX ) );
        assertEquals( "Test3",dcb.getClassName( TEST3_IDX ) );
        assertEquals( "java/io/File",dcb.getSuperClass( TEST3_IDX ) );
        assertEquals( "Test3.java",dcb.getSourceName( TEST3_IDX ) );
        assertEquals( 0,dcb.getInterfacesSize( TEST3_IDX ) );
        assertEquals( 6,dcb.getStaticFieldsSize( TEST3_IDX ) );
        assertEquals( "static bls1 Z",dcb.getStaticField( TEST3_IDX, 0 ) );
        assertEquals( "static ss1 S",dcb.getStaticField( TEST3_IDX, 5 ) );
        assertEquals( 11,dcb.getInstanceFieldsSize( TEST3_IDX ) );
        assertEquals( "array [I",dcb.getInstanceField( TEST3_IDX,0 ) );
        assertEquals( "public value I",dcb.getInstanceField( TEST3_IDX,10 ) );
        assertEquals( 1, dcb.getDirectMethodsFieldsSize( TEST3_IDX ) );
        assertEquals( "public <init>()V",dcb.getDirectMethodName( TEST3_IDX,0 ) );
        assertEquals( 0x364L,dcb.getDirectMethodOffset( TEST3_IDX,0 ) );
        assertEquals( 16,dcb.getVirtualMethodsFieldsSize( TEST3_IDX ) );
        assertEquals( "public booleanArray()V",dcb.getVirtualMethodName( TEST3_IDX,0 ) );
        assertEquals( 0x3B4L, dcb.getVirtualMethodOffset( TEST3_IDX,0 ) );
        assertEquals( "public synchronized equals(Ljava/lang/Object;)Z",
                        dcb.getVirtualMethodName( TEST3_IDX,4 ) );
        assertEquals( 0x488L, dcb.getVirtualMethodOffset( TEST3_IDX,4 ) );
        assertEquals( "public shortArray()V",dcb.getVirtualMethodName( TEST3_IDX,15 ) );
        assertEquals( 0x66CL, dcb.getVirtualMethodOffset( TEST3_IDX,15 ) );
    }

    @Test
    public void testTest4Parser() throws IOException {
        dcb.parse();
        Iterator<Integer> it = dcb.getClassIterator();
        boolean test4Found = false;
        while( it.hasNext() ) {
            Integer i = it.next();
            if( i.intValue() == TEST4_IDX ) {
                test4Found = true;
                break;
            }
        }
        assertTrue( test4Found );
        assertFalse( dcb.isInterface( TEST4_IDX ) );
        assertEquals( "Test4",dcb.getClassName( TEST4_IDX ) );
        assertEquals( "java/lang/Object",dcb.getSuperClass( TEST4_IDX ) );
        assertEquals( "Test4.java",dcb.getSourceName( TEST4_IDX ) );
        assertEquals( 0,dcb.getInterfacesSize( TEST4_IDX ) );
        assertEquals( 0,dcb.getStaticFieldsSize( TEST4_IDX ) );
        assertEquals( 0,dcb.getInstanceFieldsSize( TEST4_IDX ) );
        assertEquals( 1, dcb.getDirectMethodsFieldsSize( TEST4_IDX ) );
        assertEquals( "public <init>()V",dcb.getDirectMethodName( TEST4_IDX,0 ) );
        assertEquals( 0x698L,dcb.getDirectMethodOffset( TEST4_IDX,0 ) );
        assertEquals( 0,dcb.getVirtualMethodsFieldsSize( TEST4_IDX ) );
    }


    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
