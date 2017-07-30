package gui

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Thread
import java.util.concurrent.TimeUnit

class ReadFileThread(var filePath: String) : Thread() {
    private var updatedLines = mutableListOf<String>()

    override fun run() {
        val br = BufferedReader(FileReader(File(filePath)))
        while(true) {
            val str: String? = br.readLine()
            if (str == null) {
                TimeUnit.MILLISECONDS.sleep(500);
            } else {
                synchronized(updatedLines) {
                    updatedLines.add(str)
                }
            }
        }
    }

    fun hasUpdate(): Boolean{
        return updatedLines.size > 0
    }

    fun getUpdated(): MutableList<String> {
        val returnList = mutableListOf<String>()
        synchronized(updatedLines) {
            returnList.addAll(updatedLines)
            updatedLines.clear()
        }
        return returnList
    }
}