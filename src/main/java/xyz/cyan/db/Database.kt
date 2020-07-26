package xyz.cyan.db

import java.sql.Statement

class Database : DerbyHelper() {

    override fun onCreate(statement: Statement) {
        statement.execute("create table hello(name varchar(40), score int)")
    }

    override fun onUpgrade() {

    }
}