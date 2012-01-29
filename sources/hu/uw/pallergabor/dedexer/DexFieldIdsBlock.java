/**
  * Parses the field id table
  */

package hu.uw.pallergabor.dedexer;
import java.io.IOException;

public class DexFieldIdsBlock extends DexParser {

    public void parse() throws IOException {
        int fieldsSize = (int)dexPointerBlock.getFieldIdsSize();
        file.seek( dexPointerBlock.getFieldIdsOffset() );
        fields = new FieldHolder[ fieldsSize ];
        for( int i = 0 ; i < fieldsSize ; ++i ) {
            int classTypeIdx = read16Bit();
            int typeIdx = read16Bit();
            int nameStringIdx = (int)read32Bit();
            FieldHolder fieldHolder = new FieldHolder();
            fieldHolder.className = dexTypeIdsBlock.getClassName( classTypeIdx );
            fieldHolder.fieldName = dexStringIdsBlock.getString( nameStringIdx );
            fieldHolder.type = dexTypeIdsBlock.getType( typeIdx );
            fields[i] = fieldHolder;
            dump( "field["+i+"]: "+getField( i ) );
        }
    }

    public int getFieldsSize() {
        return (int)dexPointerBlock.getFieldIdsSize();
    }

    public String getField( int idx ) {
        FieldHolder fieldHolder = fields[idx];
        StringBuilder b = new StringBuilder();
// Class name
        b.append( fieldHolder.className );
        b.append( '.' );
// Field name
        b.append( fieldHolder.fieldName );
        b.append( ' ' );
// Type
        b.append( fieldHolder.type );
        return new String( b );
    }

    public String getFieldName( int idx ) {
        FieldHolder fieldHolder = fields[idx];
        return fieldHolder.fieldName;
    }

    public String getFieldShortName( int idx ) {
        FieldHolder fieldHolder = fields[idx];
        StringBuilder b = new StringBuilder();
// Field name
        b.append( fieldHolder.fieldName );
        b.append( ' ' );
// Type
        b.append( fieldHolder.type );
        return new String( b );
    }

    public String getFieldType( int idx ) {
        FieldHolder fieldHolder = fields[idx];
        return fieldHolder.type;
    }


    public void setDexPointerBlock( DexPointerBlock dexPointerBlock ) {
        this.dexPointerBlock = dexPointerBlock;
    }

    public void setDexStringIdsBlock( DexStringIdsBlock dexStringIdsBlock ) {
        this.dexStringIdsBlock = dexStringIdsBlock;
    }

    public void setDexTypeIdsBlock( DexTypeIdsBlock dexTypeIdsBlock ) {
        this.dexTypeIdsBlock = dexTypeIdsBlock;
    }

    private FieldHolder         fields[];
    private DexPointerBlock     dexPointerBlock = null;
    private DexStringIdsBlock   dexStringIdsBlock = null;
    private DexTypeIdsBlock     dexTypeIdsBlock = null;

    class FieldHolder {
        String className;
        String fieldName;
        String type;
    }
}
