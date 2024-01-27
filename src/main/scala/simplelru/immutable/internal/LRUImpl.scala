package simplelru.immutable.internal

import simplelru.immutable.LRU
import simplelru.immutable.internal.LRUImpl._

final private[simplelru] case class LRUImpl[K, V](first: Option[K],
                                                  last: Option[K],
                                                  items: Map[K, Entry[K, V]],
                                                  capacity: Int
) extends LRU[K, V] {

  if (capacity < 1) throw new IllegalArgumentException("capacity must be greater than 0")

  override def toString: String =
    s"LRU(first=$first, last=$last, items=${items.keys}, capacity=$capacity)"

  override def length: Int = items.size

  override def isEmpty: Boolean = items.isEmpty

  override def contains(key: K): Boolean = items.contains(key)

  override def headOption: Option[(K, V)] = first.map(key => (key, items(key).value))

  override def backOption: Option[(K, V)] = last.map(key => (key, items(key).value))

  override def add(key: K, value: V): (LRU[K, V], Option[(K, V)]) =
    items match {
      case _ if items.isEmpty       => (just(key, value, capacity), None)
      case _ if items.contains(key) => (moveToFront(this, key, value), None)
      case _ if items.size >= capacity =>
        val (newList, removed) = removeLast(this)
        (addToFront(newList, key, value), removed)
      case _ => (addToFront(this, key, value), None)
    }

  override def get(key: K): (LRU[K, V], Option[V]) =
    items.get(key) match {
      case None        => (this, None)
      case Some(entry) => (moveToFront(this, key, entry.value), Some(entry.value))
    }

  override def peek(key: K): (LRU[K, V], Option[V]) =
    items.get(key) match {
      case None        => (this, None)
      case Some(entry) => (this, Some(entry.value))
    }

  override def removeOldest(): (LRU[K, V], Option[(K, V)]) =
    removeLast(this)

  override def remove(key: K): (LRU[K, V], Option[V]) =
    removeFromList(this, key)

  override def iterator: Iterator[(K, V)] = {
    def loop(key: Option[K]): Iterator[(K, V)] =
      key match {
        case None => Iterator.empty
        case Some(k) =>
          items.get(k) match {
            case None        => Iterator.empty
            case Some(entry) => Iterator.single((k, entry.value)) ++ loop(entry.next)
          }
      }

    loop(first)
  }
}

private[immutable] object LRUImpl {
  private def just[K, V](key: K, value: V, capacity: Int): LRUImpl[K, V] =
    new LRUImpl(Some(key), Some(key), Map(key -> Entry(value)), capacity)

  private def moveToFront[K, V](list: LRUImpl[K, V], key: K, value: V): LRU[K, V] =
    list.items match {
      case _ if !list.items.contains(key) => list
      case _ if list.first.contains(key)  => list
      case _ if list.last.contains(key)   => addToFront(removeLast(list)._1, key, value)
      case _                              => addToFront(removeFromList(list, key)._1, key, value)
    }

  private def addToFront[K, V](list: LRUImpl[K, V], key: K, value: V): LRUImpl[K, V] =
    list.first match {
      case None => just(key, value, list.capacity)
      case Some(first) =>
        list.copy(
          first = Some(key),
          items = list.items
            .updatedWith(first)(_.map(_.copy(prev = Some(key))))
            .updated(key, Entry(value, next = Some(first)))
        )
    }

  private def removeFromList[K, V](list: LRUImpl[K, V], key: K): (LRUImpl[K, V], Option[V]) =
    list.items.get(key) match {
      case None => (list, None)
      case Some(Entry(value, None, None)) =>
        (
          list.copy(first = None, last = None, items = list.items.removed(key)),
          Some(value)
        )
      case Some(Entry(value, Some(prev), None)) =>
        (
          list.copy(
            first = Some(prev),
            items = list.items
              .updatedWith(prev)(_.map(_.copy(next = None)))
              .removed(key)
          ),
          Some(value)
        )
      case Some(Entry(value, None, Some(next))) =>
        (
          list.copy(
            last = Some(next),
            items = list.items
              .updatedWith(next)(_.map(_.copy(prev = None)))
              .removed(key)
          ),
          Some(value)
        )
      case Some(Entry(value, Some(prev), Some(next))) =>
        (
          list.copy(
            items = list.items
              .updatedWith(prev)(_.map(_.copy(next = Some(next))))
              .updatedWith(next)(_.map(_.copy(prev = Some(prev))))
              .removed(key)
          ),
          Some(value)
        )
    }

  private def removeLast[K, V](list: LRUImpl[K, V]): (LRUImpl[K, V], Option[(K, V)]) =
    list.last match {
      case None => (list, None)
      case Some(lastKey) =>
        list.items.get(lastKey) match {
          case None => (list, None)
          case Some(Entry(lastValue, None, None)) =>
            (
              list.copy(
                first = None,
                last = None,
                items = list.items.removed(lastKey)
              ),
              Some((lastKey, lastValue))
            )
          case Some(Entry(lastValue, None, Some(next))) =>
            (
              list.copy(
                last = Some(next),
                items = list.items
                  .updatedWith(next)(_.map(_.copy(prev = None)))
                  .removed(lastKey)
              ),
              Some((lastKey, lastValue))
            )
          case Some(Entry(lastValue, Some(prev), _)) =>
            (
              list.copy(
                last = Some(prev),
                items = list.items
                  .removed(lastKey)
                  .updatedWith(prev)(_.map(_.copy(next = None)))
              ),
              Some((lastKey, lastValue))
            )
        }
    }
}
