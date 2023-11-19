package hsk.practice.myvoca.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import hsk.practice.myvoca.R

val Paybooc = FontFamily(
    Font(R.font.paybooc_medium),
    Font(R.font.paybooc_light, FontWeight.Light),
    Font(R.font.paybooc_bold, FontWeight.Bold),
    Font(R.font.paybooc_extrabold, FontWeight.ExtraBold)
)

val NanumSquareRound = FontFamily(
    Font(R.font.nanum_square_r),
    Font(R.font.nanum_square_l),
    Font(R.font.nanum_square_round_l),
    Font(R.font.nanum_square_round_r)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
    /* Other default text styles to override
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
    */
)