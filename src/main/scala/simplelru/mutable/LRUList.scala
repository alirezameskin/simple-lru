package simplelru
package mutable

import simplelru.mutable.internal.{Entry, EvictList}

import scala.collection.{immutable, mutable}

/**
 * LRUList is a mutable LRU cache implementation.
 *
 * @param capacity
 *   the maximum number of items to store in the list
 * @tparam K
 *   the key type
 * @tparam V
 *   the value type
 */
class LRUList[K, V](val capacity: Int) {

  private val items: mutable.Map[K, Entry[K, V]] = mutable.HashMap.empty[K, Entry[K, V]]
  private val evictList: EvictList[K, V] = EvictList.empty[K, V]

  /**
   * Returns the number of items in the list.
   *
   * @return
   */
  def length: Int = items.size

  /**
   * Returns true if the list is empty.
   *
   * @return
   */
  def isEmpty: Boolean = items.isEmpty

  /**
   * Returns an iterator over the items in the list.
   *
   * @return
   */
  def iterator: Iterator[V] = evictList.iterator.map(_.value)

  /**
   * Returns an immutable list of the items in the list.
   *
   * @return
   */
  def toList: immutable.List[(K, V)] = immutable.List.from(evictList.iterator.map(e => (e.key, e.value)))

  /**
   * Add adds a value to the list. Returns true if an eviction occurred.
   *
   * @param key
   *   the key to add
   * @param value
   *   the value to add
   * @return
   *   true if an eviction occurred
   */
  def add(key: K, value: V): Boolean =
    items.get(key) match {
      case Some(entry) =>
        evictList.moveToFront(entry)
        entry.value = value
        false

      case None =>
        val entry = evictList.pushFront(key, value)
        items.put(key, entry)

        if (evictList.length > capacity) {
          removeOldest()
          return true
        }

        false;
    }

  /**
   * Return the value stored in the list for a key, or None if not found.
   *
   * @param key
   *   the key to lookup
   * @return
   *   the value stored in the list for a key, or None if not found.
   */
  def get(key: K): Option[V] =
    items.get(key) match {
      case Some(entry) =>
        evictList.moveToFront(entry)
        Some(entry.value)

      case None =>
        None
    }

  /**
   * Checks if a key is in the list, without updating the recent-ness
   *
   * @param key
   *   the key to lookup
   * @return
   *   true if the key is in the list, false otherwise.
   */
  def contains(key: K): Boolean =
    items.contains(key)

  /**
   * Returns the key value (or None if not found) without updating the recent-ness
   *
   * @param key
   *   the key to lookup
   * @return
   *   the key value (or None if not found) without updating the recent-ness
   */
  def peek(key: K): Option[V] =
    items.get(key) match {
      case Some(entry) =>
        Some(entry.value)

      case None =>
        None
    }

  /**
   * Removes the provided key from the list, returning if the value was contained.
   *
   * @param key
   *   the key to remove
   * @return
   *   true if the value was contained in the list, false otherwise.
   */
  def remove(key: K): Option[V] =
    items.get(key) match {
      case Some(entry) =>
        evictList.remove(entry)
        items.remove(key)
        Some(entry.value)

      case None =>
        None
    }

  /**
   * Removes the oldest item from the list if there is any and returns its value.
   *
   * @return
   *   the value of the oldest item, or None if the list was empty.
   */
  def removeOldest(): Option[V] =
    evictList.removeOldest() match {
      case Some(entry) =>
        items.remove(entry.key)
        Some(entry.value)

      case None =>
        None
    }

  /**
   * Returns the value of the newest item without removing it from the list.
   *
   * @return
   *   the value of the newest item, or None if the list was empty.
   */
  def headOption: Option[V] =
    evictList.headOption.map(_.value)

  /**
   * Returns the value of the oldest item without removing it from the list.
   *
   * @return
   *   the value of the oldest item, or None if the list was empty.
   */
  def backOption: Option[V] =
    evictList.backOption.map(_.value)
}

object LRUList {
  def apply[K, V](capacity: Int): LRUList[K, V] = new LRUList[K, V](capacity)
}
