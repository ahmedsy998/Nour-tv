package com.example

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoSizeSelectLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AnimatedMeshBackground
import com.example.ui.theme.FocusWhiteBorder
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.LauncherFontOption
import com.example.ui.theme.LauncherPreferencesRepository
import com.example.ui.theme.LauncherPreferencesState
import com.example.ui.theme.LauncherThemeOption
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondaryMist
import com.example.ui.theme.TileScaleOption
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LauncherPreferencesRepository.init(this)
        enableEdgeToEdge()
        setContent {
            val prefsState by LauncherPreferencesRepository.state.collectAsState()
            MyApplicationTheme(preferencesState = prefsState) {
                LauncherSettingsScreen(
                    preferencesState = prefsState,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LauncherSettingsScreen(
    preferencesState: LauncherPreferencesState,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    BackHandler {
        onNavigateBack()
    }

    val showFeedback: (String) -> Unit = { msg ->
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedMeshBackground(
            theme = preferencesState.theme,
            animated = preferencesState.animatedMeshEnabled
        )

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 28.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = preferencesState.theme.cardGlassColor,
                            border = BorderStroke(1.dp, FrostedCardBorder)
                        ) {
                            IconButton(
                                onClick = onNavigateBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("settings_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = TextPrimarySnow
                                )
                            }
                        }

                        Column {
                            Text(
                                text = stringResource(R.string.settings_title),
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimarySnow
                            )
                            Text(
                                text = stringResource(R.string.settings_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryMist
                            )
                        }
                    }

                    // Active Profile Badge
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = preferencesState.theme.primaryAccent.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, preferencesState.theme.primaryAccent)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = preferencesState.theme.primaryAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${preferencesState.theme.arabicTitle} • ${preferencesState.font.arabicTitle}",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimarySnow
                            )
                        }
                    }
                }

                // 1. UI Scaling Section (Small, Medium, Large)
                SettingsSectionGlassCard(
                    title = stringResource(R.string.settings_scale_label),
                    subtitle = "تحكم في أبعاد بطاقات التطبيقات وأيقونات الصفوف لتناسب حجم شاشة التلفاز",
                    icon = Icons.Default.PhotoSizeSelectLarge,
                    accentColor = preferencesState.theme.primaryAccent
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TileScaleOption.entries.forEach { option ->
                            val selected = preferencesState.tileScale == option
                            SelectableGlassOptionCard(
                                title = option.arabicLabel,
                                subtitle = option.arabicSubtitle,
                                selected = selected,
                                accentColor = preferencesState.theme.primaryAccent,
                                testTag = "scale_option_${option.id}",
                                onClick = {
                                    LauncherPreferencesRepository.updateTileScale(context, option)
                                    showFeedback("تم ضبط حجم البطاقات إلى: ${option.arabicLabel}")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Typography Selector Section (Amiri, Cairo, System)
                SettingsSectionGlassCard(
                    title = stringResource(R.string.settings_font_label),
                    subtitle = "اختر الخط العربي المفضل لعناوين التطبيقات والساعة الرقمية (يشمل خط أميري الكلاسيكي)",
                    icon = Icons.Default.FontDownload,
                    accentColor = preferencesState.theme.secondaryAccent
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LauncherFontOption.entries.forEach { fontOption ->
                            val selected = preferencesState.font == fontOption
                            SelectableGlassOptionCard(
                                title = fontOption.arabicTitle,
                                subtitle = fontOption.arabicDescription,
                                selected = selected,
                                accentColor = preferencesState.theme.secondaryAccent,
                                testTag = "font_option_${fontOption.id}",
                                onClick = {
                                    LauncherPreferencesRepository.updateFont(context, fontOption)
                                    showFeedback("تم تفعيل ${fontOption.arabicTitle}")
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Themes & Animated Wavy Mesh Background Section
                SettingsSectionGlassCard(
                    title = stringResource(R.string.settings_theme_label),
                    subtitle = "تخصيص المظهر الزجاجي، الخلفية الموجية المتحركة، والخلفيات الحية للنظام",
                    icon = Icons.Default.Palette,
                    accentColor = preferencesState.theme.tertiaryAccent
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LauncherThemeOption.entries.forEach { themeOption ->
                                val selected = preferencesState.theme == themeOption
                                SelectableGlassOptionCard(
                                    title = themeOption.arabicTitle,
                                    subtitle = themeOption.arabicSubtitle,
                                    selected = selected,
                                    accentColor = themeOption.primaryAccent,
                                    colorSwatch = themeOption.baseBg,
                                    testTag = "theme_option_${themeOption.id}",
                                    onClick = {
                                        LauncherPreferencesRepository.updateTheme(context, themeOption)
                                        showFeedback("تم تطبيق سمة: ${themeOption.arabicTitle}")
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Animated Wavy Mesh Toggle Row & Live Wallpaper Picker
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, FrostedCardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val next = !preferencesState.animatedMeshEnabled
                                            LauncherPreferencesRepository.updateAnimatedMesh(context, next)
                                        }
                                        .padding(horizontal = 18.dp, vertical = 14.dp)
                                        .testTag("toggle_animated_mesh"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "محرك التموجات الضوئية المتحركة (Canvas 60fps)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = TextPrimarySnow
                                        )
                                        Text(
                                            text = "إظهار حركة انسيابية ناعمة في خلفية الشاشة الرئيسية",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextSecondaryMist
                                        )
                                    }
                                    Switch(
                                        checked = preferencesState.animatedMeshEnabled,
                                        onCheckedChange = { enabled ->
                                            LauncherPreferencesRepository.updateAnimatedMesh(context, enabled)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = preferencesState.theme.primaryAccent
                                        )
                                    )
                                }
                            }

                            SystemActionGlassButton(
                                title = stringResource(R.string.action_live_wallpaper),
                                subtitle = "منتقي الخلفيات الحية",
                                icon = Icons.Default.Wallpaper,
                                accentColor = preferencesState.theme.primaryAccent,
                                testTag = "btn_live_wallpaper_picker",
                                onClick = {
                                    openLiveWallpaperPicker(context, showFeedback)
                                },
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }

                // 4. Native Android TV System Settings Integration
                SettingsSectionGlassCard(
                    title = stringResource(R.string.settings_system_label),
                    subtitle = "اختصارات مباشرة لفتح إعدادات نظام أندرويد تي في والشبكة والتطبيقات",
                    icon = Icons.Default.Settings,
                    accentColor = StatusOnlineGreen
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SystemActionGlassButton(
                            title = stringResource(R.string.action_system_settings),
                            subtitle = "android.settings.SETTINGS",
                            icon = Icons.Default.Settings,
                            accentColor = StatusOnlineGreen,
                            testTag = "btn_native_system_settings",
                            onClick = {
                                launchSystemSettingsAction(
                                    context = context,
                                    action = Settings.ACTION_SETTINGS,
                                    label = "إعدادات النظام",
                                    onFeedback = showFeedback
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SystemActionGlassButton(
                            title = "إعدادات الواي فاي والشبكة",
                            subtitle = "إدارة الاتصال والإيثرنت",
                            icon = Icons.Default.Wifi,
                            accentColor = preferencesState.theme.primaryAccent,
                            testTag = "btn_wifi_system_settings",
                            onClick = {
                                launchSystemSettingsAction(
                                    context = context,
                                    action = Settings.ACTION_WIFI_SETTINGS,
                                    label = "إعدادات الشبكة",
                                    onFeedback = showFeedback
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SystemActionGlassButton(
                            title = "إدارة التطبيقات المثبتة",
                            subtitle = "الصلاحيات والتخزين",
                            icon = Icons.Default.Apps,
                            accentColor = preferencesState.theme.secondaryAccent,
                            testTag = "btn_manage_apps_settings",
                            onClick = {
                                launchSystemSettingsAction(
                                    context = context,
                                    action = Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
                                    label = "إدارة التطبيقات",
                                    onFeedback = showFeedback
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SystemActionGlassButton(
                            title = "إعدادات الشاشة والعرض",
                            subtitle = "الدقة ومعدل التحديث",
                            icon = Icons.Default.Tv,
                            accentColor = preferencesState.theme.tertiaryAccent,
                            testTag = "btn_display_system_settings",
                            onClick = {
                                launchSystemSettingsAction(
                                    context = context,
                                    action = Settings.ACTION_DISPLAY_SETTINGS,
                                    label = "إعدادات العرض",
                                    onFeedback = showFeedback
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionGlassCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, FrostedCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimarySnow
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryMist
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun SelectableGlassOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorSwatch: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        label = "option_focus_scale"
    )

    val border = when {
        isFocused -> BorderStroke(2.5.dp, FocusWhiteBorder)
        selected -> BorderStroke(2.dp, accentColor)
        else -> BorderStroke(1.dp, FrostedCardBorder)
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) accentColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.06f),
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (colorSwatch != null) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(colorSwatch, accentColor)
                                )
                            )
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimarySnow
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryMist
                    )
                }
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "مفعل",
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SystemActionGlassButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.06f else 1.0f,
        label = "sys_btn_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = if (isFocused) 0.18f else 0.09f),
        border = BorderStroke(
            width = if (isFocused) 2.5.dp else 1.dp,
            color = if (isFocused) FocusWhiteBorder else accentColor.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimarySnow
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryMist
                )
            }
        }
    }
}

private fun openLiveWallpaperPicker(context: Context, onFeedback: (String) -> Unit) {
    val lwpIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val openedLwp = runCatching {
        context.startActivity(lwpIntent)
        true
    }.getOrDefault(false)

    if (openedLwp) {
        onFeedback("تم فتح منتقي الخلفيات الحية للنظام")
        return
    }

    val fallbackWallpaperIntent = Intent(Intent.ACTION_SET_WALLPAPER).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(Intent.createChooser(fallbackWallpaperIntent, "اختيار خلفية الشاشة"))
        onFeedback("تم فتح إعدادات خلفية الشاشة")
    }.onFailure {
        onFeedback("منتقي الخلفيات الحية غير مدعوم على هذا الجهاز، يمكنك استخدام الخلفية الموجية المدمجة")
    }
}

private fun launchSystemSettingsAction(
    context: Context,
    action: String,
    label: String,
    onFeedback: (String) -> Unit
) {
    val intent = Intent(action).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(intent)
        onFeedback("تم فتح $label")
    }.onFailure {
        val fallback = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(fallback)
            onFeedback("تم فتح إعدادات نظام أندرويد")
        }.onFailure {
            onFeedback("تعذر فتح إعدادات النظام على هذا المحاكي")
        }
    }
}
