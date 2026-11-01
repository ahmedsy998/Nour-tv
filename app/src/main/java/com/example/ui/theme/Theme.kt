package com.example.ui.theme

import android.content.Context
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.cos
import kotlin.math.sin

enum class LauncherThemeOption(
    val id: String,
    val arabicTitle: String,
    val arabicSubtitle: String,
    val baseBg: Color,
    val secondaryBg: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val tertiaryAccent: Color,
    val cardGlassColor: Color
) {
    DARK_GLASS(
        id = "dark_glass",
        arabicTitle = "الزجاج الداكن (Dark Glass)",
        arabicSubtitle = "تأثير زجاجي ضبابي أنيق مع إضاءة سماوية هادئة",
        baseBg = GlassMidnightBg,
        secondaryBg = GlassDeepNavy,
        primaryAccent = GlassElectricCyan,
        secondaryAccent = GlassRoyalIndigo,
        tertiaryAccent = GlassNeonViolet,
        cardGlassColor = Color(0x28FFFFFF)
    ),
    DEEP_CHARCOAL(
        id = "deep_charcoal",
        arabicTitle = "الفحمي العميق (Deep Charcoal)",
        arabicSubtitle = "رمادي فحمي دافئ ومريح للعين في الغرف المظلمة",
        baseBg = CharcoalObsidian,
        secondaryBg = CharcoalSlate,
        primaryAccent = CharcoalEmerald,
        secondaryAccent = CharcoalTeal,
        tertiaryAccent = Color(0xFF60A5FA),
        cardGlassColor = Color(0x24FFFFFF)
    ),
    AMOLED_BLACK(
        id = "amoled_black",
        arabicTitle = "أسود أموليد (AMOLED Black)",
        arabicSubtitle = "تباين فائق وسواد حقيقي لتوفير الطاقة على شاشات OLED",
        baseBg = AmoledTrueBlack,
        secondaryBg = AmoledSurface,
        primaryAccent = AmoledIceBlue,
        secondaryAccent = Color(0xFFE2E8F0),
        tertiaryAccent = Color(0xFF38BDF8),
        cardGlassColor = Color(0x1EFFFFFF)
    ),
    VIBRANT_WAVY_MESH(
        id = "vibrant_mesh",
        arabicTitle = "تموجات نيون حيوية (Vibrant Wavy Mesh)",
        arabicSubtitle = "خلفية موجية متحركة بألوان الطيف البنفسجي والسماوي",
        baseBg = MeshDeepPurple,
        secondaryBg = Color(0xFF1E103D),
        primaryAccent = MeshAuroraCyan,
        secondaryAccent = MeshMagenta,
        tertiaryAccent = MeshElectricViolet,
        cardGlassColor = Color(0x2EFFFFFF)
    );

    companion object {
        fun fromId(id: String?): LauncherThemeOption =
            entries.find { it.id == id } ?: DARK_GLASS
    }
}

enum class TileScaleOption(
    val id: String,
    val arabicLabel: String,
    val arabicSubtitle: String,
    val scaleFactor: Float
) {
    SMALL(
        id = "small",
        arabicLabel = "صغير (Small)",
        arabicSubtitle = "عرض عدد أكبر من التطبيقات في الصف الواحد",
        scaleFactor = 0.86f
    ),
    MEDIUM(
        id = "medium",
        arabicLabel = "متوسط (Medium)",
        arabicSubtitle = "الحجم المتوازن المثالي لشاشات التلفاز",
        scaleFactor = 1.0f
    ),
    LARGE(
        id = "large",
        arabicLabel = "كبير (Large)",
        arabicSubtitle = "أيقونات وبطاقات كبيرة لسهولة القراءة عن بُعد",
        scaleFactor = 1.18f
    );

    companion object {
        fun fromId(id: String?): TileScaleOption =
            entries.find { it.id == id } ?: MEDIUM
    }
}

data class LauncherPreferencesState(
    val theme: LauncherThemeOption = LauncherThemeOption.DARK_GLASS,
    val font: LauncherFontOption = LauncherFontOption.CAIRO,
    val tileScale: TileScaleOption = TileScaleOption.MEDIUM,
    val animatedMeshEnabled: Boolean = true
)

object LauncherPreferencesRepository {
    private const val PREFS_NAME = "noor_tv_launcher_prefs"
    private const val KEY_THEME = "key_theme"
    private const val KEY_FONT = "key_font"
    private const val KEY_SCALE = "key_scale"
    private const val KEY_ANIMATED_MESH = "key_animated_mesh"

    private val _state = MutableStateFlow(LauncherPreferencesState())
    val state: StateFlow<LauncherPreferencesState> = _state.asStateFlow()
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _state.value = LauncherPreferencesState(
            theme = LauncherThemeOption.fromId(prefs.getString(KEY_THEME, LauncherThemeOption.DARK_GLASS.id)),
            font = LauncherFontOption.fromId(prefs.getString(KEY_FONT, LauncherFontOption.CAIRO.id)),
            tileScale = TileScaleOption.fromId(prefs.getString(KEY_SCALE, TileScaleOption.MEDIUM.id)),
            animatedMeshEnabled = prefs.getBoolean(KEY_ANIMATED_MESH, true)
        )
        initialized = true
    }

    fun updateTheme(context: Context, theme: LauncherThemeOption) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.id).apply()
        _state.value = _state.value.copy(theme = theme)
    }

    fun updateFont(context: Context, font: LauncherFontOption) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FONT, font.id).apply()
        _state.value = _state.value.copy(font = font)
    }

    fun updateTileScale(context: Context, scale: TileScaleOption) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SCALE, scale.id).apply()
        _state.value = _state.value.copy(tileScale = scale)
    }

    fun updateAnimatedMesh(context: Context, enabled: Boolean) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ANIMATED_MESH, enabled).apply()
        _state.value = _state.value.copy(animatedMeshEnabled = enabled)
    }
}

@Composable
fun AnimatedMeshBackground(
    theme: LauncherThemeOption,
    animated: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "mesh_wave_transition")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val currentPhase = if (animated) phase else 0.8f

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base deep gradient fill
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.baseBg, theme.secondaryBg, theme.baseBg)
            )
        )

        if (theme == LauncherThemeOption.AMOLED_BLACK && !animated) {
            return@Canvas
        }

        val orbAlpha = if (theme == LauncherThemeOption.AMOLED_BLACK) 0.12f else 0.26f

        // Ambient glowing mesh orb 1 (Top Right in RTL)
        val orb1X = w * (0.75f + 0.12f * cos(currentPhase))
        val orb1Y = h * (0.25f + 0.10f * sin(currentPhase))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(theme.primaryAccent.copy(alpha = orbAlpha), Color.Transparent),
                center = Offset(orb1X, orb1Y),
                radius = w * 0.48f
            ),
            center = Offset(orb1X, orb1Y),
            radius = w * 0.48f
        )

        // Ambient glowing mesh orb 2 (Bottom Left)
        val orb2X = w * (0.25f + 0.14f * sin(currentPhase * 0.8f))
        val orb2Y = h * (0.72f + 0.10f * cos(currentPhase * 0.8f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(theme.secondaryAccent.copy(alpha = orbAlpha), Color.Transparent),
                center = Offset(orb2X, orb2Y),
                radius = w * 0.52f
            ),
            center = Offset(orb2X, orb2Y),
            radius = w * 0.52f
        )

        // Flowing Silk Wavy Ribbon Path
        val wavePath = Path().apply {
            val startY = h * 0.62f
            moveTo(0f, startY)
            val cp1X = w * 0.32f
            val cp1Y = h * (0.42f + 0.12f * sin(currentPhase))
            val cp2X = w * 0.68f
            val cp2Y = h * (0.82f + 0.12f * cos(currentPhase))
            cubicTo(cp1X, cp1Y, cp2X, cp2Y, w, h * 0.56f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }

        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    theme.tertiaryAccent.copy(alpha = if (theme == LauncherThemeOption.AMOLED_BLACK) 0.06f else 0.16f),
                    theme.primaryAccent.copy(alpha = 0.04f),
                    Color.Transparent
                )
            ),
            style = Fill
        )
    }
}

@Composable
fun MyApplicationTheme(
    preferencesState: LauncherPreferencesState = LauncherPreferencesState(),
    content: @Composable () -> Unit
) {
    val theme = preferencesState.theme
    val colorScheme = darkColorScheme(
        primary = theme.primaryAccent,
        secondary = theme.secondaryAccent,
        tertiary = theme.tertiaryAccent,
        background = theme.baseBg,
        surface = theme.secondaryBg,
        onPrimary = Color.Black,
        onSecondary = TextPrimarySnow,
        onBackground = TextPrimarySnow,
        onSurface = TextPrimarySnow
    )

    val typography = buildLauncherTypography(preferencesState.font)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
