package convex.core.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import convex.core.store.AStore;
import convex.core.store.MemoryStore;
import convex.core.store.Stores;
import etch.EtchStore;

@RunWith(Parameterized.class)
public class ParamTestRefs {
	private AStore store;

	public ParamTestRefs(String label,AStore store) {
		this.store = store;
	}

	@Parameterized.Parameters(name = "{index}: {0}")
	public static Collection<Object[]> dataExamples() {
		return Arrays
				.asList(new Object[][] { 
					    { "Memory Store", new MemoryStore() }, 
						{ "Temp Etch Store", EtchStore.createTemp() } });
	}
	
	@Test
	public void testStoreUsage() {
		AStore temp=Stores.current();

		try {
			Stores.setCurrent(store);
			
			{ // single embedded value
				Long n=1567565765677L;
				Ref<Long> r=Ref.create(n);
				assertEquals(Ref.EMBEDDED,r.getStatus());
				Ref<Long> r2=r.persist();
				assertEquals(Ref.EMBEDDED,r2.getStatus());
				assertSame(n,r2.getValue());
			}
			
			
			{ // structure with embedded value
				AVector<Long> v=Vectors.of(6759578996496L);
				Ref<AVector<Long>> r=Ref.create(v);
				assertEquals(Ref.UNKNOWN,r.getStatus());
				Ref<AVector<Long>> r2=r.persist();
				assertEquals(Ref.PERSISTED,r2.getStatus());
				assertEquals(v.getRef(0),r2.getValue().getRef(0));
			}
			
			{ // map with empedded structure
				AMap<Long,AVector<Long>> m=Maps.of(156746748L,Vectors.of(8797987L));
				Ref<AMap<Long,AVector<Long>>> r=Ref.create(m);
				assertEquals(Ref.UNKNOWN,r.getStatus());
				
				Ref<AMap<Long,AVector<Long>>> r2=r.persist();
				
				assertEquals(Ref.PERSISTED,r2.getStatus());
				MapEntry<Long, AVector<Long>> me2=r2.getValue().entryAt(0);
				assertEquals(Ref.EMBEDDED,me2.getRef(0).getStatus());
				assertEquals(Ref.PERSISTED,me2.getRef(1).getStatus());
			}		
			
		} finally {
			Stores.setCurrent(temp);
		}
	}


}
