/**
  * DexDependencyFile.java
  * Class representing one DEX file the DEX file under disassembly depends on
  */
package hu.uw.pallergabor.dedexer;

import java.io.*;

public class DexDependencyFile {

    public DexDependencyFile( 
            RandomAccessFile raf,
            PrintStream dexLogStream ) {
        this.raf = raf;
        this.dexLogStream = dexLogStream;
    }

    public void parse() throws IOException {
            dsb = new DexSignatureBlock();
            dsb.setRandomAccessFile( raf );
            dsb.setDumpFile( dexLogStream );
            dsb.parse();

            dpb = new DexPointerBlock();
            dpb.setRandomAccessFile( raf );
            dpb.setDumpFile( dexLogStream );
            dpb.setDexSignatureBlock( dsb );
            dpb.parse();

            dstrb = new DexStringIdsBlock();
            dstrb.setRandomAccessFile( raf );
            dstrb.setDumpFile( dexLogStream );
            dstrb.setDexPointerBlock( dpb );
            dstrb.setDexSignatureBlock( dsb );
            dstrb.parse();

            dtb = new DexTypeIdsBlock();
            dtb.setRandomAccessFile( raf );
            dtb.setDumpFile( dexLogStream );
            dtb.setDexPointerBlock( dpb );
            dtb.setDexStringIdsBlock( dstrb );
            dtb.parse();

            dpib = new DexProtoIdsBlock();
            dpib.setRandomAccessFile( raf );
            dpib.setDumpFile( dexLogStream );
            dpib.setDexPointerBlock( dpb );
            dpib.setDexStringIdsBlock( dstrb );
            dpib.setDexTypeIdsBlock( dtb );
            dpib.setDexSignatureBlock( dsb );
            dpib.parse();

            dfb = new DexFieldIdsBlock();
            dfb.setRandomAccessFile( raf );
            dfb.setDumpFile( dexLogStream );
            dfb.setDexPointerBlock( dpb );
            dfb.setDexStringIdsBlock( dstrb );
            dfb.setDexTypeIdsBlock( dtb );
            dfb.parse();

            dmb = new DexMethodIdsBlock();
            dmb.setRandomAccessFile( raf );
            dmb.setDumpFile( dexLogStream );
            dmb.setDexPointerBlock( dpb );
            dmb.setDexStringIdsBlock( dstrb );
            dmb.setDexTypeIdsBlock( dtb );
            dmb.setDexProtoIdsBlock( dpib );
            dmb.parse();

            dcb = new DexClassDefsBlock();
            dcb.setRandomAccessFile( raf );
            dcb.setDumpFile( dexLogStream );
            dcb.setDexPointerBlock( dpb );
            dcb.setDexStringIdsBlock( dstrb );
            dcb.setDexTypeIdsBlock( dtb );
            dcb.setDexFieldIdsBlock( dfb );
            dcb.setDexMethodIdsBlock( dmb );
            dcb.setDexSignatureBlock( dsb );
            dcb.parse();
            dexOffsetResolver.addToOffsetResolver( dcb );
    }

    public void setDexOffsetResolver( DexOffsetResolver dexOffsetResolver ) {
        this.dexOffsetResolver = dexOffsetResolver;
    }

    private RandomAccessFile raf;
    private PrintStream dexLogStream;
    private DexOffsetResolver dexOffsetResolver;

    DexSignatureBlock dsb;
    DexPointerBlock dpb;
    DexStringIdsBlock dstrb;
    DexTypeIdsBlock dtb;
    DexProtoIdsBlock dpib;
    DexFieldIdsBlock dfb;
    DexMethodIdsBlock dmb;
    DexClassDefsBlock dcb;
}