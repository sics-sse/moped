package sics;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

public class ArrayList<T> extends Vector<T> {
//	Vector<T> list = null;

//	public ArrayList() {
//		super();
////		list = new Vector<T>();
//	}

	public void add(T obj) {
//		list.addElement(obj);
		addElement(obj);
	}

	public T get(int index) {
		 Enumeration<T> elements = elements();
		 for (int i = 0; i < index; i++) {
		 elements.nextElement();
		 }
		 
		 return elements.nextElement();
//		int current = 0;
//		Iterator<T> iterator = iterator();
//		while (iterator.hasNext()) {
//			if(index == current) {
//				return iterator.next();
//			} else {
//				iterator.next();
//			}
//			current++;
//		}
		
//		return null;
	}
}
