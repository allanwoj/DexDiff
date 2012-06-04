package android;

/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//package com.android.dx.io.instructions;

//import com.android.dx.io.IndexType;
//import com.android.dx.io.OpcodeInfo;
//import com.android.dx.io.Opcodes;
//import com.android.dx.util.DexException;
//import com.android.dx.util.Hex;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import patch.MapManager;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * A decoded Dalvik instruction. This consists of a format codec, a
 * numeric opcode, an optional index type, and any additional
 * arguments of the instruction. The additional arguments (if any) are
 * represented as uninterpreted data.
 *
 * <p><b>Note:</b> The names of the arguments are <i>not</i> meant to
 * match the names given in the Dalvik instruction format
 * specification, specification which just names fields (somewhat)
 * arbitrarily alphabetically from A. In this class, non-register
 * fields are given descriptive names and register fields are
 * consistently named alphabetically.</p>
 */
public class DecodedInstruction {
    /** non-null; instruction format / codec */
    private final InstructionCodec format;

    /** opcode number */
    private final int opcode;

    /** constant index argument */
    private final int index;

    /** null-ok; index type */
    private final IndexType indexType;

    /**
     * target address argument. This is an absolute address, not just
     * a signed offset. <b>Note:</b> The address is unsigned, even
     * though it is stored in an {@code int}.
     */
    private final int target;

    /**
     * literal value argument; also used for special verification error
     * constants (formats 20bc and 40sc) as well as should-be-zero values
     * (formats 10x, 20t, 30t, and 32x)
     */
    private final long literal;
    
    private ArrayList<Byte> data;

    /**
     * Decodes an instruction from the given input source.
     */
    public static DecodedInstruction decode(CodeInput in) throws EOFException {
        long opcodeUnit = in.read16Bit();
        int opcode = Opcodes.extractOpcodeFromUnit((int)opcodeUnit);
        InstructionCodec format = OpcodeInfo.getFormat(opcode);

        return format.decode(opcodeUnit, in);
    }

    /**
     * Decodes an array of instructions. The result has non-null
     * elements at each offset that represents the start of an
     * instruction.
     */
    public static Collection<DecodedInstruction> decodeAll(byte[] encodedInstructions) {
        int size = encodedInstructions.length;
        ArrayList<DecodedInstruction> decoded = new ArrayList<DecodedInstruction>();
        ShortArrayCodeInput in = new ShortArrayCodeInput(encodedInstructions);

        try {
        	int count = 0;
            while (in.hasMore()) {
                //decoded[count++]= DecodedInstruction.decode(in);
            	decoded.add(DecodedInstruction.decode(in));
            }
        } catch (EOFException ex) {
            throw new AssertionError("shouldn't happen");
        }

        return decoded;
    }

    /**
     * Constructs an instance.
     */
    public DecodedInstruction(InstructionCodec format, int opcode,
            int index, IndexType indexType, int target, long literal,
            ArrayList<Byte> data) {
        if (format == null) {
            throw new NullPointerException("format == null");
        }

        if (!Opcodes.isValidShape(opcode)) {
            throw new IllegalArgumentException("invalid opcode");
        }

        this.format = format;
        this.opcode = opcode;
        this.index = index;
        this.indexType = indexType;
        this.target = target;
        this.literal = literal;
        this.data = data;
    }

    public final InstructionCodec getFormat() {
        return format;
    }

    public final int getOpcode() {
        return opcode;
    }

    /**
     * Gets the opcode, as a code unit.
     */
    public final short getOpcodeUnit() {
        return (short) opcode;
    }

    public final int getIndex() {
        return index;
    }

    /**
     * Gets the index, as a code unit.
     */
    public final short getIndexUnit() {
        return (short) index;
    }

    public final IndexType getIndexType() {
        return indexType;
    }

    /**
     * Gets the raw target.
     */
    public final int getTarget() {
        return target;
    }

    /**
     * Gets the target as a relative offset from the given address.
     */
    public final int getTarget(int baseAddress) {
        return target - baseAddress;
    }

    /**
     * Gets the target as a relative offset from the given base
     * address, as a code unit. This will throw if the value is out of
     * the range of a signed code unit.
     * @throws DexException 
     */
    public final short getTargetUnit(int baseAddress) throws DexException {
        int relativeTarget = getTarget(baseAddress);

        if (relativeTarget != (short) relativeTarget) {
            throw new DexException("Target out of range: "
                    + Hex.s4(relativeTarget));
        }

        return (short) relativeTarget;
    }

    /**
     * Gets the target as a relative offset from the given base
     * address, masked to be a byte in size. This will throw if the
     * value is out of the range of a signed byte.
     * @throws DexException 
     */
    public final int getTargetByte(int baseAddress) throws DexException {
        int relativeTarget = getTarget(baseAddress);

        if (relativeTarget != (byte) relativeTarget) {
            throw new DexException("Target out of range: "
                    + Hex.s4(relativeTarget));
        }

        return relativeTarget & 0xff;
    }

    public final long getLiteral() {
        return literal;
    }

    /**
     * Gets the literal value, masked to be an int in size. This will
     * throw if the value is out of the range of a signed int.
     * @throws DexException 
     */
    public final int getLiteralInt() throws DexException {
        if (literal != (int) literal) {
            throw new DexException("Literal out of range: " + Hex.u8(literal));
        }

        return (int) literal;
    }

    /**
     * Gets the literal value, as a code unit. This will throw if the
     * value is out of the range of a signed code unit.
     * @throws DexException 
     */
    public final short getLiteralUnit() throws DexException {
        if (literal != (short) literal) {
            throw new DexException("Literal out of range: " + Hex.u8(literal));
        }

        return (short) literal;
    }

    /**
     * Gets the literal value, masked to be a byte in size. This will
     * throw if the value is out of the range of a signed byte.
     * @throws DexException 
     */
    public final int getLiteralByte() throws DexException {
        if (literal != (byte) literal) {
            throw new DexException("Literal out of range: " + Hex.u8(literal));
        }

        return (int) literal & 0xff;
    }

    /**
     * Gets the literal value, masked to be a nibble in size. This
     * will throw if the value is out of the range of a signed nibble.
     * @throws DexException 
     */
    public final int getLiteralNibble() throws DexException {
        if ((literal < -8) || (literal > 7)) {
            throw new DexException("Literal out of range: " + Hex.u8(literal));
        }

        return (int) literal & 0xf;
    }

    public int getRegisterCount() {
    	return 1; // Implement properly
    }

    public int getA() {
        return 0;
    }

    public int getB() {
        return 0;
    }

    public int getC() {
        return 0;
    }

    public int getD() {
        return 0;
    }

    public int getE() {
        return 0;
    }

    /**
     * Gets the register count, as a code unit. This will throw if the
     * value is out of the range of an unsigned code unit.
     * @throws DexException 
     */
    public final short getRegisterCountUnit() throws DexException {
        int registerCount = getRegisterCount();

        if ((registerCount & ~0xffff) != 0) {
            throw new DexException("Register count out of range: "
                    + Hex.u8(registerCount));
        }

        return (short) registerCount;
    }

    /**
     * Gets the A register number, as a code unit. This will throw if the
     * value is out of the range of an unsigned code unit.
     * @throws DexException 
     */
    public final short getAUnit() throws DexException {
        int a = getA();

        if ((a & ~0xffff) != 0) {
            throw new DexException("Register A out of range: " + Hex.u8(a));
        }

        return (short) a;
    }

    /**
     * Gets the A register number, as a byte. This will throw if the
     * value is out of the range of an unsigned byte.
     * @throws DexException 
     */
    public final short getAByte() throws DexException {
        int a = getA();

        if ((a & ~0xff) != 0) {
            throw new DexException("Register A out of range: " + Hex.u8(a));
        }

        return (short) a;
    }

    /**
     * Gets the A register number, as a nibble. This will throw if the
     * value is out of the range of an unsigned nibble.
     * @throws DexException 
     */
    public final short getANibble() throws DexException {
        int a = getA();

        if ((a & ~0xf) != 0) {
            throw new DexException("Register A out of range: " + Hex.u8(a));
        }

        return (short) a;
    }

    /**
     * Gets the B register number, as a code unit. This will throw if the
     * value is out of the range of an unsigned code unit.
     * @throws DexException 
     */
    public final short getBUnit() throws DexException {
        int b = getB();

        if ((b & ~0xffff) != 0) {
            throw new DexException("Register B out of range: " + Hex.u8(b));
        }

        return (short) b;
    }

    /**
     * Gets the B register number, as a byte. This will throw if the
     * value is out of the range of an unsigned byte.
     * @throws DexException 
     */
    public final short getBByte() throws DexException {
        int b = getB();

        if ((b & ~0xff) != 0) {
            throw new DexException("Register B out of range: " + Hex.u8(b));
        }

        return (short) b;
    }

    /**
     * Gets the B register number, as a nibble. This will throw if the
     * value is out of the range of an unsigned nibble.
     * @throws DexException 
     */
    public final short getBNibble() throws DexException {
        int b = getB();

        if ((b & ~0xf) != 0) {
            throw new DexException("Register B out of range: " + Hex.u8(b));
        }

        return (short) b;
    }

    /**
     * Gets the C register number, as a code unit. This will throw if the
     * value is out of the range of an unsigned code unit.
     * @throws DexException 
     */
    public final short getCUnit() throws DexException {
        int c = getC();

        if ((c & ~0xffff) != 0) {
            throw new DexException("Register C out of range: " + Hex.u8(c));
        }

        return (short) c;
    }

    /**
     * Gets the C register number, as a byte. This will throw if the
     * value is out of the range of an unsigned byte.
     * @throws DexException 
     */
    public final short getCByte() throws DexException {
        int c = getC();

        if ((c & ~0xff) != 0) {
            throw new DexException("Register C out of range: " + Hex.u8(c));
        }

        return (short) c;
    }

    /**
     * Gets the C register number, as a nibble. This will throw if the
     * value is out of the range of an unsigned nibble.
     * @throws DexException 
     */
    public final short getCNibble() throws DexException {
        int c = getC();

        if ((c & ~0xf) != 0) {
            throw new DexException("Register C out of range: " + Hex.u8(c));
        }

        return (short) c;
    }

    /**
     * Gets the D register number, as a code unit. This will throw if the
     * value is out of the range of an unsigned code unit.
     * @throws DexException 
     */
    public final short getDUnit() throws DexException {
        int d = getD();

        if ((d & ~0xffff) != 0) {
            throw new DexException("Register D out of range: " + Hex.u8(d));
        }

        return (short) d;
    }

    /**
     * Gets the D register number, as a byte. This will throw if the
     * value is out of the range of an unsigned byte.
     * @throws DexException 
     */
    public final short getDByte() throws DexException {
        int d = getD();

        if ((d & ~0xff) != 0) {
            throw new DexException("Register D out of range: " + Hex.u8(d));
        }

        return (short) d;
    }

    /**
     * Gets the D register number, as a nibble. This will throw if the
     * value is out of the range of an unsigned nibble.
     * @throws DexException 
     */
    public final short getDNibble() throws DexException {
        int d = getD();

        if ((d & ~0xf) != 0) {
            throw new DexException("Register D out of range: " + Hex.u8(d));
        }

        return (short) d;
    }

    /**
     * Gets the E register number, as a nibble. This will throw if the
     * value is out of the range of an unsigned nibble.
     * @throws DexException 
     */
    public final short getENibble() throws DexException {
        int e = getE();

        if ((e & ~0xf) != 0) {
            throw new DexException("Register E out of range: " + Hex.u8(e));
        }

        return (short) e;
    }
    
    public ArrayList<Byte> getData() {
    	return data;
    }
    
    public Collection<Byte> getOutput(MapManager mm) {
    	ArrayList<Byte> ret = new ArrayList<Byte>();
    	int index = 0;
    	if (opcode == 0x1A) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.stringIndexMap[index]));
    	} else if (opcode == 0x1B) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(5), data.get(4), data.get(3), data.get(2));
    		ret.addAll(write32bit((int)mm.stringIndexMap[index]));
    	} else if (opcode == 0x1C) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    	} else if (opcode == 0x1f) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    	} else if (opcode == 0x20) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    	} else if (opcode == 0x22) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    	} else if (opcode == 0x23) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    	} else if (opcode == 0x24) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    		ret.add(data.get(4));
    		ret.add(data.get(5));
    	} else if (opcode == 0x25) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.typeIndexMap[index]));
    		ret.add(data.get(4));
    		ret.add(data.get(5));
    		return data;
    	} else if (opcode >= 0x52 && opcode <= 0x5f) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.fieldIndexMap[index]));
    	} else if (opcode >= 0x60 && opcode <= 0x6D) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.fieldIndexMap[index]));
    	} else if (opcode >= 0x6E && opcode <= 0x72) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.methodIndexMap[index]));
    		ret.add(data.get(4));
    		ret.add(data.get(5));
    	} else if (opcode >= 0x74 && opcode <= 0x78) {
    		ret.add(data.get(0));
    		ret.add(data.get(1));
    		index = combine(data.get(3), data.get(2));
    		ret.addAll(write16bit((int)mm.methodIndexMap[index]));
    		ret.add(data.get(4));
    		ret.add(data.get(5));
    	} else {
    		ret = data;
    	}
    	
    	return ret;
    }
    
    public boolean isEqual(DecodedInstruction other, MapManager mm) {
    	ArrayList<Byte> ret = new ArrayList<Byte>();
    	Collection<Byte> temp;
    	int index = 0;
    	int otherIndex = 0;
    	if (data.get(0) != other.getData().get(0) || data.get(1) != other.getData().get(1) ||
    			data.size() != other.getData().size()) {
    		return false;
    	}
    	if (opcode == 0x1A) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.stringIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x1B) {
    		index = combine(data.get(5), data.get(4), data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(5), other.getData().get(4), other.getData().get(3), other.getData().get(2));
    		if ((int)mm.stringIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x1C) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x1f) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x20) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x22) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x23) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode == 0x24) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex || data.get(4) != other.getData().get(4) || data.get(5) != other.getData().get(5))
    			return false;
    	} else if (opcode == 0x25) {
    		System.out.println("eep!");
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.typeIndexMap[index] != otherIndex || data.get(4) != other.getData().get(4) || data.get(5) != other.getData().get(5))
    			return false;
    	} else if (opcode >= 0x52 && opcode <= 0x5f) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.fieldIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode >= 0x60 && opcode <= 0x6D) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.fieldIndexMap[index] != otherIndex)
    			return false;
    	} else if (opcode >= 0x6E && opcode <= 0x72) {
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.methodIndexMap[index] != otherIndex || data.get(4) != other.getData().get(4) || data.get(5) != other.getData().get(5))
    			return false;
    	} else if (opcode >= 0x74 && opcode <= 0x78) {//
    		index = combine(data.get(3), data.get(2));
    		otherIndex = combine(other.getData().get(3), other.getData().get(2));
    		if ((int)mm.methodIndexMap[index] != otherIndex || data.get(4) != other.getData().get(4) || data.get(5) != other.getData().get(5))
    			return false;
    	} else {
    		Iterator<Byte> it = data.iterator();
    		Iterator<Byte> otherIt = other.getData().iterator();
    		while (it.hasNext()) {
    			if (it.next() != otherIt.next())
    				return false;
    		}
    	}
    	
    	return true;
    }
    
    private int combine(byte a, byte b) {
    	return ((a << 8) & 0x0000ff00) | (b & 0x000000ff);
    }
    
    private int combine(byte a, byte b, byte c, byte d) {
    	return ((a << 24) & 0xff000000)  | ((b << 16) & 0x00ff0000)  | ((c << 8) & 0x0000ff00) | (d & 0x000000ff);
    }
    
    public Collection<Byte> write16bit(int data) {
		ArrayList<Byte> output = new ArrayList<Byte>();
		
		for(int i = 0; i < 2; ++i) {
			output.add((byte)((data >> (i*8)) & 0xFF));
		}

		return output;
	}
    
    public Collection<Byte> write32bit(long data) {
    	ArrayList<Byte> output = new ArrayList<Byte>();
		
		for(int i = 0; i < 4; ++i) {
			output.add((byte)((data >> (i*8)) & 0xFF));
		}

		return output;
	}

    /**
     * Encodes this instance to the given output.
     */
    //public final void encode(CodeOutput out) {
    //    format.encode(this, out);
    //}

    /**
     * Returns an instance just like this one, except with the index replaced
     * with the given one.
     */
    //public abstract DecodedInstruction withIndex(int newIndex);
}

