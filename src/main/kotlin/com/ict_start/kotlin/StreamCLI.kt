package com.ict_start.kotlin


import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

/**
 * Main logic
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Argument error");
        System.exit(1);
    }
    val file_name = args[0]
    val br = BufferedReader(FileReader(File(file_name)))
    while(true) {
        val str: String? = br.readLine()
        if (str == null) {
            TimeUnit.MILLISECONDS.sleep(500);
        } else {
            println(str)
        }
    }
}