simple-lru
==========


LRU cache example
=================

```scala
val capability = 10
val cache = new LRUList[String, Int](10)
for (elem <- Range.inclusive(0, 200)) {
  cache.add(s"key$elem", elem)
}

assert(cache.length == capability)
assert(cache.headOption.contains(200))

// accessing an item should move it to the front of the list
cache.get("key195")

assert(cache.headOption.contains(195))

```