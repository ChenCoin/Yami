package yami.model

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.stream.Collectors

class Count(private val context: Context, private val callback: (Int, String) -> Unit) {
    fun count(data: List<RawTick>, people: List<RawStaff>) {
        callback(30, "统计数据")
        val result: List<RawPerson> = data.groupBy { it.name }
                .flatMap {
                    var result = ArrayList<RawPerson>()
                    for (i in people.indices) {
                        if (it.key == people[i].name) {
                            val ticks = it.value.stream()
                                    .sorted { a, b -> a.tick.compareTo(b.tick) }
                                    .collect(Collectors.toList())
                            val person = RawPerson(it.key, people[i].id, people[i].project, ticks)
                            countPerson(person)
                            result = arrayListOf(person)
                        }
                    }
                    result
                }.sorted().toList()
        callback(70, "统计数据完成")
        callback(80, "写入考勤计算结果文件")
        if (!context.outputDir.exists()) context.outputDir.mkdirs()

        if (!Thread.interrupted() && Writer(context, callback).outDetailXls(result)) {
            callback(90, "写入考勤计算结果文件成功")
            callback(90, "写入考勤工时数据计算结果")
            if (!Thread.interrupted()) Writer(context, callback).outMonthXls(result)
            else callback(90, "取消成功")
        } else callback(80, "取消成功")
    }

    private fun countPerson(person: RawPerson) {
        var lastTick = LocalDateTime.MIN
        var lastType = Pass.OUT
        person.data.forEach {
            if (Duration.between(lastTick, it.tick).toHours() < 2 ||
                    (lastType == Pass.IN && it.type == Pass.OUT
                            && Duration.between(lastTick, it.tick).toHours() < 20) ||
                    (person.workDays.isNotEmpty() && person.workDays.last().date == it.tick.toLocalDate())
            ) {
                person.workDays.last().list.add(PureTick(it.tick, it.type))
            } else {
                person.workDays.add(RawDay(it.tick.toLocalDate(), arrayListOf(PureTick(it.tick, it.type))))
            }
            lastTick = it.tick
            lastType = it.type
        }
        person.workDays.forEach { day ->
            val tickList = ArrayList<PureTick>()
            tickList.addAll(day.list)
            if (day.date.dayOfMonth == 1 && day.list.first().type == Pass.OUT) {
                tickList.add(0, PureTick(day.date.atStartOfDay(), Pass.IN))
            }
            if (day.date.dayOfMonth == day.date.month.length(day.date.isLeapYear) && day.list.last().type == Pass.IN) {
                tickList.add(PureTick(day.date.atTime(23, 59, 59), Pass.OUT))
            }

            val withoutRepeat = ArrayList<PureTick>()
            var match = Pass.IN
            var matchNext = Pass.OUT
            var error = false
            if (tickList.size > 1)
                for (index in 0 until tickList.size - 1) {
                    val item = tickList[index]
                    val itemNext = tickList[index + 1]
                    if (item.type == match && itemNext.type == matchNext) {
                        withoutRepeat.add(item)
                        if (index != tickList.size - 2) {
                            val temp = match
                            match = matchNext
                            matchNext = temp
                        } else withoutRepeat.add(itemNext)
                    } else if (tickList[index].type == match && tickList[index + 1].type == match
                            && Duration.between(item.tick, itemNext.tick).toMinutes() < 3) {
                        if (index == tickList.size - 2) withoutRepeat.add(itemNext)
                    } else {
                        error = true
                        break
                    }
                }
            else error = true

            if (!error && withoutRepeat.size > 1 && withoutRepeat.size % 2 == 0 && withoutRepeat[0].type == Pass.IN) {
                if (person.project.toUpperCase().contains("T1") || day.date.dayOfWeek == DayOfWeek.SATURDAY
                        || day.date.dayOfWeek == DayOfWeek.SUNDAY) {
                    // don't include lunch time
                    var workTime = Duration.ZERO
                    for (i in 0 until withoutRepeat.size / 2) {
                        workTime = workTime.plus(Duration.between(withoutRepeat[i * 2].tick, withoutRepeat[i * 2 + 1].tick))
                    }
                    day.workTime = workTime.toMinutes().toDouble()

                    var outDuration = Duration.ZERO
                    if (withoutRepeat.size > 3) for (i in 0 until withoutRepeat.size / 2 - 1) {
                        val duration = TickDuration(withoutRepeat[i * 2 + 1].tick, withoutRepeat[i * 2 + 2].tick)
                        day.outDuring.add(duration)
                        outDuration = outDuration.plus(duration.between())
                    }
                    day.outDuration = outDuration.toMinutes().toDouble()
                    day.outTimes = if (!person.project.toUpperCase().contains("T1"))
                        day.outDuring.filter { it.between().seconds > 600 }.size
                    else day.outDuring.size
                } else {
                    // lunch time is request
                    countLunch(day, withoutRepeat)
                }
            } else day.workTime = -1.0

            if (person.project.toUpperCase().contains("T1")) {
                if (withoutRepeat.isNotEmpty()) {
                    if (withoutRepeat.first().tick.toLocalTime()
                                    .isBefore(LocalTime.of(12, 0))) {
                        day.type = WorkType.DAY
                    } else if (withoutRepeat.first().tick.toLocalDate() !=
                            withoutRepeat.last().tick.toLocalDate()) {
                        day.type = WorkType.NIGHT
                    }
                }
            }
        }
    }

    private fun countLunch(day: RawDay, data: ArrayList<PureTick>) {
        // 数据为空，或者跨两个午餐时间，视为异常
        assert(data.isNotEmpty())
        assert(data.size / 2 > 0)
        assert(data.size % 2 == 0)

        val lunchDuring = TimeDuration(context.lunchStart, context.lunchEnd)

        val workList = ArrayList<TickDuration>()
        for (i in 0 until data.size / 2)
            workList.add(TickDuration(data[i * 2].tick, data[i * 2 + 1].tick))
        day.workTime = workList.flatMap { it.filter(lunchDuring) }
                .map { it.between() }
                .reduce { acc, duration -> acc.plus(duration) }
                .toMinutes()
                .toDouble()

        if (data.size > 2) {
            val list = ArrayList<TickDuration>()
            for (i in 1 until data.size / 2)
                list.add(TickDuration(data[i * 2 - 1].tick, data[i * 2].tick))
            val outList = list.flatMap { it.filter(lunchDuring) }
            day.outDuring.addAll(outList.filter { it.between().seconds > 600 })
            if (outList.isNotEmpty()) {
                day.outTimes = outList.filter { it.between().seconds > 600 }.size
                day.outDuration = outList.map { it.between() }
                        .reduce { acc, duration -> acc.plus(duration) }
                        .toMinutes()
                        .toDouble()
            } else println("empty ${day.date}")
        }
    }
}