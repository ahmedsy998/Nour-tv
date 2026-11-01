package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val AmiriFontFamily = FontFamily(
    Font(R.font.amiri, FontWeight.Normal),
    Font(R.font.amiri, FontWeight.Bold)
)

val CairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.Bold)
)

enum class LauncherFontOption(
    val id: String,
    val arabicTitle: String,
    val arabicDescription: String
) {
    CAIRO(
        id = "cairo",
        arabicTitle = "خط القاهرة (Cairo)",
        arabicDescription = "خط عربي هندسي عصري واضح للشاشات الكبيرة"
    ),
    AMIRI(
        id = "amiri",
        arabicTitle = "خط أميري (Amiri)",
        arabicDescription = "خط عربي أصيل وأنيق مستوحى من الطباعة الكلاسيكية"
    ),
    SYSTEM(
        id = "system",
        arabicTitle = "خط النظام الافتراضي",
        arabicDescription = "الخط القياسي المدمج في نظام أندرويد تي في"
    );

    fun toFontFamily(): FontFamily = when (this) {
        CAIRO -> CairoFontFamily
        AMIRI -> AmiriFontFamily
        SYSTEM -> FontFamily.Default
    }

    companion object {
        fun fromId(id: String?): LauncherFontOption =
            entries.find { it.id == id } ?: CAIRO
    }
}

fun buildLauncherTypography(fontOption: LauncherFontOption): Typography {
    val family = fontOption.toFontFamily()
    val sizeMultiplier = if (fontOption == LauncherFontOption.AMIRI) 1.06f else 1.0f

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (38 * sizeMultiplier).sp,
            lineHeight = (46 * sizeMultiplier).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (26 * sizeMultiplier).sp,
            lineHeight = (34 * sizeMultiplier).sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * sizeMultiplier).sp,
            lineHeight = (28 * sizeMultiplier).sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * sizeMultiplier).sp,
            lineHeight = (22 * sizeMultiplier).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * sizeMultiplier).sp,
            lineHeight = (22 * sizeMultiplier).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = (13 * sizeMultiplier).sp,
            lineHeight = (19 * sizeMultiplier).sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = (13 * sizeMultiplier).sp,
            lineHeight = (18 * sizeMultiplier).sp
        )
    )
}

val Typography = buildLauncherTypography(LauncherFontOption.CAIRO)
