package com.jiubang.ggheart.appgame.base.data;

import java.util.LinkedList;

/**
 * 数据栈
 * 
 * @author xiedezhi
 * 
 * @param <T>
 */
public class Stack<T> {
	private LinkedList<T> storage = new LinkedList<T>();

	/** 入栈 */
	public void push(T v) {
		storage.addFirst(v);
	}

	/**
	 * 出栈，但不删除
	 * 
	 * @return 返回栈顶的数据，没有就返回null
	 */
	public T peek() {
		try {
			return storage.getFirst();
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * 出栈
	 * 
	 * @return 返回栈顶的数据，没有就返回null
	 */
	public T pop() {
		try {
			return storage.removeFirst();
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}

	/** 栈是否为空 */
	public boolean empty() {
		return storage.isEmpty();
	}

	/** 打印栈元素 */
	@Override
	public String toString() {
		return storage.toString();
	}

	/**
	 * 获取栈的大小
	 */
	public int size() {
		return storage.size();
	}
}