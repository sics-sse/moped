/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;


/**
 * A Stripped down version of the standard J2SE java.util.LinkedList class.
 *
 * Can be useds as a FIFO queue by adding and removing from opposite ends.
 *
 * This class is NOT synchronized.
 */
public final class SimpleLinkedList {
    
    final Entry header;
    int size;
    
    /**
     * Constructs an empty list.
     */
    public SimpleLinkedList() {
        header = new Entry();
    }
    
    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list.
     * @throws    IllegalStateException if this list is empty.
     */
    public Object getFirst() {
        if (size==0)
            throw new IllegalStateException();
        
        return header.next.element;
    }
    
    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list.
     * @throws    IllegalStateException if this list is empty.
     */
    public Object getLast()  {
        if (size==0)
            throw new IllegalStateException();
        
        return header.previous.element;
    }
    
    /**
     * Removes and returns the first element from this list.
     *
     * @return the first element from this list.
     * @throws    IllegalStateException if this list is empty.
     */
    public Object removeFirst() {
        return remove(header.next);
    }
    
    /**
     * Removes and returns the last element from this list.
     *
     * @return the last element from this list.
     * @throws    IllegalStateException if this list is empty.
     */
    public Object removeLast() {
        return remove(header.previous);
    }
    
    
    /**
     * Inserts the given element at the beginning of this list.
     *
     * @param o the element to be inserted at the beginning of this list.
     */
    public void addFirst(Object o) {
        addBefore(o, header.next);
    }
    
    /**
     * Appends the given element to the end of this list.  (Identical in
     * function to the <tt>add</tt> method; included only for consistency.)
     *
     * @param o the element to be inserted at the end of this list.
     */
    public void addLast(Object o) {
        addBefore(o, header.next);
    }
    
    /**
     * Returns the number of elements in this list.
     *
     * @return the number of elements in this list.
     */
    public final int size() {
        return size;
    }
    
    /**
     * Removes the first occurrence of the specified element in this list.  If
     * the list does not contain the element, it is unchanged.  More formally,
     * removes the element with the lowest index <tt>i</tt> such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt> (if such an
     * element exists).
     *
     * @param o element to be removed from this list, if present.
     * @return <tt>true</tt> if the list contained the specified element.
     */
    public boolean remove(Object o) {
        if (o==null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element==null) {
                    remove(e);
                    return true;
                }
            }
        } else {
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element)) {
                    remove(e);
                    return true;
                }
            }
        }
        return false;
    }
    
    
    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this list contains
     * at least one element <tt>e</tt> such that <tt>(o==null ? e==null
     * : o.equals(e))</tt>.
     *
     * @param o element whose presence in this list is to be tested.
     * @return <tt>true</tt> if this list contains the specified element.
     */
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }
    
    /**
     * Returns the index in this list of the first occurrence of the
     * specified element, or -1 if the List does not contain this
     * element.  More formally, returns the lowest index i such that
     * <tt>(o==null ? get(i)==null : o.equals(get(i)))</tt>, or -1 if
     * there is no such index.
     *
     * @param o element to search for.
     * @return the index in this list of the first occurrence of the
     * 	       specified element, or -1 if the list does not contain this
     * 	       element.
     */
    public int indexOf(Object o) {
        int index = 0;
        if (o==null) {
            for (Entry e = header.next; e != header; e = e.next) {
                if (e.element==null)
                    return index;
                index++;
            }
        } else {
            for (Entry e = header.next; e != header; e = e.next) {
                if (o.equals(e.element))
                    return index;
                index++;
            }
        }
        return -1;
    }
    
    /**
     * Removes all of the elements from this list.
     */
    public void clear() {
        Entry e = header.next;
        while (e != header) {
            Entry next = e.next;
            e.next = e.previous = null;
            e.element = null;
            e = next;
        }
        header.next = header.previous = header;
        size = 0;
    }
    
    /**
     * Returns an enumeration of the components of this vector.
     *
     * @return  an enumeration of the components of this vector.
     * @see     java.util.Enumeration
     */
    public Enumeration elements() {
        return new SimpleLinkedListEnumerator(this);
    }
    
    private final static class Entry {
        Object element;
        Entry next;
        Entry previous;
        
        Entry(Object element, Entry next, Entry previous) {
            this.element = element;
            this.next = next;
            this.previous = previous;
        }
        
        Entry() {
            this.next = this;
            this.previous = this;
        }
    }
    
    private void addBefore(Object element, Entry e) {
        Entry newEntry = new Entry(element, e, e.previous);
        newEntry.previous.next = newEntry;
        newEntry.next.previous = newEntry;
        size++;
    }
    
    private Object remove(Entry e) {
        if (e.next == e) {
            throw new NoSuchElementException();
        }
        Object result = e.element;
        e.previous.next = e.next;
        e.next.previous = e.previous;
        /*
          - e should become unreachable. let GC clean up:
        e.next = null;
        e.previous = null;
        e.element = null;
         */
        size--;
        return result;
    }
    
    private final static class SimpleLinkedListEnumerator implements Enumeration {
        SimpleLinkedList list;
        Entry pointer;
        
        SimpleLinkedListEnumerator(SimpleLinkedList l) {
            list = l;
            pointer = list.header.next;
        }
        
        public boolean hasMoreElements() {
            return pointer != list.header;
        }
        
        public Object nextElement() {
            if (pointer == list.header) {
                throw new NoSuchElementException();
            }
            
            Object element = pointer.element;
            pointer = pointer.next;
            return element;
        }
    }
    
}
