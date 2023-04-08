package convex.benchmarks;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;

import convex.core.data.ACell;
import convex.core.data.AVector;
import convex.core.data.Ref;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMLong;
import etch.EtchStore;

/**
 * Benchmarks from the Etch database
 * 
 * We are interested in random writes since the use of hashes as database
 * keys results in a fairly random distribution of write keys.
 * 
 * Use of relatively small data size is reasonable since we expect majority of 
 * reads to be in cache (either recently written, or frequently access data such 
 * as commonly used libraries or actors).
 */
public class EtchBenchmark {
	
	static final EtchStore store=EtchStore.createTemp();
	
	static long nonce=0;
	
	static int NUMVALS=20000;
	
	@SuppressWarnings("unchecked")
	static Ref<ACell>[] refs=new Ref[NUMVALS];
	
	static final Random rand=new Random();
	
	static {
		for (int i=0; i<NUMVALS; i++) {
			AVector<CVMLong> v=Vectors.of(0L,(long)i);
			Ref<ACell> r=v.getRef();
			refs[i]=r;
			r.getHash();
			store.storeTopRef(r, Ref.STORED, null);
		}
	}
	
	@Benchmark
	public void writeData() {
		AVector<CVMLong> v=Vectors.of(1L,nonce++);
		store.storeTopRef(v.getRef(), Ref.STORED, null);
	}
	
	@Benchmark
	public void readDataRandom() {
		int ix=rand.nextInt(NUMVALS);
		store.refForHash(refs[ix].getHash()).getValue();
	}
	
	public static void main(String[] args) throws Exception {
		Options opt = Benchmarks.createOptions(EtchBenchmark.class);
		new Runner(opt).run();
	}
}
