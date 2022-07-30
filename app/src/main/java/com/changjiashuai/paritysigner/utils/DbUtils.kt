package com.changjiashuai.paritysigner.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/18 22:01.
 */
object DbUtils {

    // Data storage locations
    var dbName: String = ""
    private lateinit var context: Context

    fun initDb(context: Context) {
        this.context = context
        dbName = context.applicationContext.filesDir.toString() + "/Database"
    }

    /**
     * Util to copy Assets to data dir; only used in onBoard().
     */
    fun copyAsset(path: String) {
        val contents = context.assets.list("Database$path")
        if (contents == null || contents.isEmpty()) {
            copyFileAsset(path)
        } else {
            File(dbName, path).mkdirs()
            for (entry in contents) copyAsset("$path/$entry")
        }
    }

    /**
     * Util to remove directory
     */
    fun deleteDir(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            val listFiles = fileOrDirectory.listFiles()
            if (!listFiles.isNullOrEmpty()) {
                for (child in listFiles) deleteDir(child)
            }
        }
        fileOrDirectory.delete()
    }

    /**
     * Util to copy single Assets file
     */
    private fun copyFileAsset(path: String) {
        val file = File(dbName, path)
        file.createNewFile()
        val input = context.assets.open("Database$path")
        val output = FileOutputStream(file)
        val buffer = ByteArray(1024)
        var read = input.read(buffer)
        while (read != -1) {
            output.write(buffer, 0, read)
            read = input.read(buffer)
        }
        output.close()
        input.close()
    }
}