package simplelru.mutable.internal

case class Entry[K, V](key: K, var value: V, var prev: Entry[K, V] = null, var next: Entry[K, V] = null) {
  override def toString: String = "Entry(" + key + ", " + value + ")"
}
