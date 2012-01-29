/**
  * Collection of Dedexer test classes
  */
package hu.uw.pallergabor.dedexertest;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses(
    { 
        DexSignatureBlockTest.class,
        DexPointerBlockTest.class,
        DexStringIdsBlockTest.class,
        DexTypeIdsBlockTest.class,
        DexProtoIdsBlockTest.class,
        DexFieldIdsBlockTest.class,
        DexMethodIdsBlockTest.class,
        DexClassDefsBlockTest.class,
        DexInstructionParserTest.class,
        DexMethodHeadParserTest.class,
        DexTryCatchBlockParserTest.class,
		DexEncodedArrayParserTest.class,
        DexDebugInfoParserTest.class,
        DexParmRegOffsetTest.class,
        DexDependencyTest.class
    }
)
public class DedexerSuite {
/**
  * Name of the testbase system property
  */
    public static final String PN_TESTBASE = "testbase";
}
