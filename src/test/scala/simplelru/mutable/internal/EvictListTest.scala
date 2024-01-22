package simplelru.mutable.internal

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

object EvictListTest extends Properties("EvictList") {
  property("add") = forAll((key: String, value: Int) => {
    val list = EvictList.empty[String, Int]
    list.pushFront(key, value)
    list.headOption.exists(_.key == key) && list.length == 1 && list.backOption.exists(_.key == key)
  })

  property("remove") = forAll((key: String, value: Int) => {
    val list = EvictList.empty[String, Int]
    val entry = list.pushFront(key, value)
    list.remove(entry)
    list.headOption.isEmpty && list.length == 0 && list.backOption.isEmpty
  })

  val stringListGen: Gen[List[String]] =
    Gen.listOf(Gen.alphaStr).suchThat(list => list.size >= 0 && list.size <= 1000)
  val indexGen: Gen[Int] = Gen.choose(0, 1000)

  property("add multiple items the evictlist should store the reverse order ") = forAll(stringListGen) { list =>
    val evictList = EvictList.empty[String, String]
    list.foreach { s =>
      evictList.pushFront(s, s.capitalize)
    }

    evictList.length == list.length && list.reverse.map(s => (s, s.capitalize)) == evictList.iterator.toList.map(e =>
      (e.key, e.value)
    )
  }

  property("Adding and removing some items should work") = forAll(stringListGen, indexGen) { (list, index) =>
    val evictList = EvictList.empty[String, String]
    val entries = list.map { s =>
      evictList.pushFront(s, s.capitalize)
    }

    entries.take(index).foreach {
      evictList.remove
    }

    list.drop(index).size == evictList.iterator.length && list
      .drop(index)
      .reverse
      .map(s => (s, s.capitalize)) == evictList.iterator.toList.map(e => (e.key, e.value))
  }

  property("Adding and removing from the back should work") = forAll(stringListGen, indexGen) { (list, index) =>
    val evictList = EvictList.empty[String, String]
    list.foreach { s => evictList.pushFront(s, s.capitalize) }

    Range(0, index).foreach { _ => evictList.removeOldest() }

    list.drop(index).size == evictList.iterator.length && list
      .drop(index)
      .reverse
      .map(s => (s, s.capitalize)) == evictList.iterator.toList.map(e => (e.key, e.value))
  }

  property("Adding and checking the head and back should work") = forAll(stringListGen) { list =>
    val evictList = EvictList.empty[String, String]
    list.foreach { s => evictList.pushFront(s, s.capitalize) }

    evictList.headOption.map(_.key) == list.lastOption && evictList.backOption.map(_.key) == list.headOption
  }
}
