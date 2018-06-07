package enderman.util

import org.scalatest.FunSuite

class CacheTableTest extends FunSuite {

  test("FixSizeFIFOMap basic put and get") {
    val map = new FixSizeFIFOMap[String, Int](4)
    map.put("foo", 1)
    assert(map.getAndRemove("foo").get == 1)
    assert(map.getAndRemove("foo") == None)
  }

  test("FixSizeFIFOMap put overflow") {
    val map = new FixSizeFIFOMap[String, Int](4)
    for (num <- 1 to 4) {
      map.put(num.toString, num)
    }
    map.put("foo", 100)
    assert(map.getAndRemove("1") == None)
    map.put("bar", 100)
    assert(map.getAndRemove("2") == None)
    assert(map.getAndRemove("3").get == 3)
  }

  test("LRUMap basic put and get") {
    val map = new LRUMap[String, Int](4)
    map.put("foo", 1)
    assert(map.get("foo").get == 1)
  }

  test("LRUMap put overflow") {
    val map = new LRUMap[String, Int](4)
    for (num <- 1 to 4) {
      map.put(num.toString, num)
    }
    assert(map.get("3").get == 3)
    assert(map.get("3").get == 3)
    assert(map.get("3").get == 3)
    assert(map.get("1").get == 1)
    assert(map.get("1").get == 1)
    assert(map.get("4").get == 4)
    map.put("foo", 0)
    assert(map.get("2") == None)
  }

}
