package leventebajak.googletv.sudoku

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.tv.material3.Text
import leventebajak.data.GameData
import leventebajak.data.save
import leventebajak.googletv.sudoku.ui.theme.*
import java.io.File
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The main screen of the game.
 * Contains the Sudoku grid, the number list, and the timer.
 * @param navController The navigation controller to navigate between screens.
 */
@Composable
fun GameScreen(navController: NavController) {
    val context = LocalContext.current

    val data by remember { mutableStateOf(GameData.load(context)) }

    if (data == null) {
        File(GameData.saveFileName).delete()
        AlertDialog(
            onDismissRequest = {
                navController.navigate("menu") {
                    launchSingleTop = true
                }
            },
            title = {
                Text(text = "Error loading game data")
            },
            text = {
                Text(text = "The game data could not be loaded.")
            },
            confirmButton = {
                Button(onClick = {
                    navController.navigate("menu") {
                        launchSingleTop = true
                    }
                }) {
                    Text("Back to menu")
                }
            }
        )
        return
    }

    val gameData = data!!

    var focusOnSidebar by remember { mutableStateOf(true) }
    var selectedNumber by remember { mutableIntStateOf(1) }
    var selectedCell by remember { mutableStateOf(Pair(0, 0)) }

    BackHandler(onBack = {
        if (focusOnSidebar) {
            navController.navigate("menu") {
                launchSingleTop = true
            }
        } else {
            focusOnSidebar = true
        }
    })

    val timerViewModel: TimerViewModel = viewModel(factory = TimerViewModelFactory(gameData.elapsedSeconds))
    var timerRunning by remember { mutableStateOf(true) }
    var timerText by remember { mutableStateOf("00:00") }
    val elapsedSeconds by timerViewModel.elapsedSeconds.collectAsState()

    val saveAndReset = {
        timerRunning = false
        gameData.elapsedSeconds = elapsedSeconds
        gameData.save(context)
        timerViewModel.resetElapsedSeconds(0)
    }

    DisposableEffect(Unit) {
        onDispose {
            if (timerRunning)
                saveAndReset()
        }
    }


    LaunchedEffect(Unit) {
        while (timerRunning) {
            delay(1000L)
            timerViewModel.incrementElapsedSeconds()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.Center
        ) {
            NumberList(
                focusOnSidebar,
                selectedNumber,
                onFocus = { number ->
                    selectedNumber = number
                    focusOnSidebar = true
                },
                onClick = {
                    focusOnSidebar = false
                }
            )
        }
        Box(modifier = Modifier.align(Alignment.Center)) {
            SudokuGrid(gameData, focusOnSidebar, selectedNumber, selectedCell, navController, onGameOver = saveAndReset,
                onFocus = { row, col ->
                    focusOnSidebar = false
                    selectedCell = Pair(row, col)
                }
            )
        }
        Box(
            modifier = Modifier
                .width(250.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = gameData.difficulty,
                    style = Typography.displayMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )

                timerText = "%02d:%02d".format(elapsedSeconds / 60, elapsedSeconds % 60)
                Text(
                    text = timerText,
                    style = Typography.displayMedium,
                    fontFamily = redditMonoFamily,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

/**
 * The list of numbers to select from.
 * @param focusOnSidebar Whether the focus is on the sidebar.
 * @param selectedNumber The currently selected number.
 * @param onFocus The callback to call when a number is focused.
 * @param onClick The callback to call when a number is clicked.
 * @see NumberCell
 */
@Composable
fun NumberList(focusOnSidebar: Boolean, selectedNumber: Int, onFocus: (Int) -> Unit, onClick: () -> Unit) {
    Row {
        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 1..5) {
                Row(
                    modifier = Modifier
                        .width(66.dp)
                        .height(66.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    NumberCell(
                        focusOnSidebar,
                        value = i,
                        selectedNumber,
                        onFocus = { onFocus(i) },
                        onClick = onClick
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 6..9) {
                Row(
                    modifier = Modifier
                        .width(66.dp)
                        .height(66.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    NumberCell(
                        focusOnSidebar,
                        value = i,
                        selectedNumber,
                        onFocus = { onFocus(i) },
                        onClick = onClick
                    )
                }
            }
            Row(
                modifier = Modifier
                    .width(66.dp)
                    .height(66.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                NumberCell(focusOnSidebar, value = 0, selectedNumber, onFocus = { onFocus(0) }, onClick = onClick)
            }
        }
    }
}

/**
 * The Sudoku grid.
 * @param gameData The game data.
 * @param focusOnSidebar Whether the focus is on the sidebar.
 * @param selectedNumber The currently selected number.
 * @param selectedCell The currently selected cell.
 * @param navController The navigation controller to navigate between screens.
 * @param onGameOver The callback to call when the game is over.
 * @param onFocus The callback to call when a cell is focused.
 * @see SudokuBlock
 * @see SudokuCell
 */
@Composable
fun SudokuGrid(
    gameData: GameData,
    focusOnSidebar: Boolean,
    selectedNumber: Int,
    selectedCell: Pair<Int, Int>,
    navController: NavController,
    onGameOver: () -> Unit,
    onFocus: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .border(3.dp, BorderColor)
            .padding(3.dp)
    ) {
        for (blockRow in 0 until 3) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                for (blockCol in 0 until 3) {
                    SudokuBlock(
                        gameData,
                        blockRow,
                        blockCol,
                        selectedNumber,
                        selectedCell,
                        focusOnSidebar,
                        navController,
                        onGameOver,
                        onFocus
                    )
                }
            }
        }
    }
}

/**
 * A block of the Sudoku grid.
 * @param gameData The game data.
 * @param blockRow The row of the block.
 * @param blockCol The column of the block.
 * @param selectedNumber The currently selected number.
 * @param selectedCell The currently selected cell.
 * @param focusOnSidebar Whether the focus is on the sidebar.
 * @param navController The navigation controller to navigate between screens.
 * @param onGameOver The callback to call when the game is over.
 * @param onFocus The callback to call when a cell is focused.
 */
@Composable
fun SudokuBlock(
    gameData: GameData,
    blockRow: Int,
    blockCol: Int,
    selectedNumber: Int,
    selectedCell: Pair<Int, Int>,
    focusOnSidebar: Boolean,
    navController: NavController,
    onGameOver: () -> Unit,
    onFocus: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .border(1.5.dp, BorderColor)
            .padding(1.dp)
    ) {
        for (cellRow in 0 until 3) {
            Row {
                for (cellCol in 0 until 3) {
                    SudokuCell(
                        gameData,
                        blockRow * 3 + cellRow,
                        blockCol * 3 + cellCol,
                        selectedNumber,
                        selectedCell,
                        focusOnSidebar,
                        navController,
                        onGameOver,
                        onFocus
                    )
                }
            }
        }
    }
}

/**
 * A cell of the Sudoku grid.
 * @param gameData The game data.
 * @param cellRow The row of the cell.
 * @param cellCol The column of the cell.
 * @param selectedNumber The currently selected number.
 * @param selectedCell The currently selected cell.
 * @param focusOnSidebar Whether the focus is on the sidebar.
 * @param navController The navigation controller to navigate between screens.
 * @param onGameOver The callback to call when the game is over.
 * @param onFocus The callback to call when a cell is focused.
 */
@Composable
fun SudokuCell(
    gameData: GameData,
    cellRow: Int,
    cellCol: Int,
    selectedNumber: Int,
    selectedCell: Pair<Int, Int>,
    focusOnSidebar: Boolean,
    navController: NavController,
    onGameOver: () -> Unit,
    onFocus: (Int, Int) -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    var text by remember {
        mutableStateOf(
            gameData[cellRow, cellCol].value.toString().let { if (it == "0") " " else it })
    }
    var selected = cellRow == selectedCell.first && cellCol == selectedCell.second

    TextButton(
        onClick = {
            if (gameData[cellRow, cellCol].isEditable()) {
                text = if (selectedNumber == 0) " " else selectedNumber.toString()
                gameData[cellRow, cellCol].value = selectedNumber
                if (gameData.solved()) {
                    onGameOver()
                    navController.navigate("gameOver") {
                        launchSingleTop = true
                    }
                }
            }
        },
        modifier = Modifier
            .size(48.dp)
            .background(if (selected) (if (focusOnSidebar) DisabledBackgroundColor else HighlightColor) else BackgroundColor)
            .border(0.5.dp, BorderColor)
            .padding(0.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                selected = focusState.isFocused
                if (selected) onFocus(cellRow, cellCol)
            },
        shape = RoundedCornerShape(0.dp)
    ) {
        Text(
            text = text,
            fontSize = if (selected) 25.sp else 20.sp,
            fontFamily = redditMonoFamily,
            color = if (gameData[cellRow, cellCol].isEditable()) Color.White else DisabledTextColor,
            textAlign = TextAlign.Center,
        )
    }

    LaunchedEffect(focusOnSidebar) {
        if (!focusOnSidebar && selected) {
            focusRequester.requestFocus()
        }
    }
}

/**
 * A cell in the number list.
 * @param focusOnSidebar Whether the focus is on the sidebar.
 * @param value The value of the cell.
 * @param selectedNumber The currently selected number.
 * @param onFocus The callback to call when the cell is focused.
 * @param onClick The callback to call when the cell is clicked.
 */
@Composable
fun NumberCell(
    focusOnSidebar: Boolean,
    value: Int,
    selectedNumber: Int,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    val selected = value == selectedNumber

    val focusRequester = remember { FocusRequester() }

    TextButton(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .size(if (selected) 60.dp else 49.25f.dp)
            .background(if (selected) (if (focusOnSidebar) HighlightColor else DisabledBackgroundColor) else BackgroundColor)
            .border(2.dp, BorderColor)
            .padding(0.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) onFocus()
            },
        shape = RoundedCornerShape(0.dp)
    ) {
        Text(
            text = if (value == 0) "X" else value.toString(),
            fontSize = if (selected) 25.sp else 20.sp,
            fontFamily = redditMonoFamily,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }

    LaunchedEffect(focusOnSidebar) {
        if (focusOnSidebar && selected) {
            focusRequester.requestFocus()
        }
    }
}

/**
 * A view model for the timer.
 * @param initialElapsedSeconds The initial elapsed seconds.
 */
class TimerViewModel(initialElapsedSeconds: Int) : ViewModel() {
    private val _elapsedSeconds = MutableStateFlow(initialElapsedSeconds)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    fun incrementElapsedSeconds() {
        _elapsedSeconds.update { it + 1 }
    }

    fun resetElapsedSeconds(initialElapsedSeconds: Int) {
        _elapsedSeconds.value = initialElapsedSeconds
    }
}

/**
 * A factory for the timer view model.
 * @param initialElapsedSeconds The initial elapsed seconds.
 */
class TimerViewModelFactory(private val initialElapsedSeconds: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(initialElapsedSeconds) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}