package cmsc420_f22; // Don't change this line

import java.util.ArrayList;

public class ExpandingStack {
	private String[] stack;
	private int top;

	public ExpandingStack(int initialCapacity) throws Exception {
		if (initialCapacity < 1) {
			throw new Exception("Invalid capacity");
		}
		this.stack = new String[initialCapacity];
		this.top = -1;
	}
	
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
	
	public String pop() throws Exception {
		if (this.top < 0) {
			throw new Exception("Pop of empty stack");
		}
		top--;
		return this.stack[top + 1];
	}
	
	public String peek(int idx) throws Exception {
		if (idx < 0 || idx > this.top) {
			throw new Exception("Peek index out of range");
		}
		return this.stack[top - idx];
	}
	
	public int size() {
		return this.top + 1;
	}
	
	public int capacity() {
		return this.stack.length;
	}
	
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