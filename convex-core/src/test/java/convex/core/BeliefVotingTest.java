package convex.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import convex.core.crypto.AKeyPair;
import convex.core.data.AccountKey;
import convex.core.data.Address;
import convex.core.data.Maps;
import convex.core.data.SignedData;
import convex.core.data.Vectors;
import convex.core.data.prim.CVMLong;
import convex.core.exceptions.BadSignatureException;
import convex.core.exceptions.InvalidDataException;
import convex.core.init.Init;
import convex.core.transactions.ATransaction;
import convex.core.transactions.Invoke;

public class BeliefVotingTest {

	AKeyPair[] kps=new AKeyPair[] {
			AKeyPair.createSeeded(1),
			AKeyPair.createSeeded(2),
			AKeyPair.createSeeded(3),
			AKeyPair.createSeeded(4),
			AKeyPair.createSeeded(5),
			AKeyPair.createSeeded(6)
	};
	
	
	AccountKey[] keys=Stream.of(kps).map(kp->kp.getAccountKey()).toArray(AccountKey[]::new);
	
	State s=Init.createState(List.of(keys));
	
	static final long TS=0;
	
	@Test
	public void testComputeVote() {
		assertEquals(100.0, BeliefMerge.computeVote(Maps.hashMapOf(1, 50.0, 0, 50.0)), 0.000001);
		assertEquals(0.0, BeliefMerge.computeVote(Maps.hashMapOf()), 0.000001);
	}
	
	@Test
	public void testEmptyMerge() throws BadSignatureException, InvalidDataException {
		Belief b=Belief.create(kps[0],Order.create());
		
		BeliefMerge mc=BeliefMerge.create(b, kps[0], TS+5, s);
		Belief b2=mc.merge(b);
		assertSame(b,b2);
	}

	
	@SuppressWarnings("unchecked")
	@Test public void testBasicMerges() throws BadSignatureException, InvalidDataException {
		SignedData<Block> A=bl(1);
		
		
		assertTrue(s.getPeers().get(keys[0]).getTotalStake()>0);
		
		Order o0=Order.create().withTimestamp(TS);
		Belief b0=Belief.create(kps[0], o0);

		// check trivial merges are idempotent
		BeliefMerge baseMC=BeliefMerge.create(b0, kps[0], TS, s);
		assertSame(b0,baseMC.mergeOrders(b0));
		Belief b00=baseMC.merge(b0);
		assertSame(b0,b00);
		assertSame(b0,baseMC.merge(b0,b0));
		
		long ATIME=A.getValue().getTimeStamp();
		Order p1o=Order.create(0, 0, A).withTimestamp(ATIME);
		Belief b1=Belief.create(kps[1], p1o);
		
		// Shouldn't change Belief, since incoming order is from future
		Belief b0present=baseMC.merge(b1);
		assertSame(b0,b0present);
		
		// Updated merge context should allow Belief merge with new Block
		BeliefMerge mc=BeliefMerge.create(b0, kps[0], TS+1, s);
		Belief b2=mc.merge(b1);
		Order o2=b2.getOrder(keys[0]);
		assertEquals(p1o.getBlocks(),o2.getBlocks());
		assertEquals(0,o2.getConsensusPoint(1));
		assertEquals(0,o2.getConsensusPoint(2));
		assertEquals(TS+1,o2.getTimestamp());
		
		// Beliefs from other Peers, enough for Proposal
		Belief br2=Belief.create(kps[2], p1o);
		Belief br3=Belief.create(kps[3], p1o);
		Belief br4=Belief.create(kps[4], p1o);
		Belief br5=Belief.create(kps[5], p1o);
		
		// Merge new Beliefs
		Belief b3=mc.merge(b2,br2,br3,br4,br5);
		Order o3=b3.getOrder(keys[0]);
		assertEquals(p1o.getBlocks(),o3.getBlocks());
		assertEquals(1,o3.getConsensusPoint(1));
		assertEquals(0,o3.getConsensusPoint(2));
		
		mc=BeliefMerge.create(b3, kps[0], TS+1, s);
		// Future merges should be idempotent
		assertSame(b3,mc.merge(br2,br3,br4,br5));
		BeliefMerge mc3=BeliefMerge.create(b3, kps[0], TS+10, s);
		assertSame(b3,mc3.merge(br2,br3,br4,br5));
	}
	
	@SuppressWarnings("unchecked")
	@Test public void testBlockVotes() throws BadSignatureException, InvalidDataException {
		SignedData<Block> A=bl(1);
		SignedData<Block> B=bl(2);
		SignedData<Block> C=bl(3);
		SignedData<Block> D=bl(4);
		SignedData<Block> E=bl(5);
		SignedData<Block> F=bl(6);
		SignedData<Block> G=bl(7);
		State s=Init.createState(List.of(keys));

		{
			SignedData<Order> o0=or(0, TS, 0,0,A);
			SignedData<Order> o1=or(1, TS, 0,0,A,B);
			SignedData<Order> o2=or(2, TS, 0,0,B);
			SignedData<Order> o3=or(3, TS, 0,0,B,A);
			SignedData<Order> o4=or(4, TS, 0,0,B,A,C,G); // should win
			SignedData<Order> o5=or(5, TS, 0,0,B,A,E,F,D);
			
			Belief b=Belief.create(o0,o1,o2,o3,o4,o5);
			BeliefMerge mc=BeliefMerge.create(b, kps[0], TS, s);
			Belief b2=mc.merge();
			SignedData<Order> so=b2.getOrders().get(keys[0]);
			Order order=so.getValue();
			assertEquals(7,order.getBlockCount());
			assertEquals(B,order.getBlock(0));
			assertEquals(G,order.getBlock(3));
			assertEquals(0,order.getProposalPoint()); // 66.66..% just short of proposal threshold
			assertEquals(0,order.getConsensusPoint());
			// Note D,E,F not in winning ORder so sorted by timestamp order
			assertEquals(Vectors.of(B,A,C,G,D,E,F),order.getBlocks());
		}
		
		{
			SignedData<Order> o0=or(0, TS, 0,0,A);
			SignedData<Order> o1=or(1, TS, 0,0,B,A);
			SignedData<Order> o2=or(2, TS, 0,0,B);
			SignedData<Order> o3=or(3, TS, 1,0,B,A);
			SignedData<Order> o4=or(4, TS, 1,0,B,A,C,D);
			SignedData<Order> o5=or(5, TS, 1,0,B,A,E,F,G);
			
			Belief b=Belief.create(o0,o1,o2,o3,o4,o5);
			BeliefMerge mc=BeliefMerge.create(b, kps[0], TS, s);
			Belief b2=mc.merge();
			SignedData<Order> so=b2.getOrders().get(keys[0]);
			Order order=so.getValue();
			assertEquals(7,order.getBlockCount());
			assertEquals(B,order.getBlock(0));
			assertEquals(1,order.getConsensusPoint(1)); // Enough for proposal
			assertEquals(0,order.getConsensusPoint(2));
			assertEquals(Vectors.of(B,A,C,D,E,F,G),order.getBlocks());
		}
		
		{
			SignedData<Order> o0=or(0, TS, 0,0,A);
			SignedData<Order> o1=or(1, TS, 1,0,B);
			SignedData<Order> o2=or(2, TS, 1,0,B);
			SignedData<Order> o3=or(3, TS, 1,0,B,A);
			SignedData<Order> o4=or(4, TS, 1,0,B,A);
			SignedData<Order> o5=or(5, TS, 1,0,B);
			
			Belief b=Belief.create(o0,o1,o2,o3,o4,o5);
			BeliefMerge mc=BeliefMerge.create(b, kps[0], TS, s);
			Belief b2=mc.merge();
			SignedData<Order> so=b2.getOrders().get(keys[0]);
			Order order=so.getValue();
			assertEquals(2,order.getBlockCount());
			assertEquals(B,order.getBlock(0));
			assertEquals(1,order.getConsensusPoint(1)); // Enough for proposal level 1
			assertEquals(1,order.getConsensusPoint(2)); // Enough for consensus level 2
			assertEquals(Vectors.of(B,A),order.getBlocks());
		}
		
		{
			// "Everybody wants to be my enemy"
			SignedData<Order> o0=or(0, TS, 1,0,A);
			SignedData<Order> o1=or(1, TS, 1,0,B);
			SignedData<Order> o2=or(2, TS, 1,0,B);
			SignedData<Order> o3=or(3, TS, 1,0,B);
			SignedData<Order> o4=or(4, TS, 1,0,B);
			SignedData<Order> o5=or(5, TS, 1,0,B);
			
			Belief b=Belief.create(o0,o1,o2,o3,o4,o5);
			BeliefMerge mc=BeliefMerge.create(b, kps[0], TS+1, s);
			Belief b2=mc.merge();
			SignedData<Order> so=b2.getOrders().get(keys[0]);
			assertEquals(o0,so); // Shouldn't have changed
			Order order=so.getValue();
			assertEquals(1,order.getBlockCount());
			assertEquals(A,order.getBlock(0)); // didn't switch
			assertEquals(1,order.getConsensusPoint(1)); // Kept proposal
			assertEquals(0,order.getConsensusPoint(2)); // No change in my consensus level 2

			// After enough time, Peer should be willing to switch proposal
			BeliefMerge mc3=BeliefMerge.create(b, kps[0], TS+1+Constants.KEEP_PROPOSAL_TIME, s);
			Belief b3=mc3.merge();
			SignedData<Order> so3=b3.getOrders().get(keys[0]);
			Order order3=so3.getValue();
			assertEquals(2,order3.getBlockCount());
			assertEquals(B,order3.getBlock(0)); // didn't switch
			assertEquals(A,order3.getBlock(1)); // Kept own block
			assertEquals(1,order3.getConsensusPoint(1)); // Updated proposal 
			assertEquals(1,order3.getConsensusPoint(2)); // New consensus
		}
	}

	@SuppressWarnings("unchecked")
	private SignedData<Order> or(int peer, long ts, int pp, int cp, SignedData<Block>... blks) {
		Order o=Order.create(pp, cp, blks).withTimestamp(TS);
		return kps[peer].signData(o);
	}

	private SignedData<Block> bl(int i) {
		Block b=Block.of(i,tr(i),tr(i+1000000));
		return kps[i%kps.length].signData(b);
	}

	/**
	 * Create a unique dummy transaction for each seed value
	 * @param i
	 * @return
	 */
	private SignedData<ATransaction> tr(int i) {
		ATransaction t=Invoke.create(Address.create(i), i, CVMLong.create(i));
		return kps[i%kps.length].signData(t);
	}
}
