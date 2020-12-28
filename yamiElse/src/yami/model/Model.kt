package yami.model

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class Pass { IN, OUT, UNKNOWN }

enum class WorkType { DAY, NIGHT, UNKNOWN }

class RawTick(val name: String, val id: String, val tick: LocalDateTime, val type: Pass,
              val error: Boolean = false, val column: Int = -1) {
    override fun toString(): String = "name:$name, id:$id, tick:$tick, type:$type"
}

class RawStaff(val id: String, val name: String, val project: String)

class PureTick(val tick: LocalDateTime, val type: Pass) {
    override fun toString(): String = "[$tick, $type]"
}

class TimeDuration(val start: LocalTime, val end: LocalTime)

class TickDuration(private val start: LocalDateTime, private val end: LocalDateTime) {

    fun between() = Duration.between(start, end)!!

    fun exist(time: LocalDateTime): Boolean = start == time || end == time

    fun filter(during: TimeDuration): List<TickDuration> {
        val result = java.util.ArrayList<TickDuration>()
        val tickFirst = LocalDateTime.of(start.toLocalDate(), during.start)
        val tickLast = LocalDateTime.of(start.toLocalDate(), during.end)
        if (start.isBefore(tickFirst)) {
            if (tickFirst.isBefore(end)) {
                result.add(TickDuration(start, tickFirst))
                if (tickLast.isBefore(end)) result.addAll(TickDuration(tickLast, end).filter(during))
            } else result.add(TickDuration(start, end))
        } else if (tickFirst.isBefore(start) && start.isBefore(tickLast)) {
            if (tickLast.isBefore(end)) result.addAll(TickDuration(tickLast, end).filter(during))
        } else {
            if (start.toLocalDate() != end.toLocalDate()) {
                val nextStart = LocalDateTime.of(start.toLocalDate().plusDays(1), during.start)
                val nextEnd = LocalDateTime.of(start.toLocalDate().plusDays(1), during.end)
                if (nextStart.isBefore(end)) {
                    if (nextEnd.isBefore(end)) {
                        result.add(TickDuration(start, nextStart))
                        result.addAll(TickDuration(nextEnd, end).filter(during))
                    } else result.add(TickDuration(start, nextStart))
                } else result.add(TickDuration(start, end))
            } else result.add(TickDuration(start, end))
        }
        return result
    }

    override fun toString(): String {
        return "[$start - $end]"
    }
}

class RawDay(val date: LocalDate, val list: ArrayList<PureTick>,
             var workTime: Double = 0.0, var type: WorkType = WorkType.UNKNOWN,
             var outTimes: Int = 0, var outDuration: Double = 0.0,
             val outDuring: ArrayList<TickDuration> = ArrayList())

class RawPerson(val name: String, val id: String, val project: String, val data: List<RawTick>) : Comparable<RawPerson> {

    val workDays = ArrayList<RawDay>()

    override fun compareTo(other: RawPerson): Int {
        val result = this.project.compareTo(other.project)
        if (result == 0) return this.name.compareTo(other.name)
        return result
    }
}

