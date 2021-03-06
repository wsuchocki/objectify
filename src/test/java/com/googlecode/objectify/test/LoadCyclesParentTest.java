package com.googlecode.objectify.test;

import org.testng.annotations.Test;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.test.util.TestBase;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static com.googlecode.objectify.test.util.TestObjectifyService.fact;

/**
 * This test was contributed: https://code.google.com/p/objectify-appengine/issues/detail?id=144
 */
public class LoadCyclesParentTest extends TestBase {

	@Entity
	public static class A {
		@Id
		public long id = 1;

		@Load
		public Ref<B> b;
	}

	@Entity
	public static class B {
		@Id
		public long id = 1;

		@Parent @Load
		public Ref<A> a;
	}

	@Test
	public void loadCycles() {
		fact().register(A.class);
		fact().register(B.class);

		A a = new A();
		B b = new B();
		b.a = Ref.create(a);	// gotta place b.a first so it assigns parent before we create a.b ref
		a.b = Ref.create(b);

		ofy().save().entities(a, b).now();

		ofy().clear();
		A a1 = ofy().load().entity(a).now();
		assert a1.b.get().id == b.id;
	}
}
