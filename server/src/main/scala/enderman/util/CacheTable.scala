package enderman.util

import scala.collection.mutable

object FixSizeFIFOMap {

  def empty[KeyType, ValueType] = new FixSizeFIFOMap[KeyType, ValueType]()

}

final class FixSizeFIFOMap[KeyType, ValueType](
  val size: Int = 256) {

  private val kvMap = mutable.HashMap.empty[KeyType, (ValueType, Int)]
  private var counter = 0

  def getAndRemove(key: KeyType): Option[ValueType] = {
    kvMap.get(key) match {
      case Some((result, _)) =>
        kvMap.remove(key)
        Some(result)
      case None => None
    }
  }

  def put(key: KeyType, value: ValueType) = {
    kvMap.put(key, (value, counter))
    counter += 1

    if (kvMap.size > size) {
      kvMap
        .toList
        .sortBy[Int] {
          case (_, (_, cnt)) => cnt * -1 // reverse
        }
        .slice(size, kvMap.size)
        .foreach {
          case (overflowKey, _) => kvMap.remove(overflowKey)
        }
    }
  }

}

object LRUMap {

  def empty[KeyType, ValueType] = new LRUMap[KeyType, ValueType]()

}

final class LRUMap[KeyType, ValueType](
  val size: Int = 256) {

  private val kvMap = mutable.HashMap.empty[KeyType, (ValueType, Int)]
  private var counter = 0

  def get(key: KeyType): Option[ValueType] = {
    kvMap.get(key) match {
      case Some((result, _)) =>
        kvMap.update(key, (result, counter))
        counter += 1
        Some(result)
      case None => None
    }
  }

  def put(key: KeyType, value: ValueType) = {
    kvMap.put(key, (value, counter))
    counter += 1

    if (kvMap.size > size) {
      kvMap
        .toList
        .sortBy[Int] {
          case (_, (_, cnt)) => cnt * -1 // reverse
        }
        .slice(size, kvMap.size)
        .foreach {
          case (overflowKey, _) => kvMap.remove(overflowKey)
        }
    }
  }

}

final class CacheTable[KeyType, ValueType](
  val size: Int = 256) {

  private val fifoMap = new FixSizeFIFOMap[KeyType, ValueType](size)
  private val lruMap = new LRUMap[KeyType, ValueType](size)

  def get(key: KeyType): Option[ValueType] = {
    fifoMap.getAndRemove(key) match {
      case Some(result1) =>
        lruMap.put(key, result1)
        Some(result1)
      case None => lruMap.get(key)
    }
  }

  def put(key: KeyType, value: ValueType) = {
    fifoMap.put(key, value)
  }

}
