package xyz.cyan.db

import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.*

abstract class DerbyHelper {

    abstract fun onCreate(statement: Statement)

    abstract fun onUpgrade()

    private var initial = false

    fun init() {
        Thread {
            post {
                try {
                    onCreate(it)
                } catch (e: Exception) {
                    val tableExist = e.cause.toString().startsWith("ERROR X0Y32")
                    if (tableExist) println("table is already exist.")
                    else e.printStackTrace()
                }
            }
            initial = true
        }.start()
    }

    fun getWritableDatabase() {

    }

    protected fun execSQL(sql: String) {
        if (!initial) {
            println("initial database failed")
            return
        }
        post { it.execute(sql) }
    }

    protected fun rawQuery(sql: String, callback: (ResultSet) -> Unit) {
        if (!initial) {
            println("initial database failed")
            return
        }
        post { callback(it.executeQuery(sql)) }
    }

    private fun post(action: (Statement) -> Unit) {
        val userName = "root"
        val password = "123456"
        val url = "jdbc:derby:database;create=true"

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance()
        val props = Properties()
        props["user"] = userName
        props["password"] = password
        DriverManager.getConnection(url, props).run {
            autoCommit = false
            createStatement().also {
                action(it)
                it.close()
            }
            commit()
            close()
        }
    }

}