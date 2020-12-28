package xyz.cyan.db

import java.sql.ResultSet
import java.sql.Statement

class Database : DerbyHelper() {

    override fun onCreate(statement: Statement) {
        statement.execute("create table hello(name varchar(40), score int)")
    }

    override fun onUpgrade() {

    }

    fun add() {
        execSQL("insert into hello values('Ruth Cao', 86)")
    }

    fun delete() {

    }

    fun query() {
        rawQuery("SELECT name, score FROM hello ORDER BY score") {
            while (it.next()) {
                val builder = StringBuilder(it.getString(1))
                builder.append("\t")
                builder.append(it.getInt(2))
                println(builder.toString())
            }
        }

    }
}