package leventebajak.googletv.sudoku.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Typography
import leventebajak.googletv.sudoku.R
import androidx.compose.ui.text.font.Font

val merriweatherFamily = FontFamily(
    Font(R.font.merriweather_light, FontWeight.Light),
    Font(R.font.merriweather_regular, FontWeight.Normal),
    Font(R.font.merriweather_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.merriweather_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.merriweather_bold, FontWeight.Bold)
)

val redditMonoFamily = FontFamily(
    Font(R.font.redditmono_medium)
)

// Set of Material typography styles to start with
@OptIn(ExperimentalTvMaterial3Api::class)
val Typography = Typography(
    bodyMedium = TextStyle(
        fontFamily = merriweatherFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = merriweatherFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 100.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.15.sp
    ),
    displayLarge = TextStyle(
        fontFamily = merriweatherFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 50.sp,
        lineHeight = 72.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = merriweatherFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.5.sp
    ),
    displayMedium = TextStyle(
        fontFamily = merriweatherFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.15.sp
    )
)