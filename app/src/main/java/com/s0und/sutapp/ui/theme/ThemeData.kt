package com.s0und.sutapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.s0und.sutapp.R

val BonchOrange = Color(224, 146, 28, 255)
val BonchYellow = Color(117, 163, 30, 255)
val BonchRed = Color(213, 47, 32, 255)
val BonchBlue = Color(0xFF9976AC)
val SlightBonchBlue = Color(0xFFD9B3E2)
val DarkBonchBlue = Color(0xFF793288)

val Grey900 = Color(0xFF0E0E11)
val Grey800 = Color(0xFF14161A)
val Grey700 = Color(0xFFBE6B21)
val Grey100 = Color(0xFFECECEC)
val Grey50 = Color(0xFFF8F9FA)

val WhiteSubColor = Color(0xFF828B92)
val DarkSubColor = Color(0xFF555B61)

val White = Color(0xFFFBF2FD)

val Shapes = androidx.compose.material.Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(24.dp)
)

val Typography = androidx.compose.material.Typography(
    defaultFontFamily = FontFamily(
        Font(R.font.inter),
        //Font(R.font.montserrat, style = FontStyle.Italic),
        Font(R.font.inter_light, weight = FontWeight.Light),
        Font(R.font.inter_bold, weight = FontWeight.Bold),
        Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
        Font(R.font.inter_extrabold, weight = FontWeight.ExtraBold),
        Font(R.font.inter_medium, weight = FontWeight.Medium),

        Font(R.font.nunito_black, weight = FontWeight.Black),
    )

)
