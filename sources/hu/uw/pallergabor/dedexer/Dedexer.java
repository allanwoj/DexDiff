/**
  * Main Java application class
  */
package hu.uw.pallergabor.dedexer;
import java.io.*;
import java.util.*;

public class Dedexer {
    PrintStream dexLogStream = null;
    ArrayList<DexDependencyFile> depFiles = new ArrayList<DexDependencyFile>();

    public static void main( String args[] ) {
        Dedexer dedexer = new Dedexer();
        dedexer.run( args );
    }

    public void run( String args[] ) {
        String targetDir = null;
        String depsDir = null;
        String sourceFile = null;
        boolean debugMode = false;
        boolean dexlog = false;
        boolean regTraceLog = false;
        boolean regTracing = false;
        for( int i = 0 ; i < args.length ; ++i ) {
            String arg = args[i];
            if( arg.equals( "-D" ) )
                debugMode = true;
            else
            if( arg.equals( "-o" ) )
                dexlog = true;
            else
            if( arg.equals( "-r" ) ) {
                regTraceLog = true;
                regTracing = true;
            } else
            if( arg.equals( "-d" ) && ( i < args.length - 1 ) ) {
                targetDir = args[i+1];
                ++i;
            } else
            if( arg.equals( "-e" ) && ( i < args.length - 1 ) ) {
                depsDir = args[i+1];
                ++i;
                regTracing = true;
            } else
            if( sourceFile == null )
                sourceFile = arg;
            else {
                usage();
                return;
            }
        }
        if( ( targetDir == null ) ||
            ( sourceFile == null ) ) {
            usage();
            return;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile( sourceFile,"r" );
            if( dexlog )
                dexLogStream = new PrintStream( targetDir+"/dex.log" );

            DexSignatureBlock dsb = new DexSignatureBlock();
            dsb.setRandomAccessFile( raf );
            dsb.setDumpFile( dexLogStream );
            dsb.parse();

            DexDependencyParser depsParser = null;
            DexOffsetResolver resolver = null;
            if( ( depsDir != null ) && 
                ( dsb.getDexOptimizationData() != null ) &&
                dsb.getDexOptimizationData().isOptimized() ) {
                depsParser = new DexDependencyParser();
                depsParser.setDexSignatureBlock( dsb );
                depsParser.setRandomAccessFile( raf );
                depsParser.setDumpFile( dexLogStream );
                depsParser.parse();
                resolver = new DexOffsetResolver();
                resolver.setDumpFile( dexLogStream );
                if( handleDependencies( depsDir, depsParser, resolver  ) ) {
                    close();
                    return;
                }
            }

            DexPointerBlock dpb = new DexPointerBlock();
            dpb.setRandomAccessFile( raf );
            dpb.setDumpFile( dexLogStream );
            dpb.setDexSignatureBlock( dsb );
            dpb.parse();

            DexStringIdsBlock dstrb = new DexStringIdsBlock();
            dstrb.setRandomAccessFile( raf );
            dstrb.setDumpFile( dexLogStream );
            dstrb.setDexPointerBlock( dpb );
            dstrb.setDexSignatureBlock( dsb );
            dstrb.parse();

            DexTypeIdsBlock dtb = new DexTypeIdsBlock();
            dtb.setRandomAccessFile( raf );
            dtb.setDumpFile( dexLogStream );
            dtb.setDexPointerBlock( dpb );
            dtb.setDexStringIdsBlock( dstrb );
            dtb.parse();

            DexProtoIdsBlock dpib = new DexProtoIdsBlock();
            dpib.setRandomAccessFile( raf );
            dpib.setDumpFile( dexLogStream );
            dpib.setDexPointerBlock( dpb );
            dpib.setDexStringIdsBlock( dstrb );
            dpib.setDexTypeIdsBlock( dtb );
            dpib.setDexSignatureBlock( dsb );
            dpib.parse();

            DexFieldIdsBlock dfb = new DexFieldIdsBlock();
            dfb.setRandomAccessFile( raf );
            dfb.setDumpFile( dexLogStream );
            dfb.setDexPointerBlock( dpb );
            dfb.setDexStringIdsBlock( dstrb );
            dfb.setDexTypeIdsBlock( dtb );
            dfb.parse();

            DexMethodIdsBlock dmb = new DexMethodIdsBlock();
            dmb.setRandomAccessFile( raf );
            dmb.setDumpFile( dexLogStream );
            dmb.setDexPointerBlock( dpb );
            dmb.setDexStringIdsBlock( dstrb );
            dmb.setDexTypeIdsBlock( dtb );
            dmb.setDexProtoIdsBlock( dpib );
            dmb.parse();

            DexClassDefsBlock dcb = new DexClassDefsBlock();
            dcb.setRandomAccessFile( raf );
            dcb.setDumpFile( dexLogStream );
            dcb.setDexPointerBlock( dpb );
            dcb.setDexStringIdsBlock( dstrb );
            dcb.setDexTypeIdsBlock( dtb );
            dcb.setDexFieldIdsBlock( dfb );
            dcb.setDexMethodIdsBlock( dmb );
            dcb.setDexSignatureBlock( dsb );
            dcb.parse();
            if( resolver != null )
                resolver.addToOffsetResolver( dcb );

            JasminStyleCodeGenerator jscg = new JasminStyleCodeGenerator();
            jscg.setDexStringIdsBlock( dstrb );
            jscg.setDexSignatureBlock( dsb );
            jscg.setDexTypeIdsBlock( dtb );
            jscg.setDexFieldIdsBlock( dfb );
            jscg.setDexMethodIdsBlock( dmb );
            jscg.setDexClassDefsBlock( dcb );
            jscg.setGeneratedSourceBaseDir( targetDir );
            jscg.setDexOffsetResolver( resolver );
            jscg.setRandomAccessFile( raf );
            jscg.setDumpFile( dexLogStream );
            jscg.setRegTracing( regTracing );
            jscg.setRegTraceLog( regTraceLog );
            jscg.generate();
            raf.close();
        } catch( IOException ex ) {
            System.err.println( "I/O error: "+ex.getMessage() );
            if( debugMode )
                ex.printStackTrace();
        } catch( UnknownInstructionException ex ) {
            System.err.println( ex.getMessage() );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
        close();
    }

    private void usage() {
        System.err.println( "Usage: java -jar ddx.jar -o -D -r -d <destination directory> <source>" );
        System.err.println( "       <destination directory> is where the generated files will be placed." );
        System.err.println( "       <source> is the name of the source DEX file." );
        System.err.println( "       -D - if present, more detailed error report is printed in case of failure." );
        System.err.println( "       -o - if present, detailed log file will be created about the input DEX file (dex.log)." );
        System.err.println( "       -r - if present, register trace will be emitted after each instruction" );
        System.err.println( "       -e <deps> - if present, the <deps> directory is supposed to contain dependencies "+
                            "necessary for ODEX disassembly. Read the manual for details." );
    }

    private void close() {
        if( dexLogStream != null )
                dexLogStream.close();
    }

    private boolean handleDependencies( 
                        String depsDir,
                        DexDependencyParser depsParser,
                        DexOffsetResolver resolver ) 
                                                    throws IOException {
        boolean error = false;
        for( int i = 0 ; i < depsParser.getDependencySize() ; ++i ) {
            String dependencyFileName = depsParser.getDependencyElement( i );
            int slashIndex = dependencyFileName.lastIndexOf( '/' );
            String shortFileName = slashIndex < 0 ? 
                                    dependencyFileName :
                                    dependencyFileName.substring( slashIndex+1 );
            RandomAccessFile raf = null;
            File f = null;
            try {
                f = new File( depsDir, shortFileName );
                raf = new RandomAccessFile( f,"r" );
                System.out.println( "Reading dependency file "+
                                f+
                                " (derived from ODEX file dependency "+
                                dependencyFileName+
                                ")" );
                DexDependencyFile depFile = 
                        new DexDependencyFile( raf, dexLogStream );
                depFile.setDexOffsetResolver( resolver );
                depFile.parse();
                depFiles.add( depFile );
                raf.close();
            } catch( IOException ex ) {
                System.err.println( "Cannot open dependency file: "+
                                    f+
                                    " (derived from ODEX file dependency "+
                                    dependencyFileName+")" );
                error = true;
            }
        }
        return error;
    }


}
