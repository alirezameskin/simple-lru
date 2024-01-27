simple-lru
==========


LRU cache example (mutable and non thread-safe)
=================

```scala
val capability = 10
val cache = new simplelru.mutable.LRU[String, Int](10)
for (elem <- Range.inclusive(0, 200)) {
  cache.add(s"key$elem", elem)
}

assert(cache.length == capability)
assert(cache.headOption.contains(200))

// accessing an item should move it to the front of the list
cache.get("key195")

assert(cache.headOption.contains(195))

```

LRU cache example (mutable and thread-safe)
=================

```scala
val capability = 10
val cache = new simplelru.mutable.LRU.blocking[String, Int](10)
for (elem <- Range.inclusive(0, 200)) {
  cache.add(s"key$elem", elem)
}

assert(cache.length == capability)
assert(cache.headOption.contains(200))

// accessing an item should move it to the front of the list
cache.get("key195")

assert(cache.headOption.contains(195))
```


LRU cache example (immutable)
=================

```scala
val emptyCache = simplelru.immutable.LRU.empty[String, Int](10)
val capability = 10
val cache = Range.inclusive(0, 200)
  .foldLeft(emptyCache) {
    case (cache, elem) => cache.add(s"key$elem", elem)._1
  }

assert(cache.length == capability)
assert(cache.headOption.contains(200))

// accessing an item should move it to the front of the list
val (updateCache, _) = cache.get("key195")

assert(updateCache.headOption.contains(195))
```
