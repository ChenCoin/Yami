package yami.model

import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import java.io.FileOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

class Writer(private val context: Context, private val callback: (Int, String) -> Unit) {

    fun outDetailXls(data: List<RawPerson>): Boolean {
        try {
            val file = context.outDetailXls!!
            if (file.exists()) {
                file.delete();
            }

            val wb = HSSFWorkbook();
            val fs = FileOutputStream(file)
            val style = style(wb)

            val redFont = wb.createFont().apply { color = IndexedColors.RED.index }

            val titles = arrayOf("时间", "姓名", "项目组", "是否异常", "工作时间(H)", "工作时间(Min)",
                    "是否满8小时", "是否跨天", "外出超过10min次数", "外出总时长", "打卡记录")
            val titlesOfT1 = arrayOf("时间", "姓名", "项目组", "是否异常", "工作时间(H)",
                    "工作时间(Min)", "是否满12小时", "班次", "外出次数", "外出总时长", "打卡记录")
            data.groupBy { it.project.toUpperCase().contains("T1") }.toSortedMap().forEach { (isT1, list) ->
                if (!isT1) {
                    val sheet = wb.createSheet("非T1")
                    for (i in 0 until titles.size - 1) sheet.setDefaultColumnStyle(i, style)

                    val rowTitle = sheet.createRow(0)
                    for (i in titles.indices)
                        rowTitle.createCell(i).setCellValue(titles[i])

                    var rowIndex = 0
                    for (item in list) {
                        for (day in item.workDays) {
                            val row = sheet.createRow(++rowIndex)
                            row.createCell(0).setCellValue(
                                    day.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            row.createCell(1).setCellValue(item.name)
                            row.createCell(2).setCellValue(item.project)
                            row.createCell(3).setCellValue(if (day.workTime >= 0) "记录完整" else "异常")
                            if (day.workTime >= 0) {
                                val workTimeHour = BigDecimal(day.workTime / 60)
                                        .setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                                row.createCell(4).setCellValue(workTimeHour)
                                row.createCell(5).setCellValue(BigDecimal(day.workTime)
                                        .setScale(1, BigDecimal.ROUND_HALF_UP).toDouble())
                                row.createCell(6).setCellValue(if (workTimeHour >= 8) "满8小时" else "不满8小时")
                            }
                            val overDay = if (day.list.first().tick.toLocalDate() == day.list.last().tick.toLocalDate()) "没有跨天"
                            else "跨天"
                            row.createCell(7).setCellValue(overDay)
                            if (day.outTimes != 0) row.createCell(8).setCellValue(day.outTimes.toDouble())
                            if (day.outDuration != 0.0) row.createCell(9).setCellValue(day.outDuration)
                            val richText = HSSFRichTextString(day.list.joinToString(prefix = "[", postfix = "]") {
                                "[${it.type}]${it.tick.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
                            })
                            for (i in 0 until day.list.size) {
                                val tick = day.list[i]
                                if (day.outDuring.any { it.exist(tick.tick) }) {
                                    val start = day.list.subList(0, i).joinToString(prefix = "[") {
                                        "[${it.type}]${it.tick.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
                                    }.length + 1
                                    val end = start + ("[${tick.type}]" +
                                            tick.tick.format(DateTimeFormatter.ofPattern("HH:mm:ss"))).length + 1
                                    richText.applyFont(start, end, redFont)
                                }
                            }
                            row.createCell(10).setCellValue(richText)
                        }
                    }

                    for (i in 0 until titles.size - 1) sheet.autoSizeColumn(i)
                    for (i in 0 until titles.size - 1) sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024)
                } else {
                    val sheet = wb.createSheet("T1")
                    for (i in 0 until titlesOfT1.size - 1) sheet.setDefaultColumnStyle(i, style)

                    val rowTitle = sheet.createRow(0);
                    for (i in titlesOfT1.indices)
                        rowTitle.createCell(i).setCellValue(titlesOfT1[i])

                    var rowIndex = 0
                    for (item in list) {
                        for (day in item.workDays) {
                            val row = sheet.createRow(++rowIndex)
                            row.createCell(0).setCellValue(
                                    day.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            row.createCell(1).setCellValue(item.name)
                            row.createCell(2).setCellValue(item.project)
                            row.createCell(3).setCellValue(if (day.workTime >= 0) "记录完整" else "异常")
                            if (day.workTime >= 0) {
                                val workTimeHour = BigDecimal(day.workTime / 60)
                                        .setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                                row.createCell(4).setCellValue(workTimeHour)
                                row.createCell(5).setCellValue(BigDecimal(day.workTime)
                                        .setScale(1, BigDecimal.ROUND_HALF_UP).toDouble())
                                row.createCell(6).setCellValue(if (workTimeHour >= 12) "满12小时" else "不满12小时")
                            }
                            val workType = when (day.type) {
                                WorkType.DAY -> "白班"
                                WorkType.NIGHT -> "夜班"
                                else -> "白班"
                            }
                            row.createCell(7).setCellValue(workType)
                            if (day.outTimes != 0) row.createCell(8).setCellValue(day.outTimes.toDouble())
                            if (day.outDuration != 0.0) row.createCell(9).setCellValue(day.outDuration)
                            row.createCell(10).setCellValue(day.list.joinToString(prefix = "[", postfix = "]") {
                                "[${it.type}]${it.tick.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}"
                            })
                        }
                    }

                    for (i in 0 until titlesOfT1.size - 1) sheet.autoSizeColumn(i)
                    for (i in 0 until titlesOfT1.size - 1) sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024)
                }
            }

            wb.write(fs);
            fs.close();
            wb.close();
        } catch (e: FileNotFoundException) {
            callback(-1, e.message ?: "找不到文件")
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            callback(-1, e.message ?: "文件写入出错")
            e.printStackTrace()
            return false
        }
        return true
    }

    fun outMonthXls(data: List<RawPerson>) {
        try {
            val file = context.outMonthXls!!
            if (file.exists()) {
                file.delete();
            }

            val wb = HSSFWorkbook();
            val fs = FileOutputStream(file)

            val cellStyle = style(wb)
            val yellowCellStyle = yellowStyle(wb);

            val titleText = arrayOf("员工编号", "员工名称", "项目名称")
            val titles = ArrayList<String>()
            titles.addAll(titleText.asList())
            // 月份只允许有一个
            val month = context.months.first()

            data.groupBy { it.project }.toSortedMap().forEach { (project, list) ->
                val sheet = wb.createSheet(project)

                val row0 = sheet.createRow(0)
                row0.createCell(0).run {
                    setCellValue("导入月份")
                    setCellStyle(cellStyle)
                }
                row0.createCell(1).run {
                    setCellStyle(cellStyle)
                    setCellValue(month.format(DateTimeFormatter.ofPattern("yyyy/MM")))
                }

                val rowTitle = sheet.createRow(1)
                for (i in titles.indices) rowTitle.createCell(i).run {
                    setCellStyle(cellStyle)
                    setCellValue(titles[i])
                }
                for (i in 0 until month.lengthOfMonth()) rowTitle.createCell(i + 3).run {
                    setCellStyle(cellStyle)
                    cellType = CellType.NUMERIC
                    setCellValue((i + 1).toDouble())
                }

                var rowIndex = 2
                for (item in list) {
                    val row = sheet.createRow(rowIndex++)
                    row.createCell(0).run {
                        setCellStyle(cellStyle)
                        setCellValue(item.id)
                    }
                    row.createCell(1).run {
                        setCellStyle(cellStyle)
                        setCellValue(item.name)
                    }
                    row.createCell(2).run {
                        setCellStyle(cellStyle)
                        setCellValue(project)
                    }
                    for (day in item.workDays) {
                        if (day.workTime >= 0) {
                            val cell = row.createCell(day.date.dayOfMonth + 2)
                            cell.cellType = CellType.NUMERIC
                            val workTime = BigDecimal(day.workTime / 60)
                                    .setScale(1, BigDecimal.ROUND_HALF_UP).toDouble()
                            cell.setCellValue(workTime)
                            if (workTime < 8 && !project.toUpperCase().contains("T1"))
                                cell.setCellStyle(yellowCellStyle)
                            else cell.setCellStyle(cellStyle)
                        }
                    }
                }

                for (i in 0 until titleText.size) sheet.autoSizeColumn(i)
                for (i in 0 until titleText.size) sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024)
            }

            wb.write(fs);
            fs.close();
            wb.close();
        } catch (e: FileNotFoundException) {
            callback(-1, e.message ?: "找不到文件")
            e.printStackTrace()
            return
        } catch (e: IOException) {
            callback(-1, e.message ?: "文件写入出错")
            e.printStackTrace()
            return
        }
        callback(100, "写入考勤工时数据计算结果成功")
        callback(100, "考勤数据统计完成")
        callback(100, "请检查考勤结果：${context.outputDir.canonicalPath}")
    }

    private fun yellowStyle(wb: HSSFWorkbook): CellStyle {
        val style = wb.createCellStyle()
        style.alignment = HorizontalAlignment.CENTER
        style.verticalAlignment = VerticalAlignment.CENTER
        style.wrapText = true

        style.fillForegroundColor = IndexedColors.YELLOW.index
        style.fillPattern = FillPatternType.SOLID_FOREGROUND

        style.borderLeft = BorderStyle.THIN
        style.borderTop = BorderStyle.THIN
        style.borderRight = BorderStyle.THIN
        style.borderBottom = BorderStyle.THIN
        style.leftBorderColor = IndexedColors.GREY_25_PERCENT.index
        style.topBorderColor = IndexedColors.GREY_25_PERCENT.index
        style.rightBorderColor = IndexedColors.GREY_25_PERCENT.index
        style.bottomBorderColor = IndexedColors.GREY_25_PERCENT.index
//        val font = wb.createFont()
//        font.color = IndexedColors.YELLOW.index
//        cellStyle.setFont(font)
        return style
    }

    private fun style(wb: HSSFWorkbook): CellStyle {
        val cellStyle = wb.createCellStyle()
        cellStyle.alignment = HorizontalAlignment.CENTER
        cellStyle.verticalAlignment = VerticalAlignment.CENTER
        cellStyle.wrapText = true
        return cellStyle
    }

}