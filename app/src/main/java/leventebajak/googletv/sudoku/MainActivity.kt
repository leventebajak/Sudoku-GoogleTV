package leventebajak.googletv.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import leventebajak.data.BestTimes
import leventebajak.data.GameData
import leventebajak.data.save

import leventebajak.googletv.sudoku.ui.theme.SudokuTheme
import leventebajak.googletv.sudoku.ui.theme.Typography

import leventebajak.sudokugenerator.sudoku.Difficulty
import leventebajak.sudokugenerator.sudoku.Sudoku

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuTheme {
                SudokuApp()
            }
        }
    }
}

/**
 * Main entry point for the Sudoku app.
 */
@Composable
fun SudokuApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "menu") {
        composable("menu") { MenuScreen(navController) }
        composable("difficultySelection") { DifficultySelectionScreen(navController) }
        composable("generate/{difficulty}") { backStackEntry ->
            val difficultyString = backStackEntry.arguments?.getString("difficulty")!!
            val difficulty = Difficulty.valueOf(difficultyString)
            LoadingScreen(difficulty, navController)
        }
        composable("game") { GameScreen(navController) }
        composable("gameOver") { GameOverScreen(navController) }
    }
}

/**
 * Main menu screen. Allows the user to continue a saved game or start a new one.
 */
@Composable
fun MenuScreen(navController: NavHostController) {
    val focusRequester = remember { FocusRequester() }

    val context = LocalContext.current

    val canContinueGame = File(context.filesDir, GameData.saveFileName).exists()

    LaunchedEffect("menu") {
        focusRequester.requestFocus()
    }

    val activity = LocalContext.current as ComponentActivity

    BackHandler {
        activity.finish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RectangleShape,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "Sudoku Icon",
                    modifier = Modifier.size(250.dp)
                )
                Text(
                    text = "Sudoku",
                    style = Typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Button(
                onClick = {
                    navController.navigate("game")
                },
                enabled = canContinueGame,
                modifier = if (canContinueGame) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                }.padding(16.dp)
            ) {
                Text(text = "Continue Game", style = Typography.bodyLarge)
            }
            Button(
                onClick = { navController.navigate("difficultySelection") },
                modifier = if (canContinueGame) {
                    Modifier
                } else {
                    Modifier.focusRequester(focusRequester)
                }.padding(16.dp)
            ) {
                Text(text = "New Game", style = Typography.bodyLarge)
            }
        }
    }
}

/**
 * Difficulty selection screen. Allows the user to select the difficulty of the generated Sudoku.
 */
@Composable
fun DifficultySelectionScreen(navController: NavHostController) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Difficulty",
            style = Typography.displayLarge,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        for (difficulty in Difficulty.entries) {
            DifficultyOption(
                modifier = if (difficulty == Difficulty.entries[0]) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                },
                difficulty = difficulty.name,
                onSelectDifficulty = { selectedDifficulty ->
                    navController.navigate("generate/$selectedDifficulty")
                }
            )
        }
    }
}

/**
 * One of the difficulty options on the difficulty selection screen.
 */
@Composable
fun DifficultyOption(
    difficulty: String,
    onSelectDifficulty: (String) -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = { onSelectDifficulty(difficulty) },
        modifier = modifier
            .width(150.dp)
            .padding(8.dp)
    ) {
        Text(
            difficulty.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
            textAlign = TextAlign.Center,
            style = Typography.bodyLarge,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Loading screen that generates a new Sudoku in the background.
 */
@Composable
fun LoadingScreen(difficulty: Difficulty, navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current

    LaunchedEffect(difficulty) {
        coroutineScope.launch(Dispatchers.IO) {
            GameData(Sudoku.generate(difficulty), difficulty).save(context)
            withContext(Dispatchers.Main) {
                isLoading = false
                navController.navigate("game") {
                    popUpTo("menu") { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                color = Color.White
            )
        }
    }
}

/**
 * Game over screen that shows the user's time and allows them to return to the main menu.
 */
@Composable
fun GameOverScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CONGRATULATIONS!",
            style = Typography.displayLarge,
            color = Color.White
        )

        val context = LocalContext.current

        /// Load the game data from the file
        val gameData by remember { mutableStateOf(GameData.load(context)) }

        var deletedGameData by remember { mutableStateOf(false) }

        if (gameData != null) {
            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = "You solved the ${gameData!!.difficulty} sudoku in ${
                    String.format("%02d", gameData!!.elapsedSeconds / 60)
                }:${
                    String.format("%02d", gameData!!.elapsedSeconds % 60)
                }!",
                style = Typography.bodyLarge,
                color = Color.White
            )

            /// Load the best times from the file
            val bestTimes = BestTimes.load(context) ?: BestTimes()
            val bestTime by remember { mutableStateOf(bestTimes.getBestTime(gameData!!.difficulty)) }

            val text by remember {
                mutableStateOf(
                    if (bestTime == null || gameData!!.elapsedSeconds < bestTime!!) {
                        bestTimes.setBestTime(gameData!!.difficulty, gameData!!.elapsedSeconds)
                        bestTimes.save(context)
                        "New personal best!"
                    } else {
                        "Personal best: ${
                            String.format("%02d", bestTime!! / 60)
                        }:${
                            String.format("%02d", bestTime!! % 60)
                        }"
                    }
                )
            }

            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = text,
                style = Typography.bodyLarge,
                color = Color.White
            )
        }

        val focusRequester = remember { FocusRequester() }

        Button(
            onClick = {
                navController.navigate("menu")
            },
            modifier = Modifier
                .padding(top = 20.dp)
                .focusRequester(focusRequester)
        ) {
            GameData.delete(context)
            deletedGameData = true
            Text(text = "Back to Menu", style = Typography.bodyLarge)
        }

        BackHandler {
            navController.navigate("menu") {
                launchSingleTop = true
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        DisposableEffect(Unit) {
            onDispose {
                if (!deletedGameData) {
                    GameData.delete(context)
                }
            }
        }
    }
}