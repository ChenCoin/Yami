package yami.model

import java.io.FileNotFoundException
import org.apache.poi.poifs.filesystem.NotOLE2FileException
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

class Reader(private val context: Context, private val callback: (Int, String) -> Unit) {

    private lateinit var thread: Thread

    fun read() {
        thread = Thread {
            try {
                val dataWorkbook = WorkbookFactory.create(context.dataXls)
                val peopleWorkbook = WorkbookFactory.create(context.peopleXls)
                if (!Thread.interrupted()) {
                    callback(10, "读取Excel文件")
                    val dataResults = readDataXls(dataWorkbook)
                    if (context.months.size > 1) throw Exception("文件里面有多个月份：${context.months}")
                    if (context.months.isEmpty()) throw Exception("找不到对应月份")
                    val staffResults = readPeopleXls(peopleWorkbook)
                    dataWorkbook.close()
                    peopleWorkbook.close()
                    callback(20, "Excel文件读取完成")
                    if (!Thread.interrupted()) Count(context, callback).count(dataResults, staffResults)
                }
            } catch (e: NotOLE2FileException) {
                callback(-1, "文件不是Excel文件(1003)")
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                callback(-1, "出错了，文件无法打开，可能被其它软件打开了(1004)")
            } catch (e: Exception) {
                e.printStackTrace()
                callback(-1, "出错了，" + e.message + "(1005)")
            }
            if (Thread.interrupted()) callback(-1, "取消成功")
        }
        thread.start()
    }

    fun cancel() {
    }

    private fun readDataXls(xls: Workbook): List<RawTick> {
        val numberOfSheets = xls.numberOfSheets
        if (numberOfSheets <= 0) {
            throw Exception("文件内容不符合要求(1001)")
        }
        val sheet = xls.getSheetAt(0)
        val rowNumbers = sheet.lastRowNum + 1
        if (rowNumbers < 1) {
            throw Exception("文件内容不符合要求(1002)")
        }
        val result = ArrayList<RawTick>()
        for (index in 1 until rowNumbers) {
            try {
                var error = false
                val row = sheet.getRow(index)

                val cell0 = row.getCell(0)
                val time = when (cell0.cellType) {
                    CellType.STRING -> {
                        val time = LocalDateTime.parse(cell0.toString(),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        context.months.add(YearMonth.of(time.year, time.month))
                        time
                    }
                    CellType.NUMERIC -> {
                        val time = cell0.dateCellValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        context.months.add(YearMonth.of(time.year, time.month))
                        time
                    }
                    else -> {
                        error = true
                        LocalDateTime.MIN
                    }
                }

                val cell9 = row.getCell(9)
                val name = if (cell9 != null && cell9.cellType === CellType.STRING) cell9.toString()
                else {
                    error = true
                    ""
                }

                val cell8 = row.getCell(8)
                val id = if (cell8 != null && cell8.cellType === CellType.STRING) cell8.toString()
                else {
                    error = true
                    ""
                }

                val cell1 = row.getCell(1)
                val pass = if (cell1 != null && cell1.cellType == CellType.STRING) {
                    val text = cell1.toString().toUpperCase()
                    when {
                        text.contains("IN") || text.contains("侧门") -> Pass.IN
                        text.contains("OUT") -> Pass.OUT
                        else -> Pass.UNKNOWN
                    }
                } else Pass.UNKNOWN
                if (pass == Pass.UNKNOWN) error = true

                result.add(RawTick(name, id, time, pass, error, index))
            } catch (e: Exception) {
                Logger.getLogger("yami").warning("raw xls error in line $index")
                continue
            }
        }
        return result
    }

    private fun readPeopleXls(xls: Workbook): List<RawStaff> {
        val numberOfSheets = xls.numberOfSheets
        if (numberOfSheets <= 0) {
            throw Exception("文件内容不符合要求(1001)")
        }
        val sheet = xls.getSheetAt(0)
        val rowNumbers = sheet.lastRowNum + 1
        if (rowNumbers < 1) {
            throw Exception("文件内容不符合要求(1002)")
        }
        val result = ArrayList<RawStaff>()
        for (index in 1 until rowNumbers) {
            try {
                val row = sheet.getRow(index);
                val cell0 = row.getCell(0);
                if (cell0 == null || cell0.cellType != CellType.STRING) {
                    continue;
                }
                val cell1 = row.getCell(1);
                if (cell1 == null || cell0.cellType != CellType.STRING) {
                    continue;
                }
                val cell2 = row.getCell(2);
                if (cell2 == null || cell0.cellType != CellType.STRING) {
                    continue;
                }
                result.add(RawStaff(cell0.toString(), cell1.toString(), cell2.toString()));
            } catch (e: Exception) {
                Logger.getLogger("yami").warning("raw xls error in line $index")
                continue
            }
        }
        return result
    }

}