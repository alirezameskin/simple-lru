package simplelru.mutable.internal

import simplelru.mutable.LRU
import scala.collection.{immutable, mutable}

private[mutable] class LRUImpl[K, V](val capacity: Int) extends LRU[K, V] {

  private val items: mutable.Map[K, Entry[K, V]] = mutable.HashMap.empty[K, Entry[K, V]]
  private val evictList: EvictList[K, V] = EvictList.empty[K, V]

  override def length: Int = items.size

  override def isEmpty: Boolean = items.isEmpty

  override def iterator: Iterator[V] = evictList.iterator.map(_.value)

  override def toList: immutable.List[(K, V)] = immutable.List.from(evictList.iterator.map(e => (e.key, e.value)))

  override def add(key: K, value: V): Boolean =
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

  override def get(key: K): Option[V] =
    items.get(key) match {
      case Some(entry) =>
        evictList.moveToFront(entry)
        Some(entry.value)

      case None =>
        None
    }

  override def contains(key: K): Boolean =
    items.contains(key)

  override def peek(key: K): Option[V] =
    items.get(key) match {
      case Some(entry) =>
        Some(entry.value)

      case None =>
        None
    }

  override def remove(key: K): Option[V] =
    items.get(key) match {
      case Some(entry) =>
        evictList.remove(entry)
        items.remove(key)
        Some(entry.value)

      case None =>
        None
    }

  override def removeOldest(): Option[V] =
    evictList.removeOldest() match {
      case Some(entry) =>
        items.remove(entry.key)
        Some(entry.value)

      case None =>
        None
    }

  override def headOption: Option[V] =
    evictList.headOption.map(_.value)

  override def backOption: Option[V] =
    evictList.backOption.map(_.value)
}
