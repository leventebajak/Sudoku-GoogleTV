@file:JvmName("BoardUtils")

package leventebajak.sudokugenerator.sudoku

import leventebajak.sudokugenerator.dlx.Matrix
import leventebajak.sudokugenerator.dlx.nextTrueIndex

/**
 * A 9x9 [Sudoku] board.
 *
 * @property cells The cells of the board.
 * @throws IllegalArgumentException If the board is not 9x9 or contains invalid values.
 * @see String.toBoard
 * @see Matrix.toBoard
 */
data class Board(private val cells: IntArray = IntArray(81) { EMPTY_CELL }) : Iterable<Int>,
    Cloneable {
    companion object {
        /**
         * The value of an empty cell.
         */
        const val EMPTY_CELL = 0

        /**
         * The set of valid numbers.
         */
        @JvmStatic
        val NUMBERS = (1..9).toSet() + EMPTY_CELL
    }

    init {
        require(cells.size == 81) { "Sudoku board must be 9x9" }
        require(cells.all { it in NUMBERS }) { "Sudoku board must contain only numbers in $NUMBERS" }
    }

    /**
     * Gets the value of the cell at the given index.
     *
     * @param row The row of the cell.
     * @param col The column of the cell.
     * @return The value of the cell at the given [row] and [column][col].
     */
    operator fun get(row: Int, col: Int): Int = get(index(row, col))

    /**
     * Sets the value of the cell at the given [row] and [column][col].
     *
     * @param row The row of the cell.
     * @param col The column of the cell.
     * @param value The value to set the cell to.
     */
    operator fun set(row: Int, col: Int, value: Int) = set(index(row, col), value)

    /**
     * Gets the value of the cell at the given index.
     *
     * @param index The index of the cell.
     * @return The value of the cell at the given [index].
     */
    operator fun get(index: Int) = cells[index]

    /**
     * Sets the value of the cell at the given [index].
     *
     * @param index The index of the cell.
     * @param value The value to set the cell to.
     * @throws IllegalArgumentException If the value is not in [NUMBERS].
     */
    operator fun set(index: Int, value: Int) {
        require(value in NUMBERS) { "Cell value must be in $NUMBERS" }
        cells[index] = value
    }

    /**
     * Creates a deep copy of the board.
     *
     * @return A copy of the board.
     */
    public override fun clone() = Board(cells.clone())

    /**
     * Returns an iterator over the cells of the board.
     *
     * @return An iterator over the cells of the board.
     */
    override fun iterator() = cells.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return cells.contentEquals((other as Board).cells)
    }

    override fun hashCode() = cells.contentHashCode()

    override fun toString(): String {
        val builder = StringBuilder("┏━━━━━━━━━━━┳━━━━━━━━━━━┳━━━━━━━━━━━┓\n")
        repeat(9) { row ->
            builder.append("┃")
            repeat(9) { col ->
                builder.append(
                    " ${
                        this[row, col].let {
                            if (it == EMPTY_CELL) " " else it
                        }
                    } ${if (col % 3 == 2) "┃" else "│"}"
                )
            }
            builder.append("\n")
            builder.append(
                when {
                    row == 8 -> "┗━━━━━━━━━━━┻━━━━━━━━━━━┻━━━━━━━━━━━┛\n"
                    row % 3 == 2 -> "┣━━━━━━━━━━━╋━━━━━━━━━━━╋━━━━━━━━━━━┫\n"
                    else -> "┃───┼───┼───┃───┼───┼───┃───┼───┼───┃\n"
                }
            )
        }
        return builder.toString()
    }
}

/**
 * Indexing logic for a 9x9 [Board].
 */
private fun index(row: Int, col: Int) = row * 9 + col

/**
 * Converts a multiline string to a sudoku [Board].
 *
 * @return The [Board] represented by the string.
 */
fun String.toBoard() = Board().also {
    val lines = this.trimIndent().lines()
    require(lines.size == 9) { "Sudoku board must be 9x9" }
    for ((row, line) in lines.withIndex()) {
        require(line.length == 9) { "Sudoku board must be 9x9" }
        for ((col, char) in line.withIndex()) {
            val value = char.toString().toIntOrNull()
            require(value in Board.NUMBERS) { "Sudoku board must contain only numbers in ${Board.NUMBERS}" }
            it[row, col] = value ?: Board.EMPTY_CELL
        }
    }
}

/**
 * Converts the [Matrix] to a sudoku [Board].
 *
 * @return The [Board] represented by the [Matrix].
 * @throws IllegalArgumentException If the matrix does not represent a Sudoku board.
 */
fun Matrix.toBoard() = Board().also {
    for (matrixRow in this) {
        val cellConstraint = matrixRow.nextTrueIndex(0)
        require(cellConstraint != -1 && cellConstraint < 81) { "The matrix does not represent a Sudoku board" }

        val row = cellConstraint / 9
        val col = cellConstraint % 9
        require(it[row, col] == Board.EMPTY_CELL) { "Multiple numbers in the same cell" }

        val rowConstraint = matrixRow.nextTrueIndex(81)
        require(rowConstraint != -1 && rowConstraint < 162) { "The matrix does not represent a Sudoku board" }

        val number = rowConstraint - 81 - row * 9 + 1

        it[row, col] = number
    }
}