package simplelru.immutable.internal

private[immutable] case class Entry[K, A](value: A, prev: Option[K] = None, next: Option[K] = None)
