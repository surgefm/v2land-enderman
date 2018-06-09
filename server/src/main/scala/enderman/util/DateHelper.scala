package enderman.util

import java.time.ZoneId
import java.util.Date
import java.util.concurrent.TimeUnit

import scala.concurrent.duration._

object DateHelper {
  import enderman.Config

  def dayTime(duration: Int = 1): Long = TimeUnit.DAYS.toMillis(duration)
  def hourTime(duration: Int = 1): Long = TimeUnit.HOURS.toMillis(duration)
  def minuteTime(duration: Int = 1): Long = TimeUnit.MINUTES.toMillis(duration)

  def yesterdayDate = {
    val now = new Date().getTime
    val yesterdayMs = if (Config.isProduction) {
      now - ((now + hourTime(8)) % dayTime())
    } else {
      now - (now % dayTime())
    }
    new Date(yesterdayMs)
  }

  def tomorrowDate = {
    new Date(yesterdayDate.getTime + dayTime())
  }

  def beforeDay(num: Int, target: Date) =
    new Date(target.getTime - num * dayTime())

  object duration {

    def delayToTomorrow = {
      Duration(tomorrowDate.getTime - new Date().getTime, TimeUnit.MILLISECONDS)
    }

    def delayToNextWeekMonday9Am = {
      val dateP8 = new Date().toInstant().atZone(ZoneId.of("GMT+8"))
      val dayOfWeek = dateP8.getDayOfWeek
      val remainDays = 8 - dayOfWeek.getValue
      var targetDay = dateP8.plusDays(remainDays)

      val hourOfDay = dateP8.getHour
      if (hourOfDay >= 9) {
        targetDay = targetDay.minusHours(hourOfDay - 9)
      } else {
        targetDay = targetDay.plusHours(9 - hourOfDay)
      }

      Duration(Date.from(targetDay.toInstant).getTime - new Date().getTime, TimeUnit.MILLISECONDS)
    }

    def delayToNextFiveMinutes = {
      val now = new Date().getTime
      val fiveMinutes = minuteTime(5)
      val remain = now % fiveMinutes
      (fiveMinutes - remain) milliseconds
    }

  }

}
