package com.example

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AnimatedMeshBackground
import com.example.ui.theme.FocusWhiteBorder
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.LauncherPreferencesRepository
import com.example.ui.theme.LauncherPreferencesState
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StatusOnlineGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondaryMist
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NetworkConnectionType {
    ETHERNET,
    WIFI,
    ONLINE_OTHER,
    OFFLINE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LauncherPreferencesRepository.init(this)
        enableEdgeToEdge()
        setContent {
            val prefsState by LauncherPreferencesRepository.state.collectAsState()
            MyApplicationTheme(preferencesState = prefsState) {
                TvLauncherHomeScreen(
                    preferencesState = prefsState,
                    onOpenSettingsActivity = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun TvLauncherHomeScreen(
    preferencesState: LauncherPreferencesState,
    onOpenSettingsActivity: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // 1. Live Arabic 12-hour Digital Clock Loop (with AM/PM: ص / م)
    var arabicTimeText by remember { mutableStateOf(formatArabicClock12h(Date())) }
    var arabicDateText by remember { mutableStateOf(formatArabicDate(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            arabicTimeText = formatArabicClock12h(now)
            arabicDateText = formatArabicDate(now)
            delay(1000L)
        }
    }

    // 2. Real-time Wi-Fi / Ethernet Connectivity Monitor
    val networkStatus = rememberNetworkStatus(context)

    // 3. Dynamic PackageManager Loader State
    var pinnedApps by remember { mutableStateOf<List<TvAppItem>>(emptyList()) }
    var installedApps by remember { mutableStateOf<List<TvAppItem>>(emptyList()) }
    var selectedCategoryFilter by remember { mutableStateOf(AppCategoryFilter.ALL) }
    var focusedApp by remember { mutableStateOf<TvAppItem?>(null) }
    var showStoreHubOverlay by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadTrigger) {
        val (loadedPinned, loadedInstalled) = AppAdapter.loadPinnedAndInstalledApps(context)
        pinnedApps = loadedPinned
        installedApps = loadedInstalled
        if (focusedApp == null) {
            focusedApp = loadedPinned.firstOrNull() ?: loadedInstalled.firstOrNull()
        }
    }

    val filteredInstalledApps = remember(installedApps, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            AppCategoryFilter.ALL -> installedApps
            AppCategoryFilter.INSTALLED -> installedApps.filter { it.isInstalledOnDevice }
            AppCategoryFilter.MEDIA -> installedApps.filter { it.categoryFilter == AppCategoryFilter.MEDIA }
            AppCategoryFilter.SYSTEM -> installedApps.filter { it.categoryFilter == AppCategoryFilter.SYSTEM }
        }
    }

    val showStatusMessage: (String) -> Unit = { message ->
        coroutineScope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Lightweight 60fps Animated Wavy Mesh Canvas Shader Background
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 26.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Header Bar (Arabic RTL: Clock & Brand on Start/Right, Network & Settings ⚙️ on End/Left)
                TvLauncherHeaderBar(
                    timeText = arabicTimeText,
                    dateText = arabicDateText,
                    networkType = networkStatus,
                    accentColor = preferencesState.theme.primaryAccent,
                    onOpenStoreHub = { showStoreHubOverlay = true },
                    onOpenSettings = onOpenSettingsActivity
                )

                // Spotlight Focused App Banner (Interactive Hero Bar)
                focusedApp?.let { activeApp ->
                    FocusedAppSpotlightBanner(
                        app = activeApp,
                        accentColor = preferencesState.theme.primaryAccent,
                        onLaunchApp = {
                            AppAdapter.launchApp(
                                context = context,
                                app = activeApp,
                                onOpenStoreHub = { showStoreHubOverlay = true },
                                onStatusMessage = showStatusMessage
                            )
                        },
                        onOpenAppInfo = {
                            openSystemAppDetails(context, activeApp.packageName, showStatusMessage)
                        }
                    )
                }

                // Top Row: Pinned / Fast Access (YouTube, Google Chrome, System App Store, File Manager)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = preferencesState.theme.primaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.section_pinned_apps),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimarySnow
                            )
                        }

                        Text(
                            text = "بطاقات متساوية • تكبير 1.1x عند التركيز",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryMist
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pinned_apps_row"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(pinnedApps, key = { it.id }) { app ->
                            GlassAppCard(
                                app = app,
                                isSelectedSpotlight = focusedApp?.id == app.id,
                                tileScale = preferencesState.tileScale,
                                themeOption = preferencesState.theme,
                                onFocused = { focusedApp = it },
                                onClick = { clickedApp ->
                                    focusedApp = clickedApp
                                    AppAdapter.launchApp(
                                        context = context,
                                        app = clickedApp,
                                        onOpenStoreHub = { showStoreHubOverlay = true },
                                        onStatusMessage = showStatusMessage
                                    )
                                }
                            )
                        }
                    }
                }

                // Bottom Row: All Installed Apps (Dynamically queried from PackageManager)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = preferencesState.theme.secondaryAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "${stringResource(R.string.section_all_apps)} (${filteredInstalledApps.size})",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimarySnow
                            )
                        }

                        // Filter Chips + Refresh PackageManager Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppCategoryFilter.entries.forEach { filter ->
                                val selected = selectedCategoryFilter == filter
                                Surface(
                                    modifier = Modifier
                                        .clickable { selectedCategoryFilter = filter }
                                        .testTag("filter_chip_${filter.id}"),
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) {
                                        preferencesState.theme.primaryAccent.copy(alpha = 0.25f)
                                    } else {
                                        Color.White.copy(alpha = 0.08f)
                                    },
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (selected) preferencesState.theme.primaryAccent else FrostedCardBorder
                                    )
                                ) {
                                    Text(
                                        text = filter.arabicTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimarySnow,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    reloadTrigger++
                                    showStatusMessage("تم تحديث قائمة التطبيقات المثبتة")
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("refresh_apps_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحديث التطبيقات",
                                    tint = TextPrimarySnow
                                )
                            }
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("installed_apps_row"),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredInstalledApps, key = { it.id }) { app ->
                            GlassAppCard(
                                app = app,
                                isSelectedSpotlight = focusedApp?.id == app.id,
                                tileScale = preferencesState.tileScale,
                                themeOption = preferencesState.theme,
                                onFocused = { focusedApp = it },
                                onClick = { clickedApp ->
                                    focusedApp = clickedApp
                                    AppAdapter.launchApp(
                                        context = context,
                                        app = clickedApp,
                                        onOpenStoreHub = { showStoreHubOverlay = true },
                                        onStatusMessage = showStatusMessage
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Built-in Essential TV App Store & Utilities Directory Overlay
        AnimatedVisibility(
            visible = showStoreHubOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EssentialTvStoreOverlay(
                preferencesState = preferencesState,
                onDismiss = { showStoreHubOverlay = false },
                onStatusMessage = showStatusMessage
            )
        }
    }
}

@Composable
private fun TvLauncherHeaderBar(
    timeText: String,
    dateText: String,
    networkType: NetworkConnectionType,
    accentColor: Color,
    onOpenStoreHub: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("launcher_header_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Digital Clock (Arabic 12-hour format with AM/PM) + Date
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, FrostedCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimarySnow,
                        modifier = Modifier.testTag("arabic_digital_clock")
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(FrostedCardBorder)
                    )

                    Column {
                        Text(
                            text = stringResource(R.string.launcher_brand_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryMist
                        )
                    }
                }
            }
        }

        // End Controls: Wi-Fi / Ethernet Status Indicator + Built-in Store Button + Settings ⚙️ Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wi-Fi / Ethernet Connectivity Status Pill
            val (netIcon, netLabel, netTint) = when (networkType) {
                NetworkConnectionType.ETHERNET -> Triple(
                    Icons.Default.SettingsEthernet,
                    stringResource(R.string.status_ethernet_connected),
                    StatusOnlineGreen
                )
                NetworkConnectionType.WIFI -> Triple(
                    Icons.Default.Wifi,
                    stringResource(R.string.status_wifi_connected),
                    StatusOnlineGreen
                )
                NetworkConnectionType.ONLINE_OTHER -> Triple(
                    Icons.Default.Wifi,
                    "متصل بالشبكة",
                    StatusOnlineGreen
                )
                NetworkConnectionType.OFFLINE -> Triple(
                    Icons.Default.WifiOff,
                    stringResource(R.string.status_offline),
                    StatusWarningAmber
                )
            }

            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, FrostedCardBorder),
                modifier = Modifier.testTag("network_status_indicator")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = netIcon,
                        contentDescription = netLabel,
                        tint = netTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = netLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimarySnow
                    )
                }
            }

            // Built-in Essential TV App Store Button
            HeaderActionButton(
                label = stringResource(R.string.action_open_store),
                icon = Icons.Default.ShoppingBag,
                accentColor = accentColor,
                testTag = "open_store_button",
                onClick = onOpenStoreHub
            )

            // Settings Shortcut Icon (⚙️)
            HeaderActionButton(
                label = stringResource(R.string.action_open_settings),
                icon = Icons.Default.Settings,
                accentColor = FocusWhiteBorder,
                testTag = "open_settings_button",
                onClick = onOpenSettings
            )
        }
    }
}

@Composable
private fun HeaderActionButton(
    label: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.08f else 1.0f,
        label = "header_btn_scale"
    )

    Surface(
        modifier = Modifier
            .height(48.dp)
            .scale(scale)
            .focusable(interactionSource = interactionSource)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = if (isFocused) 0.24f else 0.12f),
        border = BorderStroke(
            width = if (isFocused) 2.5.dp else 1.dp,
            color = if (isFocused) FocusWhiteBorder else FrostedCardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimarySnow
            )
        }
    }
}

@Composable
private fun FocusedAppSpotlightBanner(
    app: TvAppItem,
    accentColor: Color,
    onLaunchApp: () -> Unit,
    onOpenAppInfo: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("focused_app_spotlight"),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, FrostedCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            app.brandColor.copy(alpha = 0.26f),
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(app.brandColor.copy(alpha = 0.30f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.iconBitmap != null) {
                        androidx.compose.foundation.Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.arabicName,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        Icon(
                            imageVector = app.fallbackIcon,
                            contentDescription = app.arabicName,
                            tint = TextPrimarySnow,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = app.arabicName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimarySnow
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = app.brandColor.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = if (app.isPinned) "وصول سريع" else "تطبيق نظام",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimarySnow,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${app.subtitle} • ${app.packageName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryMist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .height(48.dp)
                        .clickable(onClick = onLaunchApp)
                        .testTag("spotlight_launch_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = accentColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "تشغيل",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "تشغيل التطبيق",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onOpenAppInfo)
                        .testTag("spotlight_app_info_button"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, FrostedCardBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "معلومات التطبيق",
                            tint = TextPrimarySnow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EssentialTvStoreOverlay(
    preferencesState: LauncherPreferencesState,
    onDismiss: () -> Unit,
    onStatusMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("الكل") }
    val categories = listOf(
        "الكل",
        "مشغلات الوسائط",
        "متصفحات التلفاز",
        "إدارة الملفات",
        "أدوات البث والشبكة"
    )

    val filteredCatalog = remember(selectedCategory) {
        if (selectedCategory == "الكل") {
            AppAdapter.essentialTvStoreCatalog
        } else {
            AppAdapter.essentialTvStoreCatalog.filter { it.categoryName == selectedCategory }
        }
    }

    BackHandler {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xEB070B14))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 20.dp)
            .testTag("tv_store_overlay")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Store Header
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
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, FrostedCardBorder)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_store_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "إغلاق المتجر",
                                tint = TextPrimarySnow
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.section_tv_store),
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimarySnow
                        )
                        Text(
                            text = "دليل أدوات التلفاز الأساسية (مشغلات الفيديو، المتصفحات، وأدوات البث والملفات عبر الروابط والمتجر الرسمي)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryMist
                        )
                    }
                }
            }

            // Category Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        modifier = Modifier
                            .clickable { selectedCategory = category }
                            .testTag("store_category_$category"),
                        shape = RoundedCornerShape(50),
                        color = if (isSelected) {
                            preferencesState.theme.primaryAccent.copy(alpha = 0.26f)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) preferencesState.theme.primaryAccent else FrostedCardBorder
                        )
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimarySnow,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // Store Tools List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredCatalog, key = { it.id }) { tool ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    val scale by animateFloatAsState(
                        targetValue = if (isFocused) 1.03f else 1.0f,
                        label = "store_item_scale"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .scale(scale)
                            .focusable(interactionSource = interactionSource)
                            .clickable {
                                AppAdapter.openExternalStoreOrWeb(
                                    context = context,
                                    tool = tool,
                                    onStatusMessage = onStatusMessage
                                )
                            }
                            .testTag("store_tool_${tool.id}"),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = if (isFocused) 0.16f else 0.08f),
                        border = BorderStroke(
                            width = if (isFocused) 2.5.dp else 1.dp,
                            color = if (isFocused) FocusWhiteBorder else FrostedCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(tool.accentColor.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.arabicName,
                                        tint = tool.accentColor,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = tool.arabicName,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = TextPrimarySnow
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = tool.accentColor.copy(alpha = 0.22f)
                                        ) {
                                            Text(
                                                text = tool.badgeText,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TextPrimarySnow,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = tool.arabicDescription,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondaryMist
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = tool.accentColor
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "فتح المتجر",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberNetworkStatus(context: Context): NetworkConnectionType {
    var status by remember { mutableStateOf(queryCurrentNetworkType(context)) }

    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                status = queryCurrentNetworkType(context)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                status = parseNetworkCapabilities(networkCapabilities)
            }

            override fun onLost(network: Network) {
                status = queryCurrentNetworkType(context)
            }
        }

        runCatching {
            cm?.registerDefaultNetworkCallback(callback)
        }

        onDispose {
            runCatching {
                cm?.unregisterNetworkCallback(callback)
            }
        }
    }

    return status
}

private fun queryCurrentNetworkType(context: Context): NetworkConnectionType {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return NetworkConnectionType.OFFLINE
    val activeNet = cm.activeNetwork ?: return NetworkConnectionType.OFFLINE
    val caps = cm.getNetworkCapabilities(activeNet) ?: return NetworkConnectionType.OFFLINE
    return parseNetworkCapabilities(caps)
}

private fun parseNetworkCapabilities(caps: NetworkCapabilities): NetworkConnectionType {
    return when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkConnectionType.ETHERNET
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkConnectionType.WIFI
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkConnectionType.ONLINE_OTHER
        else -> NetworkConnectionType.OFFLINE
    }
}

private fun formatArabicClock12h(date: Date): String {
    val arabicLocale = Locale.forLanguageTag("ar")
    val formatter = SimpleDateFormat("hh:mm a", arabicLocale)
    return formatter.format(date)
}

private fun formatArabicDate(date: Date): String {
    val arabicLocale = Locale.forLanguageTag("ar")
    val formatter = SimpleDateFormat("EEEE، d MMMM yyyy", arabicLocale)
    return formatter.format(date)
}

private fun openSystemAppDetails(
    context: Context,
    packageName: String,
    onFeedback: (String) -> Unit
) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        val fallback = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(fallback)
        }.onFailure {
            onFeedback("تعذر فتح إعدادات التطبيق ($packageName)")
        }
    }
}
