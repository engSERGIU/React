package io.github.engsergiu.react

import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * File handler used for editing files.
 * @author Sergiu
 */
class FileHandler(
		private val folder: String, //the folder which contains the file
		private val fileName: String //the name of the file
) {
	// true if the folder path is vaild = false
	private var clearPath = false

	/**
	 * Check if external storage is available for read and write
	 *
	 * @see [Saving Files | Android Developers](https://developer.android.com/training/basics/data-storage/files.html)
	 */
	private val isExternalStorageWritable: Boolean
		get() = Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

	/**
	 * @param folder   folder containing app's files
	 * @param fileName name of the file
	 */
	init {
		makePath(folder)
	}

	/**
	 * Create the path for the generated file
	 */
	private fun makePath(folder: String) {
		val directory = File(Environment.getExternalStorageDirectory().toString() + "/" + folder)
		clearPath = if (directory.isDirectory) true else directory.mkdirs()
	}

	/**
	 * Write data to file in append mode
	 *
	 * @param content written content
	 */
	fun write(content: String) {
		if (isExternalStorageWritable && clearPath) {
			val file: File
			val outputStream: FileOutputStream
			val writeColumnNames: Boolean
			try {
				file = File(Environment.getExternalStorageDirectory(), "$folder/$fileName")
				writeColumnNames = !file.isFile
				outputStream = FileOutputStream(file, true)
				if (writeColumnNames) outputStream.write("\"date\",\"reaction time\",\"red screen duration\"\n".toByteArray())
				outputStream.write(content.toByteArray())
				outputStream.close()
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}
}