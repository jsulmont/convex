package convex.actors;
 
import static convex.core.lang.TestState.step;
import static convex.test.Assertions.assertNotError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import convex.core.data.Address;
import convex.core.lang.Context;
import convex.core.lang.Reader;
import convex.core.lang.TestState;
import convex.core.util.Utils;

public class TorusTest {
	static Address USD = null;
	static Address GBP = null;
	static Address TORUS = null;
	static Address USD_MARKET = null;
	static Context<Address> CONTEXT = null;

	static {
		Context<?> INITIAL=TestState.INITIAL_CONTEXT.fork();
		try {
			Context<?> ctx=INITIAL;
			ctx=step(ctx,"(import convex.fungible :as fun)");
			
			// Deploy currencies for testing
			ctx=step(ctx,"(def USD (deploy (fun/build-token {:supply 1000000000})))");
			assertNotError(ctx);
			USD=(Address) ctx.getResult();
			//System.out.println("USD deployed Address = "+USD);
			ctx=step(ctx,"(def GBP (deploy (fun/build-token {:supply 1000000000})))");
			GBP=(Address) ctx.getResult();
			
			// Deploy Torus actor itself
			ctx= ctx.deployActor(Reader.readResource("actors/torus.con"));
			assertNotError(ctx);
			TORUS=(Address)ctx.getResult();
			assertNotNull(ctx.getAccountStatus(TORUS));
			ctx= step(ctx,"(def TORUS "+TORUS+")");
			//System.out.println("Torus deployed Address = "+TORUS);
			
			// Deploy USD market. No market for GBP yet!
			ctx= step(ctx,"(call TORUS (create-market USD))");
			USD_MARKET=(Address)ctx.getResult();
			CONTEXT= ctx.withResult(TORUS);
		} catch (Throwable e) {
			e.printStackTrace();
			throw Utils.sneakyThrow(e);
		}
	}
	
	@Test public void testInitialMarket() {
		Context<?> ctx=CONTEXT.fork();
		
		ctx= step(ctx,"(call TORUS (get-market USD))");
		assertEquals(USD_MARKET,ctx.getResult());
	}

	@Test public void testSetup() {
		assertNotNull(TORUS);
		assertNotNull(USD);
		assertNotNull(GBP);
		assertNotNull(USD_MARKET);
	}
	
}
