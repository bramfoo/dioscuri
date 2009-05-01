/*
    JPC: A x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.0

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007 Isis Innovation Limited

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 
    Details (including contact information) can be found at: 

    www.physics.ox.ac.uk/jpc
*/
package nl.kbna.dioscuri.module.cpu32;

//package org.jpc.emulator.memory.codeblock;

import java.util.*;

public class PriorityDeque extends AbstractQueue  implements nl.kbna.dioscuri.util.Deque
{
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    
    private transient Object[] queue;
    
    private int size = 0;

    private transient int modCount = 0;

    public PriorityDeque()
    {
    this(DEFAULT_INITIAL_CAPACITY);
    }
    
    public PriorityDeque(int initialCapacity)
    {
    if (initialCapacity < 1)
        throw new IllegalArgumentException();
    this.queue = new Object[initialCapacity];
    }
    
    public Iterator descendingIterator()
    {
    throw new UnsupportedOperationException();
    }

    public Iterator iterator()
    {    
    throw new UnsupportedOperationException();
    }

    public boolean removeLastOccurrence(Object o)
    {
    throw new UnsupportedOperationException();
    }

    public boolean removeFirstOccurrence(Object o)
    {
    throw new UnsupportedOperationException();
    }

    public int size()
    {
    return size;
    }

    public boolean contains(Object o)
    {
    return indexOf(o) != -1;
    }

    public boolean remove(Object o)
    {
    int i = indexOf(o);
    if (i == -1)
        return false;
    else {
        removeAt(i);
        return true;
    }
    }

    public Object pop()
    {
    return removeFirst();
    }

    public void push(Object o)
    {
    add(o);
    }

    public Object peek()
    {
    return peekFirst();
    }

    public Object poll()
    {
    return pollFirst();
    }

    public boolean offerLast(Object o)
    {
    return offer(o);
    }

    public boolean offerFirst(Object o)
    {
    return offer(o);
    }

    public void addLast(Object o)
    {
    add(o);
    }

    public void addFirst(Object o)
    {
    add(o);
    }

    public Object getFirst()
    {
    return element();
    }

    public Object removeFirst()
    {
    return remove();
    }

    public boolean offer(Object o)
    {
    if (o == null)
        throw new NullPointerException();
    modCount++;
    int i = size;
    if (i >= queue.length)
        grow(i + 1);
    size = i + 1;
    if (i == 0)
        queue[0] = o;
    else {
        int idx = siftUp(i, o);
        siftDown(idx, o);
    }
    return true;
    }

    public Object pollFirst() {
        if (size == 0)
            return null;
        int s = --size;
        modCount++;
        Object result = queue[0];
        Object x = queue[s];
        queue[s] = null;
        if (s > 1)
            siftDown(0, x);
    else
        queue[0] = x;

        return result;
    }

    public Object peekFirst() {
        if (size == 0)
            return null;
        return queue[0];
    }

    public Object pollLast() {
    if (size < 2)
        return pollFirst();

        int s = --size; //1
        modCount++;
        Object result = queue[1];
        Object x = queue[s];

    queue[s] = null;
    if (s > 1)
        siftUp(1, x);

        return result;
    }

    public Object peekLast() {
        if (size == 0)
            return null;
    if (size == 1)
        return peekFirst();
    
    return queue[1];
    }

    public Object getLast() {
        Object x = peek();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    public Object removeLast() {
        Object x = pollLast();
        if (x != null)
            return x;
        else
            throw new NoSuchElementException();
    }

    private void grow(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
    int oldCapacity = queue.length;
        // Double size if small; else grow by 50%
        int newCapacity = ((oldCapacity < 64)?
                           ((oldCapacity + 1) * 2):
                           ((oldCapacity / 2) * 3));
        if (newCapacity < 0) // overflow
            newCapacity = Integer.MAX_VALUE;
        if (newCapacity < minCapacity)
            newCapacity = minCapacity;
        
        //queue = Arrays.copyOf(queue, newCapacity);
        Object[] newQueue = new Object[newCapacity];
        System.arraycopy(queue, 0, newQueue, 0, queue.length);
        queue = newQueue;
    }

    private int indexOf(Object o) {
    if (o != null) {
            for (int i = 0; i < size; i++)
                if (o.equals(queue[i]))
                    return i;
        }
        return -1;
    }

    private Object removeAt(int i) {
        // assert i >= 0 && i < size;
        modCount++;
        int s = --size;
        if (s == i) // removed last element
            queue[i] = null;
        else {
            Object moved = queue[s];
            queue[s] = null;
        if (s > 1) {
        siftDown(i, moved);
        if (queue[i] == moved) {
            siftUp(i, moved);
            if (queue[i] != moved)
            return moved;
        }
        } else
        queue[0] = moved;
        }
        return null;
    }

    private int siftUp(int k, Object x)
    {
    Comparable key = (Comparable) x;
    while (k != 0) {
        int predecessor = findPredecessor(k);
        // assert predecessor < size : "Predecessor Outside Limit: " + predecessor;
        Object o = queue[predecessor];
        if (key.compareTo(o) >= 0)
        break;
        queue[k] = o;
        k = predecessor;
    }
    queue[k] = key;
    return k;
    }
    
    private int siftDown(int k, Object x)
    {
    Comparable key = (Comparable)x;
    while (k != 1) {
        int successor = findSuccessor(k);
        // assert successor < size : "Successor Outside Limit: " + successor;
        Object o = queue[successor];
        if (key.compareTo(o) <= 0)
        break;
        queue[k] = o;
        k = successor;
    }
    queue[k] = key;
    return k;
    }

    private int findPredecessor(int index)
    {
    if (index == 0)
        return 0;

    if ((index & 0x1) == 0) { // bubble is in min-heap
        if ((index & 0x2) == 0)
        return (index >>> 1) - 2; //B0
        else
        return (index >>> 1) - 1; //B1
    } else { // bubble is in max-heap
        int leftIP = (index << 1) + 1;
        int rightIP = leftIP + 2;
        if (rightIP < size) { //B2
        Comparable left = (Comparable)queue[leftIP];
        if (left.compareTo(queue[rightIP]) >= 0)
            return leftIP;
        else
            return rightIP;
        } else if (leftIP < size)
        return leftIP; //B3 + B4
        else
        return index - 1; //B5 + B6 + B7
    }
    }

    private int findSuccessor(int index)
    {
    if (index == 1)
        return 1;

    if ((index & 0x1) == 0) { // bubble is in min-heap
        int leftIS = (index << 1) + 2;
        int rightIS = leftIS + 2;
        if (rightIS < size) { //T0
        Comparable left = (Comparable)queue[leftIS];
        if (left.compareTo(queue[rightIS]) <= 0)
            return leftIS;
        else
            return rightIS;
        } else if (leftIS < size)
        return leftIS; //T3
        else if (index + 1 < size)
        return index + 1;
        else
        return findSuccessor(index + 1);
    } else { // bubble is in max-heap
        if ((index & 0x2) == 0)
        return (index >>> 1) - 1; //T2
        else
        return (index >>> 1); //T1
    }
    }

    public static final void main(String[] args)
    {
    Random rndm = new Random();
    PriorityDeque deque = new PriorityDeque();
    List list = new ArrayList();

//  for (int i = 0; i < 20; i++) {
//      Integer j = new Integer(rndm.nextInt());
//      deque.offer(i);
//      list.add(i);
//  }
    
    System.err.println("<===== Starting =====> S:" + deque.size());

    int j = 0, adds = 0, removes = 0;
    while (true) {
        System.err.println("<===== 100k Operations =====> S:" + deque.size() + " A:" + adds + " R:" + removes);
        
        if (rndm.nextBoolean()) {
        if (deque.size() > 20)
            continue;

        Integer i = new Integer(rndm.nextInt());
        deque.offer(i);
        list.add(i);
        adds++;
        } else {
        if (deque.size() == 0)
            continue;

        switch (rndm.nextInt(3)) {
        case 0:
            {
            Object o = list.remove(rndm.nextInt(list.size()));
            boolean r = deque.remove(o);
            // assert r : "Couldn't remove " + o + " from Deque";
            removes++;
            } break;
        case 1:
            {
            Object o = deque.pollFirst();
            boolean r = list.remove(o);
            // assert r : "Object " + o + " should not have been in the Deque";
            removes++;
            } break;
        case 2:
            {
            Object o = deque.pollLast();
            boolean r = list.remove(o);
            // assert r : "Object " + o + " should not have been in the Deque";
            removes++;
            } break;
        default:
            throw new IllegalStateException();
        }
        }
    }
    }
    
    public String toString()
    {
    	Object[] temp = new Object[size];
        System.arraycopy(queue, 0, temp, 0, temp.length);
    	return Arrays.toString(temp);
    }
}
