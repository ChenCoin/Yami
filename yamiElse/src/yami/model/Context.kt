package yami.model

import java.io.File
import java.time.LocalTime
import java.time.YearMonth

class Context {

    var lunchStart = LocalTime.of(11, 30)

    var lunchEnd = LocalTime.of(12, 30)

    var months: HashSet<YearMonth> = HashSet()

    var dataXls: File? = null

    var peopleXls: File? = null

    var outDetailXls: File? = File("./out/考勤计算结果.xls")

    var outMonthXls: File? = File("./out/考勤工时数据计算结果.xls")

    val outputDir = File("./out")
}