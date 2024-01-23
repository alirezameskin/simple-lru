package simplelru.mutable

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

object LRUTest extends Properties("mutable.LRU") {

  property("should fill up to capacity") = forAll(Gen.choose(10, 100)) { count =>
    val lru = LRU[String, Int](10)
    Range.inclusive(0, count).foreach { i =>
      lru.add(s"key$i", i)
    }

    lru.length == 10 && lru.headOption.contains(count)
  }

  property("should accessing an item should move it to the front of the list") = forAll(Gen.choose(10, 100)) { count =>
    val lru = LRU[String, Int](10)
    Range.inclusive(0, count).foreach { i =>
      lru.add(s"key$i", i)
    }

    val item = count - 3

    lru.length == 10 && lru.get(s"key$item").contains(item) && lru.headOption.contains(item)
  }

  property("contains methods should be true only for the recent items") = forAll(Gen.choose(40, 100)) { count =>
    val lru = LRU[String, Int](10)
    Range.inclusive(0, count).foreach { i =>
      lru.add(s"key$i", i)
    }

    lru.length == 10 && !lru.contains(s"key10") && lru.contains(s"key$count") && lru.contains(s"key${count - 1}") && lru
      .contains(s"key${count - 2}")
  }

  property("peek methods should not move an item to the front of the list") = forAll(Gen.choose(10, 100)) { count =>
    val lru = LRU[String, Int](10)
    Range.inclusive(0, count).foreach { i =>
      lru.add(s"key$i", i)
    }

    val item = count - 5;

    lru.length == 10 && lru.peek(s"key$item").contains(item) && lru.headOption.contains(count)
  }

  property("removeOldest method should remove the oldest item in the list") = forAll { (_: Int) =>
    val lru = LRU[String, Int](10)
    Range.inclusive(0, 200).foreach { i =>
      lru.add(s"key$i", i)
    }

    val evictedItem = lru.removeOldest()

    lru.length == 9 && lru.headOption.contains(200) && lru.backOption.contains(192) && evictedItem.contains(191)
  }

  property("remove method should remove the item from the list") = forAll(Gen.choose(20, 200)) { count =>
    val lru = LRU[String, Int](10)
    Range.inclusive(0, count).foreach { i =>
      lru.add(s"key$i", i)
    }

    val item = count - 5;
    val evictedItem = lru.remove(s"key$item")

    val key3RemoveResult = lru.remove(s"key3") // this should not affect the result of the test

    lru.length == 9 && evictedItem.contains(item) && key3RemoveResult.isEmpty
  }
}
