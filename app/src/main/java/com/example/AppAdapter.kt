package com.example

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FocusWhiteBorder
import com.example.ui.theme.FrostedCardBorder
import com.example.ui.theme.LauncherThemeOption
import com.example.ui.theme.TextPrimarySnow
import com.example.ui.theme.TextSecondaryMist
import com.example.ui.theme.TileScaleOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AppCategoryFilter(val id: String, val arabicTitle: String) {
    ALL("all", "الكل"),
    INSTALLED("installed", "مثبتة على الجهاز"),
    MEDIA("media", "الوسائط والبث"),
    SYSTEM("system", "أدوات النظام")
}

data class TvAppItem(
    val id: String,
    val packageName: String,
    val arabicName: String,
    val subtitle: String,
    val categoryFilter: AppCategoryFilter,
    val isPinned: Boolean,
    val isInstalledOnDevice: Boolean,
    val brandColor: Color,
    val fallbackIcon: ImageVector,
    val iconBitmap: ImageBitmap? = null,
    val fallbackUrl: String? = null,
    val opensStoreHub: Boolean = false,
    val systemIntentAction: String? = null
)

data class EssentialStoreTool(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val categoryName: String,
    val arabicDescription: String,
    val packageName: String,
    val officialWebUrl: String,
    val badgeText: String,
    val accentColor: Color,
    val icon: ImageVector
)

object AppAdapter {

    val essentialTvStoreCatalog: List<EssentialStoreTool> = listOf(
        EssentialStoreTool(
            id = "vlc_tv",
            arabicName = "مشغل VLC للتلفاز",
            englishName = "VLC for Android TV",
            categoryName = "مشغلات الوسائط",
            arabicDescription = "مشغل فيديو وصوت شامل مفتوح المصدر يدعم جميع الصيغ والترجمة العربية والشبكة المحلية.",
            packageName = "org.videolan.vlc",
            officialWebUrl = "https://play.google.com/store/apps/details?id=org.videolan.vlc",
            badgeText = "مفتوح المصدر • 4K HDR",
            accentColor = Color(0xFFF97316),
            icon = Icons.Default.PlayCircleFilled
        ),
        EssentialStoreTool(
            id = "kodi_tv",
            arabicName = "مركز وسائط كودي",
            englishName = "Kodi Media Center",
            categoryName = "مشغلات الوسائط",
            arabicDescription = "منصة مسرح منزلي متكاملة لتنظيم مكتبة الأفلام والمسلسلات والبث المباشر على التلفاز.",
            packageName = "org.xbmc.kodi",
            officialWebUrl = "https://play.google.com/store/apps/details?id=org.xbmc.kodi",
            badgeText = "مسرح منزلي",
            accentColor = Color(0xFF0EA5E9),
            icon = Icons.Default.LiveTv
        ),
        EssentialStoreTool(
            id = "nova_player",
            arabicName = "مشغل نوفا فيديو",
            englishName = "Nova Video Player",
            categoryName = "مشغلات الوسائط",
            arabicDescription = "مشغل فيديو مصمم خصيصاً لواجهة Leanback مع دعم خوادم SMB/FTP وجلب الملصقات تلقائياً.",
            packageName = "org.courville.nova",
            officialWebUrl = "https://play.google.com/store/apps/details?id=org.courville.nova",
            badgeText = "مخصص للتلفاز",
            accentColor = Color(0xFF6366F1),
            icon = Icons.Default.Movie
        ),
        EssentialStoreTool(
            id = "tv_bro",
            arabicName = "متصفح TV Bro",
            englishName = "TV Bro Web Browser",
            categoryName = "متصفحات التلفاز",
            arabicDescription = "متصفح إنترنت خفيف ومفتوح المصدر مصمم للعمل بسلاسة مع جهاز التحكم عن بُعد (D-Pad).",
            packageName = "com.phlox.tvwebbrowser",
            officialWebUrl = "https://play.google.com/store/apps/details?id=com.phlox.tvwebbrowser",
            badgeText = "تحكم ريموت كامل",
            accentColor = Color(0xFF10B981),
            icon = Icons.Default.Language
        ),
        EssentialStoreTool(
            id = "cx_explorer",
            arabicName = "مدير ملفات CX",
            englishName = "CX File Explorer",
            categoryName = "إدارة الملفات",
            arabicDescription = "استعراض وإدارة وحدات التخزين الخارجية، الشبكة المحلية، وتحليل المساحة بواجهة عربية نظيفة.",
            packageName = "com.cxinventor.file.explorer",
            officialWebUrl = "https://play.google.com/store/apps/details?id=com.cxinventor.file.explorer",
            badgeText = "إدارة ملفات ذكية",
            accentColor = Color(0xFFF59E0B),
            icon = Icons.Default.Folder
        ),
        EssentialStoreTool(
            id = "xplore_tv",
            arabicName = "مدير ملفات X-plore",
            englishName = "X-plore File Manager",
            categoryName = "إدارة الملفات",
            arabicDescription = "مدير ملفات ثنائي اللوحة يدعم نقل الملفات عبر Wi-Fi وعرض محتويات الفلاشات على التلفاز.",
            packageName = "com.lonelycatgames.Xplore",
            officialWebUrl = "https://play.google.com/store/apps/details?id=com.lonelycatgames.Xplore",
            badgeText = "نقل لاسلكي",
            accentColor = Color(0xFFEC4899),
            icon = Icons.Default.Storage
        ),
        EssentialStoreTool(
            id = "send_files_tv",
            arabicName = "إرسال الملفات للتلفاز",
            englishName = "Send Files to TV",
            categoryName = "أدوات البث والشبكة",
            arabicDescription = "مشاركة الوسائط والملفات بسرعة فائقة بين الهاتف الذكي والتلفاز عبر شبكة الواي فاي المحلية.",
            packageName = "com.yablio.sendfilestotv",
            officialWebUrl = "https://play.google.com/store/apps/details?id=com.yablio.sendfilestotv",
            badgeText = "مشاركة محلية",
            accentColor = Color(0xFF14B8A6),
            icon = Icons.Default.CloudQueue
        ),
        EssentialStoreTool(
            id = "analiti_speed",
            arabicName = "اختبار سرعة الشبكة",
            englishName = "analiti Speed Test WiFi",
            categoryName = "أدوات البث والشبكة",
            arabicDescription = "فحص دقة وسرعة اتصال الواي فاي والإيثرنت للتأكد من جاهزية البث بدقة 4K و 8K.",
            packageName = "com.analiti.fastest.android",
            officialWebUrl = "https://play.google.com/store/apps/details?id=com.analiti.fastest.android",
            badgeText = "فحص 4K",
            accentColor = Color(0xFF8B5CF6),
            icon = Icons.Default.Speed
        )
    )

    suspend fun loadPinnedAndInstalledApps(
        context: Context
    ): Pair<List<TvAppItem>, List<TvAppItem>> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPkg = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }

        val resolvedMap = linkedMapOf<String, ResolveInfo>()
        runCatching {
            pm.queryIntentActivities(leanbackIntent, 0).forEach { info ->
                val pkg = info.activityInfo?.packageName
                if (!pkg.isNullOrBlank() && pkg != selfPkg) {
                    resolvedMap[pkg] = info
                }
            }
        }
        runCatching {
            pm.queryIntentActivities(mainIntent, 0).forEach { info ->
                val pkg = info.activityInfo?.packageName
                if (!pkg.isNullOrBlank() && pkg != selfPkg && !resolvedMap.containsKey(pkg)) {
                    resolvedMap[pkg] = info
                }
            }
        }

        // Resolve Top Row: 4 Pinned Fast-Access Apps
        val youtubePkg = listOf(
            "com.google.android.youtube.tv",
            "com.google.android.youtube"
        ).firstOrNull { resolvedMap.containsKey(it) } ?: "com.google.android.youtube.tv"

        val chromePkg = listOf(
            "com.android.chrome",
            "org.chromium.webview_shell",
            "com.google.android.apps.chrome"
        ).firstOrNull { resolvedMap.containsKey(it) } ?: "com.android.chrome"

        val storePkg = listOf(
            "com.android.vending"
        ).firstOrNull { resolvedMap.containsKey(it) } ?: "com.android.vending"

        val filesPkg = listOf(
            "com.google.android.documentsui",
            "com.android.documentsui",
            "com.google.android.apps.nbu.files"
        ).firstOrNull { resolvedMap.containsKey(it) } ?: "com.google.android.documentsui"

        val pinnedApps = listOf(
            TvAppItem(
                id = "pinned_youtube",
                packageName = youtubePkg,
                arabicName = "يوتيوب",
                subtitle = "YouTube TV • البث المرئي",
                categoryFilter = AppCategoryFilter.MEDIA,
                isPinned = true,
                isInstalledOnDevice = resolvedMap.containsKey(youtubePkg),
                brandColor = Color(0xFFEF4444),
                fallbackIcon = Icons.Default.PlayCircleFilled,
                iconBitmap = resolvedMap[youtubePkg]?.let { drawableToImageBitmap(it.loadIcon(pm)) },
                fallbackUrl = "https://www.youtube.com/tv"
            ),
            TvAppItem(
                id = "pinned_chrome",
                packageName = chromePkg,
                arabicName = "متصفح كروم",
                subtitle = "Google Chrome • تصفح الويب",
                categoryFilter = AppCategoryFilter.MEDIA,
                isPinned = true,
                isInstalledOnDevice = resolvedMap.containsKey(chromePkg),
                brandColor = Color(0xFF3B82F6),
                fallbackIcon = Icons.Default.Language,
                iconBitmap = resolvedMap[chromePkg]?.let { drawableToImageBitmap(it.loadIcon(pm)) },
                fallbackUrl = "https://www.google.com"
            ),
            TvAppItem(
                id = "pinned_store",
                packageName = storePkg,
                arabicName = "متجر التطبيقات",
                subtitle = "متجر وأدوات التلفاز",
                categoryFilter = AppCategoryFilter.SYSTEM,
                isPinned = true,
                isInstalledOnDevice = true,
                brandColor = Color(0xFF10B981),
                fallbackIcon = Icons.Default.ShoppingBag,
                iconBitmap = resolvedMap[storePkg]?.let { drawableToImageBitmap(it.loadIcon(pm)) },
                opensStoreHub = true
            ),
            TvAppItem(
                id = "pinned_files",
                packageName = filesPkg,
                arabicName = "مدير الملفات",
                subtitle = "استعراض التخزين والملفات",
                categoryFilter = AppCategoryFilter.SYSTEM,
                isPinned = true,
                isInstalledOnDevice = resolvedMap.containsKey(filesPkg),
                brandColor = Color(0xFFF59E0B),
                fallbackIcon = Icons.Default.Folder,
                iconBitmap = resolvedMap[filesPkg]?.let { drawableToImageBitmap(it.loadIcon(pm)) },
                systemIntentAction = Settings.ACTION_INTERNAL_STORAGE_SETTINGS
            )
        )

        // Bottom Row: All Installed Apps queried dynamically from PackageManager
        val installedList = mutableListOf<TvAppItem>()
        resolvedMap.forEach { (pkg, info) ->
            val rawLabel = runCatching { info.loadLabel(pm).toString() }.getOrDefault(pkg)
            val iconBmp = runCatching { drawableToImageBitmap(info.loadIcon(pm)) }.getOrNull()
            val isMedia = pkg.contains("youtube", ignoreCase = true) ||
                pkg.contains("video", ignoreCase = true) ||
                pkg.contains("music", ignoreCase = true) ||
                pkg.contains("tv", ignoreCase = true) ||
                pkg.contains("chrome", ignoreCase = true) ||
                pkg.contains("gallery", ignoreCase = true) ||
                pkg.contains("photos", ignoreCase = true)

            installedList.add(
                TvAppItem(
                    id = "installed_$pkg",
                    packageName = pkg,
                    arabicName = localizeCommonSystemAppName(rawLabel, pkg),
                    subtitle = rawLabel,
                    categoryFilter = if (isMedia) AppCategoryFilter.MEDIA else AppCategoryFilter.SYSTEM,
                    isPinned = false,
                    isInstalledOnDevice = true,
                    brandColor = pickBrandAccentForPackage(pkg),
                    fallbackIcon = Icons.Default.Apps,
                    iconBitmap = iconBmp
                )
            )
        }

        // Sort alphabetically while keeping user-facing media/system tools prominent
        installedList.sortBy { it.arabicName }

        // Ensure quick access to System Settings & Essential TV Utilities if emulator has minimal packages
        installedList.add(
            TvAppItem(
                id = "builtin_system_settings",
                packageName = "com.android.settings",
                arabicName = "إعدادات النظام",
                subtitle = "Android TV System Settings",
                categoryFilter = AppCategoryFilter.SYSTEM,
                isPinned = false,
                isInstalledOnDevice = true,
                brandColor = Color(0xFF64748B),
                fallbackIcon = Icons.Default.Settings,
                systemIntentAction = Settings.ACTION_SETTINGS
            )
        )
        installedList.add(
            TvAppItem(
                id = "builtin_tv_store_gallery",
                packageName = "com.example.tvstore",
                arabicName = "دليل تطبيقات التلفاز",
                subtitle = "مشغلات الفيديو والمتصفحات والأدوات",
                categoryFilter = AppCategoryFilter.MEDIA,
                isPinned = false,
                isInstalledOnDevice = true,
                brandColor = Color(0xFF8B5CF6),
                fallbackIcon = Icons.Default.Tv,
                opensStoreHub = true
            )
        )

        Pair(pinnedApps, installedList.distinctBy { it.packageName })
    }

    private fun localizeCommonSystemAppName(rawLabel: String, pkg: String): String {
        val lower = rawLabel.lowercase()
        return when {
            pkg.contains("settings") || lower == "settings" -> "الإعدادات"
            pkg.contains("chrome") || lower.contains("chrome") -> "متصفح كروم"
            pkg.contains("youtube") -> "يوتيوب"
            pkg.contains("vending") || lower.contains("play store") -> "متجر جوجل بلاي"
            pkg.contains("documentsui") || lower == "files" -> "الملفات"
            pkg.contains("camera") || lower == "camera" -> "الكاميرا"
            pkg.contains("contacts") || lower == "contacts" -> "جهات الاتصال"
            pkg.contains("deskclock") || lower == "clock" -> "الساعة والمنبه"
            pkg.contains("calendar") || lower == "calendar" -> "التقويم"
            pkg.contains("calculator") || lower == "calculator" -> "الآلة الحاسبة"
            pkg.contains("gallery") || pkg.contains("photos") -> "المعرض والصور"
            pkg.contains("music") -> "الموسيقى"
            pkg.contains("webview") -> "مستعرض الويب"
            else -> rawLabel
        }
    }

    private fun pickBrandAccentForPackage(pkg: String): Color {
        val hash = kotlin.math.abs(pkg.hashCode())
        val palette = listOf(
            Color(0xFF38BDF8),
            Color(0xFF818CF8),
            Color(0xFF34D399),
            Color(0xFFF472B6),
            Color(0xFFFBBF24),
            Color(0xFFA78BFA),
            Color(0xFF2DD4BF)
        )
        return palette[hash % palette.size]
    }

    private fun drawableToImageBitmap(drawable: Drawable?): ImageBitmap? {
        if (drawable == null) return null
        return runCatching {
            val width = drawable.intrinsicWidth.coerceAtLeast(96).coerceAtMost(192)
            val height = drawable.intrinsicHeight.coerceAtLeast(96).coerceAtMost(192)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        }.getOrNull()
    }

    fun launchApp(
        context: Context,
        app: TvAppItem,
        onOpenStoreHub: () -> Unit,
        onStatusMessage: (String) -> Unit
    ) {
        if (app.opensStoreHub) {
            onOpenStoreHub()
            return
        }

        val pm: PackageManager = context.packageManager
        val animBundle = runCatching {
            ActivityOptions.makeCustomAnimation(
                context,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            ).toBundle()
        }.getOrNull()

        // 1. Try Leanback Launch Intent first, then standard Launch Intent
        val leanbackIntent = pm.getLeanbackLaunchIntentForPackage(app.packageName)
        val standardIntent = pm.getLaunchIntentForPackage(app.packageName)
        val targetIntent = leanbackIntent ?: standardIntent

        if (targetIntent != null) {
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching {
                context.startActivity(targetIntent, animBundle)
                onStatusMessage("جاري تشغيل ${app.arabicName}...")
                return
            }
        }

        // 2. Try explicit system intent action if configured (e.g., storage/settings)
        if (!app.systemIntentAction.isNullOrBlank()) {
            val sysIntent = Intent(app.systemIntentAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val launched = runCatching {
                context.startActivity(sysIntent, animBundle)
                true
            }.getOrDefault(false)
            if (launched) {
                onStatusMessage("تم فتح ${app.arabicName}")
                return
            }
        }

        // 3. Try fallback web URL via ACTION_VIEW
        if (!app.fallbackUrl.isNullOrBlank()) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(app.fallbackUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val launched = runCatching {
                context.startActivity(webIntent, animBundle)
                true
            }.getOrDefault(false)
            if (launched) {
                onStatusMessage("تم فتح ${app.arabicName} عبر المتصفح")
                return
            }
        }

        // 4. Open store hub if app is not installed on emulator
        onStatusMessage("التطبيق (${app.arabicName}) غير مثبت حالياً - تم فتح دليل أدوات التلفاز")
        onOpenStoreHub()
    }

    fun openExternalStoreOrWeb(
        context: Context,
        tool: EssentialStoreTool,
        onStatusMessage: (String) -> Unit
    ) {
        val pm = context.packageManager
        val existingIntent = pm.getLeanbackLaunchIntentForPackage(tool.packageName)
            ?: pm.getLaunchIntentForPackage(tool.packageName)

        if (existingIntent != null) {
            existingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            runCatching {
                context.startActivity(existingIntent)
                onStatusMessage("جاري تشغيل ${tool.arabicName}...")
                return
            }
        }

        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=${tool.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val openedMarket = runCatching {
            context.startActivity(marketIntent)
            true
        }.getOrDefault(false)

        if (openedMarket) {
            onStatusMessage("تم فتح صفحة ${tool.arabicName} في متجر التطبيقات")
            return
        }

        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tool.officialWebUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching {
            context.startActivity(webIntent)
            onStatusMessage("تم فتح الرابط الرسمي لتطبيق ${tool.arabicName}")
        }.onFailure {
            onStatusMessage("تعذر فتح المتصفح على هذا الجهاز")
        }
    }
}

@Composable
fun GlassAppCard(
    app: TvAppItem,
    isSelectedSpotlight: Boolean,
    tileScale: TileScaleOption,
    themeOption: LauncherThemeOption,
    onFocused: (TvAppItem) -> Unit,
    onClick: (TvAppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val activeHighlight = isFocused || isSelectedSpotlight

    LaunchedEffect(isFocused) {
        if (isFocused) {
            onFocused(app)
        }
    }

    // 1.1x D-Pad focus zoom/scale elevation animation as specified
    val targetScale = when {
        isPressed -> 0.96f
        activeHighlight -> 1.10f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "card_focus_scale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (activeHighlight) 18.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "card_focus_elevation"
    )

    val baseWidth = if (app.isPinned) 182.dp else 156.dp
    val baseHeight = if (app.isPinned) 132.dp else 124.dp
    val cardWidth = baseWidth * tileScale.scaleFactor
    val cardHeight = baseHeight * tileScale.scaleFactor
    val iconBoxSize = (if (app.isPinned) 54.dp else 48.dp) * tileScale.scaleFactor

    val borderStroke = if (activeHighlight) {
        BorderStroke(2.5.dp, FocusWhiteBorder)
    } else {
        BorderStroke(1.dp, FrostedCardBorder)
    }

    val cardBackgroundBrush = if (activeHighlight) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.25f),
                app.brandColor.copy(alpha = 0.28f),
                themeOption.cardGlassColor
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                themeOption.cardGlassColor,
                Color.White.copy(alpha = 0.06f)
            )
        )
    }

    Surface(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 10.dp)
            .width(cardWidth)
            .height(cardHeight)
            .scale(animatedScale)
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (activeHighlight) app.brandColor else Color.Black,
                spotColor = if (activeHighlight) FocusWhiteBorder else Color.Black
            )
            .clip(RoundedCornerShape(22.dp))
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onFocused(app)
                onClick(app)
            }
            .testTag("app_card_${app.id}"),
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = borderStroke
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardBackgroundBrush)
                .padding(14.dp)
        ) {
            // Subtle top-left status dot for installed/pinned state
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (activeHighlight) FocusWhiteBorder
                        else app.brandColor.copy(alpha = 0.85f)
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(iconBoxSize)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    app.brandColor.copy(alpha = 0.35f),
                                    app.brandColor.copy(alpha = 0.12f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.arabicName,
                            modifier = Modifier
                                .size(iconBoxSize * 0.76f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Icon(
                            imageVector = app.fallbackIcon,
                            contentDescription = app.arabicName,
                            tint = TextPrimarySnow,
                            modifier = Modifier.size(iconBoxSize * 0.58f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = app.arabicName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (activeHighlight) FontWeight.Bold else FontWeight.SemiBold,
                    color = TextPrimarySnow,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                if (app.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (activeHighlight) "اضغط للتشغيل" else app.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (activeHighlight) FocusWhiteBorder else TextSecondaryMist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
