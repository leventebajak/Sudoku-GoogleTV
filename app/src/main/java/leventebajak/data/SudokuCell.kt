package leventebajak.data

import leventebajak.sudokugenerator.sudoku.Board.Companion.EMPTY_CELL
import leventebajak.sudokugenerator.sudoku.Board.Companion.NUMBERS
import java.io.Serializable

/**
 * A cell of a Sudoku board.
 *
 * @constructor Creates a cell with the given clue and solution.
 *
 * @param clue The clue of the cell. If the clue is [EMPTY_CELL], the cell is editable.
 * @param solution The solution of the cell.
 * @throws IllegalArgumentException If the clue and solution do not match.
 */
data class SudokuCell(private val clue: Int, private val solution: Int) : Serializable {
    /**
     * The current value of the cell.
     */
    var value: Int = clue
        /**
         * Sets the value of the cell.
         *
         * @param newValue The new value of the cell.
         * @throws IllegalArgumentException If the value is not in [NUMBERS].
         * @throws IllegalStateException If the cell is not editable.
         */
        set(newValue) {
            require(newValue in NUMBERS) { "Cell value must be in $NUMBERS" }
            check(isEditable()) { "Cell is not editable" }
            field = newValue
        }

    init {
        require(clue == EMPTY_CELL || solution == clue) { "Clue and solution do not match" }
    }

    /**
     * Returns whether the cell is editable, meaning its [clue] was [EMPTY_CELL].
     *
     * @return Whether the cell is editable.
     */
    fun isEditable() = clue == EMPTY_CELL

    /**
     * Returns whether the cell is solved, meaning its [value] is equal to its [solution].
     *
     * @return Whether the cell is solved.
     */
    fun isSolved() = value == solution
}