package convex.core;

import java.util.concurrent.TimeoutException;

import convex.core.data.ACell;
import convex.core.data.AMap;
import convex.core.data.ARecordGeneric;
import convex.core.data.AString;
import convex.core.data.AVector;
import convex.core.data.Address;
import convex.core.data.Blob;
import convex.core.data.Format;
import convex.core.data.Keyword;
import convex.core.data.Keywords;
import convex.core.data.Maps;
import convex.core.data.Strings;
import convex.core.data.Tag;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMLong;
import convex.core.exceptions.BadFormatException;
import convex.core.exceptions.InvalidDataException;
import convex.core.lang.Context;
import convex.core.lang.impl.AExceptional;
import convex.core.lang.impl.ErrorValue;
import convex.core.lang.impl.RecordFormat;

/**
 * Class representing the result of a Query or Transaction.
 * 
 * A Result is typically used to communicate the outcome of a Query or a Transaction from a Peer to a Client.
 * 
 * Contains:
 * <ol>
 * <li>Message ID</li>
 * <li>Result value</li>
 * <li>Error Code</li>
 * <li>Additional info</li>
 * </ol>
 * 
 * 
 */
public final class Result extends ARecordGeneric {

	private static final RecordFormat RESULT_FORMAT=RecordFormat.of(Keywords.ID,Keywords.RESULT,Keywords.ERROR,Keywords.INFO);
	
	private Result(AVector<ACell> values) {
		super(RESULT_FORMAT, values);
	}
	
	private static Result buildFromVector(AVector<ACell> values) {
		return new Result(values);
	}
	
	/**
	 * Create a Result
	 * @param id ID of Result message
	 * @param value Result Value
	 * @param errorCode Error Code (may be null for success)
	 * @param info Additional info
	 * @return Result instance
	 */
	public static Result create(CVMLong id, ACell value, ACell errorCode, AMap<Keyword,ACell> info) {
		return buildFromVector(Vectors.of(id,value,errorCode,info));
	}
	
	/**
	 * Create a Result
	 * @param id ID of Result message
	 * @param value Result Value
	 * @param errorCode Error Code (may be null for success)
	 * @return Result instance
	 */
	public static Result create(CVMLong id, ACell value, ACell errorCode) {
		return create(id,value,errorCode,null);
	}

	/**
	 * Create a Result
	 * @param id ID of Result message
	 * @param value Result Value
	 * @return Result instance
	 */
	public static Result create(CVMLong id, ACell value) {
		return create(id,value,null,null);
	}

	/**
	 * Returns the message ID for this result. Message ID is an arbitrary ID assigned by a client requesting a transaction.
	 * 
	 * @return ID from this result
	 */
	public ACell getID() {
		return values.get(0);
	}
	
	/**
	 * Returns the value for this result. The value is the result of transaction execution (may be an error message if the transaction failed)
	 * 
	 * @param <T> Type of Value
	 * @return ID from this result
	 */
	@SuppressWarnings("unchecked")
	public <T extends ACell> T getValue() {
		return (T)values.get(1);
	}
	
	/**
	 * Returns the stack trace for this result. May be null
	 * 
	 * @return Trace vector from this result
	 */
	@SuppressWarnings("unchecked")
	public AVector<AString> getTrace() {
		AMap<Keyword,ACell> info=getInfo();
		if (info instanceof AMap) {
			AMap<Keyword,ACell> m=(AMap<Keyword, ACell>) info;
			return (AVector<AString>) m.get(Keywords.TRACE);
		}
		return null;
	}
	
	/**
	 * Returns the info for this Result. May be null
	 * 
	 * @return Info map from this result
	 */
	@SuppressWarnings("unchecked")
	public AMap<Keyword,ACell> getInfo() {
		return (AMap<Keyword, ACell>) values.get(3);
	}
	
	/**
	 * Returns the Error Code from this Result. Normally this should be a Keyword.
	 * 
	 * Will be null if no error occurred.
	 * 
	 * @return ID from this result
	 */
	public ACell getErrorCode() {
		return values.get(2);
	}
	
	@Override
	public AVector<ACell> values() {
		return values;
	}

	@Override
	protected Result withValues(AVector<ACell> newValues) {
		if (values==newValues) return this;
		return new Result(newValues);
	}
	
	@Override
	public void validate() throws InvalidDataException {
		super.validate();
		
		ACell id=values.get(0);
		if ((id!=null)&&!(id instanceof CVMLong)) {
			throw new InvalidDataException("Result ID must be a CVM long value",this);
		}
	}
	
	@Override
	public int encode(byte[] bs, int pos) {
		bs[pos++]=Tag.RESULT;
		pos=values.encodeRaw(bs,pos);
		return pos;
	}
	
	/**
	 * Reads a Result from a Blob encoding. Assumes tag byte already checked.
	 * 
	 * @param b Blob to read from
	 * @param pos Start position in Blob (location of tag byte)
	 * @return New decoded instance
	 * @throws BadFormatException In the event of any encoding error
	 */
	public static Result read(Blob b, int pos) throws BadFormatException {
		int epos=pos; 
		// include tag location since we are reading raw Vector (will ignore tag)
		AVector<ACell> v=Vectors.read(b,epos);
		epos+=Format.getEncodingLength(v);
		
		Blob enc=v.getEncoding();
		v.attachEncoding(null); // This is an invalid encoding for vector, see above
		Result r=buildFromVector(v);
		r.attachEncoding(enc);
		return r;
	}


	/**
	 * Tests is the Result represents an Error
	 * @return True if error, false otherwise
	 */
	public boolean isError() {
		return getErrorCode()!=null;
	}

	/**
	 * Constructs a Result from a Context
	 * @param id Id for Result
	 * @param ctx Context
	 * @return New Result instance
	 */
	public static Result fromContext(CVMLong id,Context ctx) {
		Object result=ctx.getValue();
		ACell errorCode=null;
		AMap<Keyword,ACell> info=null;
		if (result instanceof AExceptional) {
			AExceptional ex=(AExceptional)result;
			result=ex.getMessage();
			errorCode=ex.getCode();
			if (ex instanceof ErrorValue) {
				ErrorValue ev=(ErrorValue) ex;
				AVector<?> trace=Vectors.create(ev.getTrace());
				Address address=ev.getAddress();
				info =Maps.of(Keywords.TRACE,trace,Keywords.ADDRESS,address);
			}
		}
		return create(id,(ACell)result,errorCode,info);
	}
	
	/**
	 * Constructs a Result from a Context
	 * @param ctx Context
	 * @return New Result instance
	 */
	public static Result fromContext(Context ctx) {
		return fromContext(null,ctx);
	}


	/**
	 * Updates result with a given message ID. Used to tag Results for return to Clients
	 * @param id New Result message ID
	 * @return Updated Result
	 */
	public Result withID(ACell id) {
		return withValues(values.assoc(0, id));
	}

	@Override
	public byte getTag() {
		return Tag.RESULT;
	}

	@Override
	public RecordFormat getFormat() {
		return RESULT_FORMAT;
	}

	public static Result fromException(Throwable e) {
		if (e instanceof TimeoutException) {
			String msg=e.getMessage();
			if (msg==null) msg=e.getClass().getName();
			return Result.create(null,ErrorCodes.TIMEOUT,Strings.create(msg));
		}
		return Result.create(null, ErrorCodes.EXCEPTION,Strings.create(e.getMessage()));
	}



}
