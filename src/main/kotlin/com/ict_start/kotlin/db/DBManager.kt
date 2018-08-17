package com.ict_start.kotlin.db

import java.sql.Connection
import java.sql.SQLException
import java.sql.DriverManager

class DBManager(internal var file: String) {
    @Throws(ClassNotFoundException::class, SQLException::class)
    fun createConnection(): Connection? {
        Class.forName("org.sqlite.JDBC")
        return DriverManager.getConnection("jdbc:sqlite:$file")
    }
}