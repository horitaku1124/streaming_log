package com.ict_start.kotlin.gui

import java.io.File
import java.util.concurrent.TimeUnit

class ReadFileThread(var filePath: String) : Thread() {
  private var updatedLines = mutableListOf<String>()
  var isReading = true
  private val InitLines = 5

  override fun run() {
    val lineBuffer = arrayOfNulls<String>(InitLines)
    var arrayAmount:Long = 0

    println("Started")
    isReading = true
    val br = File(filePath).inputStream().bufferedReader(Charsets.UTF_8)

    while(true) {
      val line: String = br.readLine() ?: break
      lineBuffer[(arrayAmount % InitLines).toInt()] = line
      arrayAmount++
    }
    synchronized(updatedLines) {
      if (arrayAmount < InitLines) {
        for (i in (0 until arrayAmount)) {
          val line = lineBuffer[i.toInt()]
          if (line != null) {
            updatedLines.add(line)
          }
        }
      } else {
        for (i in ((arrayAmount - InitLines) until arrayAmount)) {
          val index = (i % InitLines).toInt()
          val line = lineBuffer[index]
          if (line != null) {
            updatedLines.add(line)
          }
        }
      }
    }

    while(isReading) {
      val line: String? = br.readLine()
      if (line == null) {
        TimeUnit.MILLISECONDS.sleep(500);
      } else {
        synchronized(updatedLines) {
          updatedLines.add(line)
        }
      }
    }
    println("Finished")
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