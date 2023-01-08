import java.util.ArrayList;

/* Simple stack implemented using an array in Java. */

public class ExpandingStack {
	private String[] stack;
	private int top;

	/* Construct new stack by creating an empty array of given size, and setting the "top" variable to -1. "top" is
	used to determine whether we can continue pushing elements onto the stack, or if the stack is full and we need to
	resize. */
	public ExpandingStack(int initialCapacity) throws Exception {
		if (initialCapacity < 1) {
			throw new Exception("Invalid capacity");
		}
		this.stack = new String[initialCapacity];
		this.top = -1;
	}
	
	/* Push a new element onto the top of the stack. If the stack overflows, we resize by allocating a new array with
	twice as much space as our current one and copying existing elements over. Amortized analysis shows this runs in
	O(n). */
	public void push(String x) {
		if (this.top == this.stack.length - 1) {
			int newSize = this.stack.length * 2;
			String[] newStack = new String[newSize];
			for (int i = 0; i < this.stack.length; i++) {
				newStack[i] = this.stack[i];
			}
			this.stack = newStack;
		}
		this.top++;
		this.stack[this.top] = x;
	}
	
	/* Pop the topmost element off the stack. */
	public String pop() throws Exception {
		if (this.top < 0) {
			throw new Exception("Pop of empty stack");
		}
		top--;
		return this.stack[top + 1];
	}
	
	/* Examine the element at a given index in the stack. */
	public String peek(int idx) throws Exception {
		if (idx < 0 || idx > this.top) {
			throw new Exception("Peek index out of range");
		}
		return this.stack[top - idx];
	}
	
	/* Return the size of the stack. */
	public int size() {
		return this.top + 1;
	}
	
	/* Return how many elements the stack can store. */
	public int capacity() {
		return this.stack.length;
	}
	
	/* Return a list containing all the elements inside the stack. */
	public ArrayList<String> list() {
		ArrayList<String> res = new ArrayList<String>();
		int idx = this.top;
		while (idx > -1) {
			res.add(this.stack[idx]);
			idx--;
		}
		return res;
	}
}