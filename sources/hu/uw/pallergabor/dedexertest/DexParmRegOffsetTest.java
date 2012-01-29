package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for DexClassDefsBlock.getMethodParameterOffsets method
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.util.*;
import hu.uw.pallergabor.dedexer.*;

public class DexParmRegOffsetTest extends Assert {
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
                            "test2.dex","r" );
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
    private static final int TEST3_IDX = 5;
    private static final int TEST4_IDX = 6;

    @Test
    public void testGetMethodParameterOffsets() throws IOException {
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
        assertEquals( 
            "public complexParms2(Ljava/lang/String;Ljava/lang/String;JILjava/lang/Object;)V",
            dcb.getVirtualMethodName( TEST3_IDX,5 ) );
        assertEquals(
            "public complexParms(Ljava/lang/String;Ljava/lang/String;JID)V",
            dcb.getVirtualMethodName( TEST3_IDX,4 ) );
        assertEquals(
            "public booleanArray()V",
            dcb.getVirtualMethodName( TEST3_IDX,0 ) );

        ArrayList complexParms = dcb.getVirtualMethodParameterOffsets( TEST3_IDX,4, 0x0A );
        System.out.println( "complexParms: "+complexParms );

        assertEquals( complexParms.size(),10 );
        assertEquals( ((Integer)complexParms.get( 0 )).intValue(),3 );
        assertEquals( ((String)complexParms.get( 1 ) ),"Ljava/lang/String;" );
        assertEquals( ((Integer)complexParms.get( 8 )).intValue(),8 );
        assertEquals( ((String)complexParms.get( 9 ) ),"D" );

        ArrayList complexParms2 = dcb.getVirtualMethodParameterOffsets( TEST3_IDX,5, 9 );
        System.out.println( "complexParms2: "+complexParms2 );

        assertEquals( complexParms2.size(),10 );
        assertEquals( ((Integer)complexParms2.get( 0 )).intValue(),3 );
        assertEquals( ((String)complexParms2.get( 1 ) ),"Ljava/lang/String;" );
        assertEquals( ((Integer)complexParms2.get( 8 )).intValue(),8 );
        assertEquals( ((String)complexParms2.get( 9 ) ),"Ljava/lang/Object;" );

        ArrayList nullParms = dcb.getVirtualMethodParameterOffsets( TEST3_IDX,0, 0 );
        assertEquals( nullParms.size(), 0 );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
