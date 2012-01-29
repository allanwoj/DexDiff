package hu.uw.pallergabor.dedexertest;
/**
  * Unit test for DexAnnotationParser
  */

import org.junit.*;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.File;
import java.util.*;
import hu.uw.pallergabor.dedexer.*;

public class DexAnnotationTest extends Assert {
    DexSignatureBlock dsigb;
    DexPointerBlock dpb;
    DexStringIdsBlock dsb;
    DexTypeIdsBlock dtb;
    DexProtoIdsBlock    dpib;
    DexMethodIdsBlock    dmb;
    DexFieldIdsBlock    dfb;
    DexAnnotationParser dab;
    RandomAccessFile raf;
    String testBase;

    @Before
    public void setup() throws IOException,UnknownInstructionException {
        testBase = System.getProperty( DedexerSuite.PN_TESTBASE );
        assertNotNull( testBase );
        raf = new RandomAccessFile( testBase+
                            File.separator+
                            "annotation.dex","r" );
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
        dab = new DexAnnotationParser();
        dab.setRandomAccessFile( raf );
        dab.setDexTypeIdsBlock( dtb );
        dab.setDexStringIdsBlock( dsb );
        dab.setDexFieldIdsBlock( dfb );
        dab.setDexMethodIdsBlock( dmb );
        dab.setDumpFile( System.out );
    }

    @Test
    public void testTest9Annotation() throws IOException,UnknownInstructionException {
        dab.parse( 0x46C );
        assertEquals( 1, dab.getAnnotationBlocksSize( DexAnnotationParser.AnnotationType.FIELD ) );
        assertEquals( 2, dab.getAnnotationBlocksSize( DexAnnotationParser.AnnotationType.METHOD ) );
        assertEquals( 1, dab.getAnnotationBlocksSize( DexAnnotationParser.AnnotationType.PARAMETER ) );
        assertEquals( 3, dab.getAnnotationBlocksSize( DexAnnotationParser.AnnotationType.CLASS ) );
        assertEquals( 1, dab.getAnnotationsSize( DexAnnotationParser.AnnotationType.FIELD, 0 ) );
// Fields
        assertEquals( "Test9.field I",
                dab.getAnnotationAsset( DexAnnotationParser.AnnotationType.FIELD, 0 ) );
        int v = dab.getAnnotationVisibilityFlag( DexAnnotationParser.AnnotationType.FIELD,
                                                0, 0 );
        assertEquals( DexAnnotationParser.VISIBILITY_BUILD, v );
        String type = dab.getAnnotationType( DexAnnotationParser.AnnotationType.FIELD,
                                                0, 0 );
        assertEquals( "LFieldAnnotation;",type );
        assertEquals( 1,dab.getAnnotationElementsSize( DexAnnotationParser.AnnotationType.FIELD,
                                    0, 0 ) );
        assertEquals( "value",
                dab.getAnnotationElementName( 
                    DexAnnotationParser.AnnotationType.FIELD,
                                    0, 0, 0 ) );
        String stringValue = dab.getAnnotationElementValue( 
                    DexAnnotationParser.AnnotationType.FIELD,
                                    0, 0, 0 ).toString();
        assertEquals( "\"importantField\"",stringValue );
// Methods
        assertEquals( "Test9/method3",
                dab.getAnnotationAsset( DexAnnotationParser.AnnotationType.METHOD, 1 ) );
        v = dab.getAnnotationVisibilityFlag( DexAnnotationParser.AnnotationType.METHOD,
                                                1, 1 );
        assertEquals( DexAnnotationParser.VISIBILITY_SYSTEM, v );
        type = dab.getAnnotationType( DexAnnotationParser.AnnotationType.METHOD,
                                                1, 1 );
        assertEquals( "Ldalvik/annotation/Throws;",type );
        assertEquals( "value",
                dab.getAnnotationElementName( 
                    DexAnnotationParser.AnnotationType.METHOD,
                                    1, 1, 0 ) );
        Object elements[] = (Object[])dab.getAnnotationElementValue( 
                    DexAnnotationParser.AnnotationType.METHOD,
                                    1, 1, 0 );
        assertEquals( 1,elements.length );
        stringValue = (String)elements[0];
        assertEquals( "Ljava/io/IOException;",stringValue );
// Parameters
        assertEquals( "Test9/method2",
                dab.getAnnotationAsset( DexAnnotationParser.AnnotationType.PARAMETER, 0 ) );
        v = dab.getAnnotationVisibilityFlag( DexAnnotationParser.AnnotationType.PARAMETER,
                                                0, 1 );
        assertEquals( DexAnnotationParser.VISIBILITY_BUILD, v );
        type = dab.getAnnotationType( DexAnnotationParser.AnnotationType.PARAMETER,
                                                0, 1 );
        assertEquals( "LParameterAnnotation2;", type );
        int parameterIndex = dab.getAnnotationParameterIndex( 
                        DexAnnotationParser.AnnotationType.PARAMETER,
                        0, 1 );
        assertEquals( 3,parameterIndex );
// Class
        v = dab.getAnnotationVisibilityFlag( DexAnnotationParser.AnnotationType.CLASS,
                                                2, 0 );
        assertEquals( DexAnnotationParser.VISIBILITY_SYSTEM, v );
        type = dab.getAnnotationType( DexAnnotationParser.AnnotationType.CLASS,
                                                2, 0 );
        assertEquals( "Ldalvik/annotation/MemberClasses;", type );
        assertEquals( 1,dab.getAnnotationElementsSize( DexAnnotationParser.AnnotationType.CLASS,
                                    2, 0 ) );
// Maps
        assertEquals( 0,dab.getBlockIndexFromAsset( 
                DexAnnotationParser.AnnotationType.FIELD, "Test9.field I" ) );
        assertEquals( 1,dab.getBlockIndexFromAsset( 
                DexAnnotationParser.AnnotationType.METHOD, "Test9/method3" ) );
        assertEquals( 0,dab.getBlockIndexFromAsset( 
                DexAnnotationParser.AnnotationType.PARAMETER, "Test9/method2" ) );
    }

    @After
    public void teardown() throws IOException {
        raf.close();
    }

}
