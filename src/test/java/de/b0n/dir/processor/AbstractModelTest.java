//package de.b0n.dir.processor;
//
//import static org.junit.Assert.*;
//
//import org.junit.Ignore;
//import org.junit.Test;
//
//public class ClusterTest {
//	private static final String EMPTY_STRING_ELEMENT = "";
//
//	@Test
//	public void constructorTest() {
//		assertNotNull(new AbstractProcessorModel<Integer, String>());
//	}
//
//	@Test(expected=IllegalArgumentException.class)
//	public void addGroupedElementNullNullTest() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		cluster.addGroupedElement(null, null);
//	}
//
//	@Test(expected=IllegalArgumentException.class)
//	public void addGroupedElementIntegerNullTest() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		cluster.addGroupedElement(Integer.valueOf(0), null);
//	}
//
//	@Test(expected=IllegalArgumentException.class)
//	public void addGroupedElementNullStringTest() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		cluster.addGroupedElement(null, EMPTY_STRING_ELEMENT);
//	}
//
//	@Test
//	public void size0Test() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		assertEquals(0, cluster.size());
//	}
//
//	@Test
//	public void size1Test() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		cluster.addGroupedElement(Integer.valueOf(0), EMPTY_STRING_ELEMENT);
//		assertEquals(1, cluster.size());
//		assertEquals(EMPTY_STRING_ELEMENT, cluster.values().iterator().next().peek());
//		assertEquals(EMPTY_STRING_ELEMENT, cluster.getGroup(Integer.valueOf(0)).peek());
//		assertEquals(EMPTY_STRING_ELEMENT, cluster.removeGroup(Integer.valueOf(0)).peek());
//		assertEquals(0, cluster.size());
//	}
//
//	@Test
//	public void uniqueTest() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		cluster.addGroupedElement(Integer.valueOf(0), EMPTY_STRING_ELEMENT);
//		cluster.addGroupedElement(Integer.valueOf(0), EMPTY_STRING_ELEMENT);
//		cluster.addGroupedElement(Integer.valueOf(1), EMPTY_STRING_ELEMENT);
//		assertEquals(3, cluster.size());
//		assertEquals(EMPTY_STRING_ELEMENT, cluster.removeUniques().peek());
//		assertEquals(2, cluster.size());
//		assertTrue(cluster.containsGroup(Integer.valueOf(0)));
//		assertFalse(cluster.containsGroup(Integer.valueOf(1)));
//		assertEquals(2, cluster.popGroup().size());
//		assertFalse(cluster.containsGroup(Integer.valueOf(0)));
//		assertFalse(cluster.containsGroup(Integer.valueOf(1)));
//		assertNull(cluster.popGroup());
//		assertEquals(0, cluster.size());
//	}
//
//	@Test
//	@Ignore("Extrem langsam")
//	public void sizeMaxTest() {
//		AbstractProcessorModel<Integer, String> cluster = new AbstractProcessorModel<Integer, String>();
//		for (long l = 0; l < Integer.MAX_VALUE; ++l) {
//			if (cluster.size()%1000 == 0)
//				System.out.println(l);
//			assertEquals(l, cluster.size());
//			cluster.addGroupedElement(Integer.valueOf((int) (l%100000)), EMPTY_STRING_ELEMENT);
//			assertEquals(l+1, cluster.size());
//		}
//		assertEquals(Integer.MAX_VALUE, cluster.size());
//
//		cluster.addGroupedElement(Integer.valueOf(1), EMPTY_STRING_ELEMENT);
//		assertEquals(Integer.MAX_VALUE, cluster.size());
//
//		cluster.addGroupedElement(Integer.valueOf(1), EMPTY_STRING_ELEMENT);
//		assertEquals(Integer.MAX_VALUE, cluster.size());
//	}
//}
