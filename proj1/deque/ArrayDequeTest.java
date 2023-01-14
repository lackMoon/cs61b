package deque;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Performs some basic linked list tests. */
public class ArrayDequeTest {


	/** Adds a few things to the list, checking isEmpty() and size() are correct,
	 * finally printing the results.
	 *
	 * && is the "and" operation. */
	@Test
	public void addIsEmptySizeTest() {
		System.out.println("Running add/isEmpty/Size test.");

		ArrayDeque<String> ad1 = new ArrayDeque<String>();

		assertEquals(true, ad1.isEmpty());

		ad1.addFirst("Deque1");
		
		// The && operator is the same as "and" in Python.
		// It's a binary operator that returns true if both arguments true, and false otherwise.
		assertEquals(1, ad1.size());
		assertEquals(false, ad1.isEmpty());

		ad1.addFirst("Deque8");
		assertEquals(2, ad1.size());

		ad1.addFirst("Deque7");
		assertEquals(3, ad1.size());

		ad1.addLast("Deque2");
		assertEquals(4, ad1.size());

		ad1.addLast("Deque3");

		ad1.addLast("Deque4");

		ad1.addLast("Deque5");

		ad1.addLast("Deque6");

		ad1.addFirst("Deque31");

		ad1.addLast("Deque9");

		ad1.printDeque();

	}

	/** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
	@Test
	public void addRemoveTest() {

		System.out.println("Running add/remove test.");

		ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
		// should be empty
		assertEquals(true, ad1.isEmpty());

		ad1.addFirst(3);

		ad1.addLast(4);

		ad1.addFirst(2);

		ad1.addLast(5);

		ad1.addLast(6);

		ad1.addFirst(1);

		ad1.printDeque();

		ad1.removeFirst();

		ad1.removeFirst();

		ad1.removeLast();

		ad1.removeLast();


		ad1.printDeque();

		ad1.addFirst(2);

		ad1.removeLast();

		ad1.printDeque();

		ad1.removeLast();

		ad1.removeLast();
		// should be empty
		assertEquals(true, ad1.isEmpty());

	}

	@Test
	public void FillEmptyTest() {

		System.out.println("Running add/remove test.");

		ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
		// should be empty
		assertEquals(true, ad1.isEmpty());

		ad1.addFirst(3);

		ad1.addLast(4);

		ad1.addFirst(2);

		ad1.addLast(5);

		ad1.addLast(6);

		ad1.addFirst(1);

		ad1.addFirst(7);

		ad1.addFirst(8);

		ad1.printDeque();

		ad1.removeFirst();

		ad1.removeFirst();

		ad1.removeLast();

		ad1.removeFirst();

		ad1.removeLast();

		ad1.removeFirst();

		ad1.removeFirst();

		ad1.removeLast();
		assertEquals(true, ad1.isEmpty());
		ad1.addFirst(2);

		ad1.addLast(5);

		ad1.addLast(6);

		ad1.addFirst(3);

		ad1.printDeque();

		assertEquals(4, ad1.size());

	}

} 