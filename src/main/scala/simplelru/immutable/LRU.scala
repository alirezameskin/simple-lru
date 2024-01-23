package simplelru.immutable

import simplelru.immutable.internal.LRUImpl

trait LRU[K, V] {

  /**
   * Returns the number of items in the list.
   */
  def length: Int

  /**
   * Returns true if the list is empty.
   */
  def isEmpty: Boolean

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
   * Returns the value of the newest item without removing it from the list.
   *
   * @return
   *   the (key, value) of the newest item, or None if the list was empty.
   */
  def headOption: Option[(K, V)]

  /**
   * Returns the value of the oldest item without removing it from the list.
   *
   * @return
   *   the (key, value) of the oldest item, or None if the list was empty.
   */
  def backOption: Option[(K, V)]

  /**
   * Adds an item to the LRU. If the key was already present in the LRU, the value is changed to the new value passed
   * in. The item added is marked as the most recently accessed item in the LRU returned. If this would cause the LRU to
   * exceed its maximum size, the least recently used item is dropped from the cache.
   */
  def add(key: K, value: V): (LRU[K, V], Option[(K, V)])

  /**
   * Return the value stored in the list for a key, or None if not found. and update the recent-ness
   */
  def get(key: K): (LRU[K, V], Option[V])

  /**
   * Returns the new LRU and the value (or None if not found) without updating the recent-ness
   */
  def peek(key: K): (LRU[K, V], Option[V])

  /**
   * Removes the oldest item from the LRU. Returns the new LRU, and the (key, value) removed if the LRU was not empty.
   */
  def removeOldest(): (LRU[K, V], Option[(K, V)])

  /**
   * Removes an item from the LRU. Returns the new LRU, and the value removed if the key was present.
   */
  def remove(key: K): (LRU[K, V], Option[V])
}

object LRU {
  def empty[K, V](capacity: Int): LRU[K, V] =
    LRUImpl(None, None, Map.empty, capacity)
}
