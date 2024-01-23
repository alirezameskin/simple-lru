package simplelru.immutable

import org.scalacheck.Prop.{all, forAll}
import org.scalacheck.{Gen, Properties}

object LRUTest extends Properties("immutable.LRU") {

  property("should fill up to capacity") = forAll(Gen.choose(10, 100)) { count =>
    val empty = LRU.empty[String, Int](10)
    val lru = Range.inclusive(0, count).foldLeft(empty) { (lru, i) =>
      lru.add(s"key$i", i)._1
    }

    lru.length == 10 && lru.headOption.contains((s"key$count", count))
  }

  property("should accessing an item should move it to the front of the list") = forAll(Gen.choose(10, 100)) { count =>
    val empty = LRU.empty[String, Int](10)
    val filled = Range.inclusive(0, count).foldLeft(empty) { (lru, i) =>
      lru.add(s"key$i", i)._1
    }

    val item = count - 3
    val (accessedLRU, storedValue) = filled.get(s"key$item")

    filled.length == 10 &&
    accessedLRU.length == 10 &&
    storedValue.contains(item) &&
    accessedLRU.headOption.exists(_._2 == item)
  }

  property("contains methods should be true only for the recent items") = forAll(Gen.choose(40, 100)) { count =>

    val empty = LRU.empty[String, Int](10)
    val lru = Range.inclusive(0, count).foldLeft(empty) { (lru, i) =>
      lru.add(s"key$i", i)._1
    }

    lru.length == 10 && !lru.contains(s"key10") && lru.contains(s"key$count") && lru.contains(s"key${count - 1}") && lru
      .contains(s"key${count - 2}")
  }

  property("peek methods should not move an item to the front of the list") = forAll(Gen.choose(10, 100)) { count =>
    val empty = LRU.empty[String, Int](10)
    val lru = Range.inclusive(0, count).foldLeft(empty) { (lru, i) =>
      lru.add(s"key$i", i)._1
    }

    val peekedKey = s"key${count - 5}"
    val (peekedLRU, _) = lru.peek(peekedKey)

    lru.length == 10 && peekedLRU.length == 10 && peekedLRU.contains(peekedKey) && peekedLRU.headOption.contains(count)
    true
  }

  property("removeOldest method should remove the oldest item in the list") = forAll { (_: Int) =>
    val empty = LRU.empty[String, Int](10)
    val lru = Range.inclusive(0, 200).foldLeft(empty) { (lru, i) =>
      lru.add(s"key$i", i)._1
    }

    val (evictedLru, evictedItem) = lru.removeOldest()

    lru.length == 10 &&
    evictedLru.length == 9 &&
    lru.headOption.exists(_._2 == 200) &&
    evictedLru.headOption.exists(_._2 == 200) &&
    evictedLru.backOption.exists(_._2 == 192) &&
    evictedItem.contains((s"key${191}", 191))
  }

  property("remove method should remove the item from the list") = forAll { (_: Int) =>
    val empty = LRU.empty[String, Int](10)
    val lru = Range.inclusive(0, 100).foldLeft(empty) { (lru, i) =>
      lru.add(s"key$i", i)._1
    }

    val item = 100 - 5;
    val (evictedList, evictedItem) = lru.remove(s"key$item")

    val key3RemoveResult = evictedList.remove(s"key3") // this should not affect the result of the test

    lru.length == 10 &&
    evictedList.length == 9 &&
    key3RemoveResult._1.length == 9 &&
    evictedList.headOption.exists(_._2 == 100) && evictedItem.contains(item)
  }
}
