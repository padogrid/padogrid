package org.hazelcast.addon.test.perf.junit;

import java.util.List;

import org.junit.Test;

public class ClassFinderTest {

	@Test
	public void testPutCustomer() {
		List<Class<?>> list = ClassFinder.find("org.hazelcast.demo.nw.data.avro");
		list.forEach(clazz->System.out.println(clazz));
	}

}
