package leventebajak.data

import android.content.Context
import com.google.gson.Gson
import leventebajak.sudokugenerator.sudoku.Board.Companion.NUMBERS
import leventebajak.sudokugenerator.sudoku.Difficulty
import leventebajak.sudokugenerator.sudoku.Sudoku
import java.io.FileOutputStream

/**
 * Represents the state of a Sudoku game.
 *
 * @constructor Creates a new Sudoku game state from the given cells.
 *
 * @property cells The cells of the Sudoku game.
 * @property elapsedSeconds The number of elapsed seconds since the start of the game.
 */
class GameData(private val cells: Array<Array<SudokuCell>>, difficulty: Difficulty) {
    var elapsedSeconds = 0
        get
        set (value) {
            require(value >= field) { "The elapsed time cannot be decreased" }
            field = value
        }

    val difficulty = difficulty.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }

    init {
        require(cells.size == 9) { "Sudoku must have 9 rows" }
        require(cells.all { row -> row.size == 9 }) { "Sudoku must have 9 columns" }
        require(cells.all { row -> row.all { cell -> cell.value in NUMBERS } }) { "Cell values must be in $NUMBERS" }
    }

    /**
     * Creates a new Sudoku game state from the given [Sudoku].
     */
    constructor(sudoku: Sudoku, difficulty: Difficulty) : this(
        Array(9) { row ->
            Array(9) { col ->
                SudokuCell(sudoku.clues[row, col], sudoku.solutions[0][row, col])
            }
        },
        difficulty
    ) {
        require(sudoku.solutions.size == 1) { "Sudoku must have exactly one solution" }
    }

    /**
     * Returns the cell at the given [row] and [column].
     */
    operator fun get(row: Int, column: Int) = cells[row][column]

    /**
     * Sets the cell at the given [row] and [column] to the given [value]. The cell must be editable.
     *
     * @throws IllegalArgumentException if the given [value] is not in [NUMBERS].
     */
    operator fun set(row: Int, column: Int, value: Int) {
        require(value in NUMBERS) { "Value must be in $NUMBERS" }
        cells[row][column].value = value
    }

    /**
     * Returns whether all cells are solved.
     */
    fun solved() = cells.all { row -> row.all { cell -> cell.isSolved() } }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameData

        if (!cells.contentDeepEquals(other.cells)) return false
        if (difficulty != other.difficulty) return false
        if (elapsedSeconds != other.elapsedSeconds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cells.contentDeepHashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + elapsedSeconds
        return result
    }

    companion object {
        @JvmStatic
        val saveFileName = "gameData.json"

        /**
         * Loads a [GameData] from the given [fileName].
         *
         * @return the loaded [GameData] or `null` if the file could not be loaded.
         * @see [GameData.save]
         */
        @JvmStatic
        fun load(context: Context, fileName: String = saveFileName): GameData? = runCatching {
            val fileInputStream = context.openFileInput(fileName)
            val bytes = ByteArray(fileInputStream.available())
            fileInputStream.read(bytes)
            Gson().fromJson(String(bytes), GameData::class.java)
        }.getOrNull()

        @JvmStatic
        fun delete(context: Context, fileName: String = saveFileName) {
            context.deleteFile(fileName)
        }
    }
}

/**
 * Saves this [GameData] to the given [fileName].
 * @see [GameData.load]
 */
fun GameData.save(context: Context, fileName: String = GameData.saveFileName) {
    val fileOutputStream: FileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
    fileOutputStream.write(Gson().toJson(this).toByteArray())
    fileOutputStream.close()
}