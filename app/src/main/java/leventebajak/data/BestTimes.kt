package leventebajak.data

import android.content.Context
import com.google.gson.Gson
import java.io.FileOutputStream
import java.io.Serializable

/**
 * A class to store the best times for each difficulty level.
 * @param bestTimes A HashMap to store the best times for each difficulty level.
 */
data class BestTimes(private val bestTimes: HashMap<String, Int> = HashMap()) : Serializable {
    /**
     * Sets the best [time] for a given [difficulty] level.
     */
    fun setBestTime(difficulty: String, time: Int) {
        bestTimes[difficulty] = time
    }

    /**
     * Gets the best time for a given [difficulty] level.
     */
    fun getBestTime(difficulty: String): Int? {
        return bestTimes[difficulty]
    }

    companion object {
        /**
         * The default file name to save the best times.
         */
        @JvmStatic
        val saveFileName = "bestTimes.json"

        /**
         * Loads the best times from a file.
         * @param context The context to load the file from.
         * @param fileName The name of the file to load the best times from.
         * @return The [BestTimes] loaded from the file or null if the file does not exist.
         */
        @JvmStatic
        fun load(context: Context, fileName: String = saveFileName): BestTimes? = runCatching {
            val fileInputStream = context.openFileInput(fileName)
            val bytes = ByteArray(fileInputStream.available())
            fileInputStream.read(bytes)
            Gson().fromJson(String(bytes), BestTimes::class.java)
        }.getOrNull()
    }
}

/**
 * Saves the best times to a file.
 * @param context The context to save the best times to.
 * @param fileName The name of the file to save the best times to.
 * @see BestTimes.saveFileName
 */
fun BestTimes.save(context: Context, fileName: String = BestTimes.saveFileName) {
    val fileOutputStream: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fileOutputStream.write(Gson().toJson(this).toByteArray())
    fileOutputStream.close()
}