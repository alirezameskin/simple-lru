package simplelru.mutable.internal

/**
 * Duplicate of [[scala.collection.mutable.MutationTracker]].
 */

import java.util.ConcurrentModificationException
import scala.collection.AbstractIterator

/**
 * Utilities to check that mutations to a client that tracks its mutations have not occurred since a given point.
 * [[Iterator `Iterator`]]s that perform this check automatically during iteration can be created by wrapping an
 * `Iterator` in a [[MutationTracker.CheckedIterator `CheckedIterator`]], or by manually using the
 * [[MutationTracker.checkMutations() `checkMutations`]] and
 * [[MutationTracker.checkMutationsForIteration() `checkMutationsForIteration`]] methods.
 */
private[mutable] object MutationTracker {

  /**
   * Checks whether or not the actual mutation count differs from the expected one, throwing an exception, if it does.
   *
   * @param expectedCount
   *   the expected mutation count
   * @param actualCount
   *   the actual mutation count
   * @param message
   *   the exception message in case of mutations
   * @throws ConcurrentModificationException
   *   if the expected and actual mutation counts differ
   */
  @throws[ConcurrentModificationException]
  def checkMutations(expectedCount: Int, actualCount: Int, message: String): Unit = {
    if (actualCount != expectedCount) throw new ConcurrentModificationException(message)
  }

  /**
   * Checks whether or not the actual mutation count differs from the expected one, throwing an exception, if it does.
   * This method produces an exception message saying that it was called because a backing collection was mutated during
   * iteration.
   *
   * @param expectedCount
   *   the expected mutation count
   * @param actualCount
   *   the actual mutation count
   * @throws ConcurrentModificationException
   *   if the expected and actual mutation counts differ
   */
  @throws[ConcurrentModificationException]
  @inline def checkMutationsForIteration(expectedCount: Int, actualCount: Int): Unit =
    checkMutations(expectedCount, actualCount, "mutation occurred during iteration")

  /**
   * An iterator wrapper that checks if the underlying collection has been mutated.
   *
   * @param underlying
   *   the underlying iterator
   * @param mutationCount
   *   a by-name provider of the current mutation count
   * @tparam A
   *   the type of the iterator's elements
   */
  final class CheckedIterator[A](underlying: Iterator[A], mutationCount: => Int) extends AbstractIterator[A] {
    private[this] val expectedCount = mutationCount

    def hasNext: Boolean = {
      checkMutationsForIteration(expectedCount, mutationCount)
      underlying.hasNext
    }
    def next(): A = underlying.next()
  }
}
