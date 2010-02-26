/* $Revision: 163 $ $Date: 2009-08-17 15:12:57 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
 * 
 * Copyright (C) 2007-2009  National Library of the Netherlands, 
 *                          Nationaal Archief of the Netherlands, 
 *                          Planets
 *                          KEEP
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 * 
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 * 
 * Project Title: DIOSCURI
 */

package dioscuri.util;

import java.util.Iterator;
import java.util.Queue;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 * @param <E>
 */
public interface Deque<E> extends Queue<E> {

    /**
     *
     * @param e
     */
    void addFirst(E e);

    /**
     *
     * @param e
     */
    void addLast(E e);

    /**
     *
     * @param e
     * @return
     */
    boolean offerFirst(E e);

    /**
     *
     * @param e
     * @return
     */
    boolean offerLast(E e);

    /**
     *
     * @return
     */
    E removeFirst();

    /**
     *
     * @return
     */
    E removeLast();

    /**
     *
     * @return
     */
    E pollFirst();

    /**
     *
     * @return
     */
    E pollLast();

    /**
     *
     * @return
     */
    E getFirst();

    /**
     *
     * @return
     */
    E getLast();

    /**
     *
     * @return
     */
    E peekFirst();

    /**
     *
     * @return
     */
    E peekLast();

    /**
     *
     * @param o
     * @return
     */
    boolean removeFirstOccurrence(Object o);

    /**
     *
     * @param o
     * @return
     */
    boolean removeLastOccurrence(Object o);

    boolean add(E e);

    boolean offer(E e);

    E remove();

    E poll();

    E element();

    E peek();

    /**
     *
     * @param e
     */
    void push(E e);

    /**
     *
     * @return
     */
    E pop();

    boolean remove(Object o);

    boolean contains(Object o);

    public int size();

    Iterator<E> iterator();

    /**
     *
     * @return
     */
    Iterator<E> descendingIterator();
}
