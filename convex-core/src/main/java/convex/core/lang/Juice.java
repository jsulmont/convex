package convex.core.lang;

import convex.core.Constants;
import convex.core.data.ACell;
import convex.core.data.ACountable;
import convex.core.data.ADataStructure;

/**
 * Static class defining juice costs for executable operations.
 * 
 * "LISP programmers know the value of everything and the cost of nothing." -
 * Alan Perlis
 * 
 */
public class Juice {
	/**
	 * Juice required to resolve a constant value
	 * 
	 * Very cheap, no allocs / lookup.
	 */
	public static final long CONSTANT = 5;
	
	/**
	 * Juice required to define a value in the current environment.
	 * 
	 * We make this somewhat expensive - we want to discourage over-use as a general rule
	 * since it writes to global chain state. However memory accounting helps discourage 
	 * superfluous defs, so it only needs to reflect execution cost.
	 */
	public static final long DEF = 100;

	/**
	 * Juice required to look up a value in the local environment.
	 */
	public static final long LOOKUP = 15;
	
	/**
	 * Juice required to look up a core symbol.
	 */
	public static final long CORE = Constants.OPT_STATIC? Juice.CONSTANT:(Juice.LOOKUP_DYNAMIC+CONSTANT);
	
	/**
	 * Juice required to look up a value in the dynamic environment.
	 * 
	 * Potentially a bit pricey since read only, but might hit storage so.....
	 */
	public static final long LOOKUP_DYNAMIC = 40;
	
	/**
	 * Juice required to look up a symbol with a regular Address
	 */
	public static final long LOOKUP_SYM = LOOKUP_DYNAMIC+CONSTANT;

	/**
	 * Juice required to execute a Do block
	 * 
	 * Very cheap, no allocs.
	 */
	public static final long DO = 5;


	/**
	 * Juice required to execute a Let block
	 * 
	 * Fairly cheap but some parameter munging required. TODO: revisit binding
	 * costs?
	 */
	public static final long LET = 30;



	/**
	 * Juice required to execute a Cond expression
	 * 
	 * Pretty cheap, nothing nasty here (though conditions / results themselves
	 * might get pricey).
	 */
	public static final long COND_OP = 20;

	/**
	 * Juice required to create a lambda
	 * 
	 * Sort of expensive - might allocate a bunch of stuff for the closure?
	 */
	public static final long LAMBDA = 100;

	/**
	 * Juice required to call an Actor
	 * 
	 * Slightly expensive for context switching?
	 */
	public static final long CALL_OP = 100;

	/**
	 * Juice required to build a data structure. Make a bit expensive?
	 */
	protected static final long BUILD_DATA = 50;

	/**
	 * Juice required per element changed when building a data structure. Map entries
	 * count as two elements.
	 * 
	 * We need to be a bit harsh on this! Risk of consuming too much heap space,
	 * might also result in multiple allocs for tree structures.
	 */
	protected static final long BUILD_PER_ELEMENT = 50;

	protected static final long MAP = 100;
	protected static final long REDUCE = 100;

	/**
	 * Juice for general object equality comparison
	 * 
	 * Pretty cheap.
	 */
	public static final long EQUALS = 5;

	/**
	 * Juice for numeric comparison
	 * 
	 * Pretty cheap. Bit of casting perhaps.
	 */
	public static final long NUMERIC_COMPARE = 10;

	/**
	 * Juice for an apply operation
	 * 
	 * Bit of cost to allow for parameter construction. Might need to revisit for
	 * bigger sequences?
	 */
	public static final long APPLY = 50;

	/**
	 * Juice for a cryptographic hash
	 * 
	 * Expensive.
	 */
	public static final long HASH = 10000;

	/**
	 * Juice for a very cheap operation. O(1), no new cell allocations or non-trivial lookups.
	 */
	public static final long CHEAP_OP = 10;
	
	/**
	 * Juice for a simple built-in core function. Simple operations are assumed to
	 * require no expensive resource access, and operate with O(1) allocations
	 */
	public static final long SIMPLE_FN = 20;

	/**
	 * Juice for constructing a String
	 * 
	 * Fairly cheap, since mostly in fast code, but charge extra for additional
	 * chars.
	 */
	protected static final long STR = SIMPLE_FN;

	protected static final long ARITHMETIC = SIMPLE_FN;

	protected static final long ADDRESS = 20;

	/**
	 * Juice for balance core function. Some lookups required
	 */
	protected static final long BALANCE = 50;

	/**
	 * Juice for creation of a blob. Fairly cheap but needs per-byte cost
	 */
	protected static final long BLOB = 20;
	protected static final long BUILD_PER_BYTE = 1;

	/**
	 * Juice for data structure get. Hash lookup possibly required.
	 */
	protected static final long GET = 30;

	protected static final long KEYWORD = 20;

	protected static final long SYMBOL = 20;

	/**
	 * Juice for a transfer execution. Some account updates
	 */
	public static final long TRANSFER = 200;
	
	/**
	 * Base juice for any signed transaction
	 */
	public static final long TRANSACTION_BASE = 1000;
	
	/**
	 * Juice per byte for any signed transaction
	 */
	public static final long TRANSACTION_PER_BYTE = 5;



	public static final long SIMPLE_MACRO = 200;

	/**
	 * Juice for a recur form
	 * 
	 * Fairly cheap, might have to construct some temp structures for recur
	 * arguments.
	 */
	public static final long RECUR = 30;

	/**
	 * Juice for a contract deployment
	 * 
	 * Make this quite expensive, mainly to deter lots of willy-nilly deploying
	 */
	public static final long DEPLOY_CONTRACT = 1000;

	/**
	 * Probably should be expensive?
	 */
	protected static final long EVAL = 500;

	// Juice amounts for compiler. TODO: figure out if compile / eval should be
	// allowed on-chain

	/**
	 * Juice cost to compile a Constant value
	 */
	public static final long COMPILE_CONSTANT = 30;

	/**
	 * Juice cost to compile a Constant value
	 */
	public static final long COMPILE_LOOKUP = 50;

	/**
	 * Juice cost to compile a general AST node
	 */
	public static final long COMPILE_NODE = 200;

	/**
	 * Juice cost to expand a constant
	 */
	public static final long EXPAND_CONSTANT = 40;

	/**
	 * Juice cost to expand a sequence
	 */
	public static final long EXPAND_SEQUENCE = 100;

	/**
	 * Juice cost to schedule
	 */
	public static final long SCHEDULE = 800;

	/**
	 * Default future schedule juice (10 per hour)
	 * 
	 * This makes scheduling a few hours / days ahead cheap but year is quite
	 * expensive (~87,600). Also places an upper bound on advance schedules.
	 * 
	 * TODO: review this. Maybe not needed given memory accounting?
	 */
	public static final long SCHEDULE_MILLIS_PER_JUICE_UNIT = 360000;

	
	/**
	 * Juice required to execute an exceptional return (return, halt, rollback etc.)
	 * 
	 * Pretty cheap, one alloc and a bit of exceptional value handling.
	 */
	public static final long RETURN = 20;

	/**
	 * Juice cost for accepting an offer of Convex coins.
	 * 
	 * Define this to be equal to a transfer.
	 */
	public static final long ACCEPT = TRANSFER;

	/**
	 * Juice cost for constructing a Syntax Object. Fairly lightweight.
	 */
	public static final long SYNTAX = Juice.SIMPLE_FN;

	/**
	 * Juice cost for extracting metadata from a Syntax object.
	 */
	public static final long META = Juice.CHEAP_OP;

	/**
	 * Juice cost for an 'assoc'
	 */
	public static final long ASSOC = Juice.BUILD_DATA+Juice.BUILD_PER_ELEMENT*2;

	/**
	 * Variable Juice cost for set comparison
	 */
	public static final long SET_COMPARE_PER_ELEMENT = 10;

	/**
	 * Juice to create an account. Some cost for new account data structure entry.
	 */
	public static final long CREATE_ACCOUNT = 100;

	public static final long QUERY = Juice.CHEAP_OP;

	public static final long LOG = 100;

	public static final long SPECIAL = Juice.CHEAP_OP;

	public static final long SET_BANG = 20;

	/**
	 * Make this quite expensive. Discourage spamming Peer updates
	 */
	public static final long PEER_UPDATE = 1000;

	/**
	 * Saturating multiply and add: result = a + (b * c)
	 * 
	 * Returns Long.MAX_VALUE on overflow.
	 * 
	 * @param a First number (to be added)
	 * @param b Second number (to be multiplied)
	 * @param c Thirst number (to be multiplied)
	 * @return long result, capped at Long.MAX_VALUE
	 */
	public static final long addMul(long a, long b, long c) {
		return add(a,mul(b,c));
	}
	
	/**
	 * Saturating multiply. Returns Long.MAX_VALUE on overflow.
	 * @param a First number
	 * @param b Second number
	 * @return long result, capped at Long.MAX_VALUE
	 */
	public static final long mul(long a, long b) {
		if ((a<0)||(b<0)) return Long.MAX_VALUE;
		if (Math.multiplyHigh(a, b)>0) return Long.MAX_VALUE;
		return a*b;
	}
	
	/**
	 * Saturating addition. Returns Long.MAX_VALUE on overflow.
	 * @param a First number
	 * @param b Second number
	 * @return long result, capped at Long.MAX_VALUE
	 */
	public static final long add(long a, long b) {
		if ((a<0)||(b<0)) return Long.MAX_VALUE;
		if ((a+b)<0) return Long.MAX_VALUE;
		return a+b;
	}

	/**
	 * Computes the data build cost of a countable structure of given length 
	 * @param counted Counted data structure, used for type
	 * @param n Element count of data structure constructed
	 * @return Calculated juice cost
	 */
	public static long buildCost(ACountable<ACell> counted, long n) {
		long elementCost=elementCost(counted);
		return addMul(Juice.BUILD_DATA,elementCost,n);
	}

	/**
	 * Gets the Juice cost for an additional element of the given countable value
	 * @param counted
	 * @return
	 */
	private static long elementCost(ACountable<ACell> counted) {
		if (counted instanceof ADataStructure) return BUILD_PER_ELEMENT;
		if (counted==null) return BUILD_PER_ELEMENT;
		return BUILD_PER_BYTE;
	}

	/**
	 * Gets the Juice cost for building a String
	 * @param n Length of String
	 * @return Juice cost
	 */
	public static long buildStringCost(long n) {
		return addMul(Juice.STR,BUILD_PER_BYTE,n);
	}
	
	/**
	 * Gets the Juice cost for building a Blob
	 * @param n Length of Blob
	 * @return Juice cost
	 */
	public static long buildBlobCost(long n) {
		return addMul(Juice.BLOB,BUILD_PER_BYTE,n);
	}

	/**
	 * Gets the maximum number of string bytes that can be constructed
	 * @param context Context to check for Juice
	 * @return Limit in number of bytes
	 */
	public static long limitString(Context<?> context) {
		long juice=context.getJuice();
		long limit=juice/BUILD_PER_BYTE;
		return limit;
	}

	/**
	 * Gets the Juice cost for building a data structure
	 * @param n Element count of data structure (number of CVM values)
	 * @return Juice cost
	 */
	 public static long buildDataCost(long n) {
		return addMul(Juice.BUILD_DATA,BUILD_PER_ELEMENT,n);
	}



}
