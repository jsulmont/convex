package convex.core.lang;

import convex.core.data.ACell;
import convex.core.data.Blob;
import convex.core.exceptions.BadFormatException;
import convex.core.lang.ops.Cond;
import convex.core.lang.ops.Constant;
import convex.core.lang.ops.Def;
import convex.core.lang.ops.Do;
import convex.core.lang.ops.Invoke;
import convex.core.lang.ops.Lambda;
import convex.core.lang.ops.Let;
import convex.core.lang.ops.Local;
import convex.core.lang.ops.Lookup;
import convex.core.lang.ops.Query;
import convex.core.lang.ops.Set;
import convex.core.lang.ops.Special;
import convex.core.util.Utils;

/**
 * Static utility class for coded operations.
 * 
 * Ops are the fundamental units of code (e.g. as used to implement Actors), and may be
 * effectively considered as "bytecode" for the decentralised state machine.
 */
public class Ops {
	public static final byte CONSTANT = 1;
	public static final byte INVOKE = 2;
	public static final byte DO = 3;
	public static final byte COND = 4;
	public static final byte LOOKUP = 5;
	public static final byte DEF = 6;
	public static final byte LAMBDA = 7;
	public static final byte LET = 8;
	public static final byte QUERY = 9;
	public static final byte LOOP = 10;
	public static final byte LOCAL=11;
	public static final byte SET = 12;
	// public static final byte CALL = 9;
	// public static final byte RETURN = 10;
	
	public static final byte SPECIAL_BASE = 64;

	

	/**
	 * Reads an Op from the given Blob. Assumes tag specifying an Op already read.
	 * 
	 * @param <T> The return type of the Op
	 * @param b Blob to read from
	 * @param pos Start position in Blob (location of tag byte)
	 * @return New decoded instance
	 * @throws BadFormatException In the event of any encoding error
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ACell> AOp<T> read(Blob b, int pos) throws BadFormatException {
		byte opCode = b.byteAt(pos+1); // second byte identifies Op
		switch (opCode) {
		case Ops.INVOKE:
			return Invoke.read(b,pos);
		case Ops.COND:
			return Cond.read(b,pos);
		case Ops.CONSTANT:
			return Constant.read(b,pos);
		case Ops.DEF:
			return Def.read(b,pos);
		case Ops.DO:
			return Do.read(b,pos);
		case Ops.LOOKUP:
			return Lookup.read(b,pos);
		case Ops.LAMBDA:
			return (AOp<T>) Lambda.read(b,pos);
		case Ops.LET:
			return Let.read(b,pos,false);
		case Ops.QUERY:
			return Query.read(b,pos);
		case Ops.LOOP:
			return Let.read(b,pos,true);
		case Ops.LOCAL:
			return Local.read(b,pos);
		case Ops.SET:
			return Set.read(b,pos);

		// case Ops.RETURN: return (AOp<T>) Return.read(bb);
		default:
			// range 64-127 is special ops
			if ((opCode&0xC0) == 0x40) {
				Special<T> special=(Special<T>) Special.create(opCode);
				if (special==null) throw new BadFormatException("Bad OpCode for Special value: "+Utils.toHexString((byte)opCode));
				return special;
			}
			
			throw new BadFormatException("Invalide OpCode: " + opCode);
		}
	}
}
