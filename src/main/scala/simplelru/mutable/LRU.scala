package simplelru
package mutable

import simplelru.mutable.internal.{BlockingLRU, LRUImpl}

import scala.collection.immutable

/**
 * LRU is a mutable LRU cache implementation.
 *
 * @tparam K
 *   the key type
 * @tparam V
 *   the value type
 */
trait LRU[K, V] {

  /**
   * Returns the number of items in the list.
   *
   * @return
   */
  def length: Int

  /**
   * Returns true if the list is empty.
   *
   * @return
   */
  def isEmpty: Boolean

  /**
   * Returns an iterator over the items in the list.
   *
   * @return
   */
  def iterator: Iterator[(K, V)]

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
  def add(key: K, value: V): Boolean

  /**
   * Return the value stored in the list for a key, or None if not found.
   *
   * @param key
   *   the key to lookup
   * @return
   *   the value stored in the list for a key, or None if not found.
   */
  def get(key: K): Option[V]

  /**
   * Checks if a key is in the list, without updating the recent-ness
   *
   * @param key
   *   the key to lookup
   * @return
   *   true if the key is in the list, false otherwise.
   */
  def contains(key: K): Boolean

  /**
   * Returns the key value (or None if not found) without updating the recent-ness
   *
   * @param key
   *   the key to lookup
   * @return
   *   the key value (or None if not found) without updating the recent-ness
   */
  def peek(key: K): Option[V]

  /**
   * Removes the provided key from the list, returning if the value was contained.
   *
   * @param key
   *   the key to remove
   * @return
   *   true if the value was contained in the list, false otherwise.
   */
  def remove(key: K): Option[V]

  /**
   * Removes the oldest item from the list if there is any and returns its value.
   *
   * @return
   *   the value of the oldest item, or None if the list was empty.
   */
  def removeOldest(): Option[V]

  /**
   * Returns the value of the newest item without removing it from the list.
   *
   * @return
   *   the value of the newest item, or None if the list was empty.
   */
  def headOption: Option[V]

  /**
   * Returns the value of the oldest item without removing it from the list.
   *
   * @return
   *   the value of the oldest item, or None if the list was empty.
   */
  def backOption: Option[V]
}

object LRU {

  /**
   * Returns a mutable LRU implementation that is not thread-safe.
   * @param capacity
   * @tparam K
   * @tparam V
   * @return
   */
  def apply[K, V](capacity: Int): LRU[K, V] = LRUImpl[K, V](capacity)

  /**
   * Returns a blocking LRU cache implementation.
   *
   * @param capacity
   * @tparam K
   * @tparam V
   * @return
   */
  def blocking[K, V](capacity: Int): LRU[K, V] = BlockingLRU(LRUImpl(capacity))
}
