/**
  * Parses a try-catch block of a method
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexTryCatchBlockParser extends DexParser {

    public void parse() throws IOException {
        file.seek( dexMethodHeadParser.getNextBlockOffset() );
        int triesNumber = dexMethodHeadParser.getTriesSize();
        triesList = new TryHolder[ triesNumber ];
        for( int i = 0 ; i < triesNumber ; ++i ) {
            long tryBase = read32Bit();
            int trySpan = read16Bit();
            TryHolder tryHolder = new TryHolder();
            tryHolder.startOffset = 
                dexMethodHeadParser.getInstructionBase() + ( tryBase * 2 );
            tryHolder.endOffset = tryHolder.startOffset + ( trySpan * 2 );
            dump( "start offset: 0x"+Long.toHexString( tryHolder.startOffset )+
                    "; end offset: 0x"+Long.toHexString( tryHolder.endOffset ) );
            tryHolder.catchOffset = read16Bit();
            tryHolder.catchIndex = -1;
            dump( "catch offset: "+tryHolder.catchOffset );
            triesList[i] = tryHolder;
        }
        long catchTableBase = file.getFilePointer();
        int handlerSize = (int)readVLN();        
        catchList = new CatchHolder[ handlerSize ];
        for( int i = 0 ; i < handlerSize ; ++i ) {
            int catchItemOffset = (int)( file.getFilePointer() - catchTableBase );
            int encodedItems = (int)readSignedVLN();
            int realSize = encodedItems;
            boolean anyHandlerPresent = false;
            if( encodedItems == 0 ) {
                realSize = 1;
                anyHandlerPresent = true;
            }
            else
            if( encodedItems < 0 ) {
                encodedItems = -encodedItems;
                realSize = encodedItems + 1;
                anyHandlerPresent = true;
            }
            CatchHolder catchHolder = new CatchHolder();
            catchHolder.catchOffset = catchItemOffset;
            dump( "Handler #"+i+"; offset: "+catchItemOffset );
            CatchItemHolder catchItemList[] = new CatchItemHolder[ realSize ];
            for( int n = 0 ; n < encodedItems ; ++n ) {
                CatchItemHolder catchItemHolder = new CatchItemHolder();
                catchItemHolder.typeidx = (int)readVLN();
                catchItemHolder.offset = 
                    dexMethodHeadParser.getInstructionBase() +  ( 2*readVLN() );
                catchItemList[n] = catchItemHolder;
                dump( "Exception: "+
                        dexTypeIdsBlock.getClassName( catchItemHolder.typeidx )+
                        "; offset: "+
                        Long.toHexString( catchItemHolder.offset ) );
            }
            if( anyHandlerPresent ) {
                CatchItemHolder catchItemHolder = new CatchItemHolder();
                catchItemHolder.typeidx = -1;
                catchItemHolder.offset = 
                    dexMethodHeadParser.getInstructionBase() +  ( 2*readVLN() );
                catchItemList[ realSize-1 ] = catchItemHolder;
                dump( "Exception: <any>; offset: "+
                        Long.toHexString( catchItemHolder.offset ) );
            }
            catchHolder.items = catchItemList;
// Now resolve TryHolders and set their catchIndex according to catchOffset in
// both TryHolders and CatchHolders. This code is not very efficient but we are
// not in a hurry and these tables are quite small.
            for( int n = 0 ; n < triesNumber ; ++n )
                if( triesList[n].catchOffset == catchItemOffset )
                    triesList[n].catchIndex = i;
            catchList[i] = catchHolder;
        }
    }

    public void setDexMethodHeadParser( DexMethodHeadParser dexMethodHeadParser ) {
        this.dexMethodHeadParser = dexMethodHeadParser;
    }

    public void setDexTypeIdsBlock( DexTypeIdsBlock dexTypeIdsBlock ) {
        this.dexTypeIdsBlock = dexTypeIdsBlock;
    }

    public int getTriesSize() {
        return triesList.length;
    }

/**
  * Gets the starting offset of a try block.
  * @param idx Index of the try block
  * @return file offset of the starting offset of the try block
  */
    public long getTryStartOffset( int idx ) {
        return triesList[ idx ].startOffset;
    }

/**
  * Gets the end offset of a try block.
  * @param idx Index of the try block
  * @return file offset of the end offset of the try block
  */
    public long getTryEndOffset( int idx ) {
        return triesList[ idx ].endOffset;
    }

/**
  * Gets the number of handler block belonging to a try block.
  * @param idx The index of the try block
  * @return Size of the handler block belonging to a try block.
  */
    public int getTryHandlersSize( int idx ) {
        int holderIdx = triesList[ idx ].catchIndex;
        if( holderIdx < 0 )
            return 0;
        CatchHolder catchHolder = catchList[ holderIdx ];
        return catchHolder.items.length;
    }

/**
  * Gets the exception type associated with a given handler block
  * @param tryidx Index of the try block
  * @param handleridx Index of the handler block within the given try block.
  * @return class name of the exception associated to the given handler block.
  */
    public String getTryHandlerType( int tryidx, int handleridx ) {
        int holderIdx = triesList[ tryidx ].catchIndex;
        if( holderIdx < 0 )
            return null;
        CatchHolder catchHolder = catchList[ holderIdx ];
        CatchItemHolder catchItemHolder = catchHolder.items[ handleridx ];
        int typeidx = catchItemHolder.typeidx;
        String typeName = typeidx < 0 ? 
                        "java/lang/Exception" :
                        dexTypeIdsBlock.getClassName( typeidx );
        return typeName;
    }

/**
  * Gets the file offset associated with a given handler block
  * @param tryidx Index of the try block
  * @param handleridx Index of the handler block within the given try block.
  * @return class name of the exception associated to the given handler block.
  */
    public long getTryHandlerOffset( int tryidx, int handleridx ) {
        int holderIdx = triesList[ tryidx ].catchIndex;
        if( holderIdx < 0 )
            return 0;
        CatchHolder catchHolder = catchList[ holderIdx ];
        CatchItemHolder catchItemHolder = catchHolder.items[ handleridx ];
        return catchItemHolder.offset;
    }

    private DexMethodHeadParser dexMethodHeadParser;
    private DexTypeIdsBlock     dexTypeIdsBlock;
    private TryHolder triesList[];
    private CatchHolder catchList[];

    class TryHolder {
        long startOffset;
        long endOffset;
        int catchOffset;
        int catchIndex;
    }

    class CatchHolder {
        CatchItemHolder items[];
        int catchOffset;
    }

    class CatchItemHolder {
        int typeidx;
        long offset;
    }
}
