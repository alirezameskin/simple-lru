package simplelru.mutable.internal

import scala.collection.AbstractIterator

/**
 * EvictList is a mutable doubly-linked list implementation.
 *
 * @tparam K
 *   the key type
 * @tparam V
 *   the value type
 */
private[simplelru] class EvictList[K, V] {
  private var size = 0
  private var first: Entry[K, V] = null
  private var last: Entry[K, V] = null

  /**
   * Returns the number of items in the list.
   */
  def length: Int = size

  /**
   * Returns the first item in the list.
   */
  def headOption: Option[Entry[K, V]] = Option(first)

  /**
   * Returns the last item in the list.
   */
  def backOption: Option[Entry[K, V]] = Option(last)

  /**
   * Iterates over the items in the list.
   */
  def iterator: Iterator[Entry[K, V]] = new AbstractIterator[Entry[K, V]] {
    private[this] var current = first

    def hasNext: Boolean = current != null

    def next(): Entry[K, V] = {
      val r = current;
      current = current.next;
      r
    }
  }

  /**
   * Pushes a new entry to the front of the list.
   */
  def pushFront(key: K, value: V): Entry[K, V] = {
    val f = first
    val entry = Entry(key, value, null, f)
    first = entry

    if (f == null) {
      last = entry
    } else {
      f.prev = entry
    }
    size += 1
    entry
  }

  /**
   * Moves an existing entry to the front of the list.
   */
  def moveToFront(entry: Entry[K, V]): Unit =
    if (entry eq first) {
      // already in front
    } else if (entry eq last) {
      // last element
      val f = first

      last = entry.prev
      first = entry

      entry.next = f
      f.prev = entry
    } else {
      // middle element
      val f = first
      val next = entry.next
      val prev = entry.prev

      first = entry
      entry.next = f
      f.prev = entry

      prev.next = next
      next.prev = prev
    }

  /**
   * Removes an entry from the list.
   */
  def remove(entry: Entry[K, V]): Unit = {
    if (entry eq first) {
      first = entry.next
    } else {
      entry.prev.next = entry.next
    }

    if (entry eq last) {
      last = entry.prev
    } else {
      entry.next.prev = entry.prev
    }

    size -= 1
  }

  /**
   * Removes the oldest entry from the list and returns the entry if there was any.
   */
  def removeOldest(): Option[Entry[K, V]] =
    if (size == 0) {
      // empty list
      None
    } else if (size == 1) {
      // only one element
      val f = first
      first = null
      last = null
      size = 0
      Some(f)
    } else {
      // more than one element
      size -= 1
      val l = last
      last = l.prev
      last.next = null
      Some(l)
    }

}

object EvictList {

  /**
   * Creates a new empty EvictList.
   *
   * @tparam K
   * @tparam V
   * @return
   */
  def empty[K, V]: EvictList[K, V] = new EvictList[K, V]()
}
