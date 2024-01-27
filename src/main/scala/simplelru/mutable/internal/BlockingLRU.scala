package simplelru.mutable.internal

import simplelru.mutable.LRU
import scala.collection.immutable

import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * BlockingLRU represents a thread-safe, mutable LRU cache implementation. It utilizes a lock-based mechanism, ensuring
 * synchronization for both read and write operations, resulting in blocking behavior.
 *
 * @param underlying
 *   the underlying LRU cache implementation
 * @tparam K
 *   the key type
 * @tparam V
 *   the value type
 */
private[mutable] class BlockingLRU[K, V](val underlying: LRU[K, V]) extends LRU[K, V] {

  private val lock = new ReentrantReadWriteLock()

  override def length: Int = readLock { underlying.length }

  override def isEmpty: Boolean = readLock { underlying.isEmpty }

  override def iterator: Iterator[(K, V)] = readLock { underlying.iterator }

  override def add(key: K, value: V): Boolean = writeLock { underlying.add(key, value) }

  override def get(key: K): Option[V] = readLock { underlying.get(key) }

  override def contains(key: K): Boolean = readLock { underlying.contains(key) }

  override def peek(key: K): Option[V] = readLock { underlying.peek(key) }

  override def remove(key: K): Option[V] = writeLock { underlying.remove(key) }

  override def removeOldest(): Option[V] = writeLock { underlying.removeOldest() }

  override def headOption: Option[V] = readLock { underlying.headOption }

  override def backOption: Option[V] = readLock { underlying.backOption }

  private def readLock[R](f: => R): R = {
    lock.readLock().lock()
    try {
      f
    } finally {
      lock.readLock().unlock()
    }
  }

  private def writeLock[R](f: => R): R = {
    lock.writeLock().lock()
    try {
      f
    } finally {
      lock.writeLock().unlock()
    }
  }
}

object BlockingLRU {
  def apply[K, V](underlying: LRU[K, V]): BlockingLRU[K, V] = new BlockingLRU(underlying)
}
