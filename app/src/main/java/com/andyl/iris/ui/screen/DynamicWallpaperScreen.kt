package com.andyl.iris.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.andyl.iris.R
import com.andyl.iris.ui.components.AppWallpaperBackground
import com.andyl.iris.ui.components.DaySelectionSection
import com.andyl.iris.ui.components.FixedTimeSection
import com.andyl.iris.ui.components.BoxContainer
import com.andyl.iris.ui.components.CollapsibleSectionCard
import com.andyl.iris.ui.components.SectionBuildMode
import com.andyl.iris.ui.components.CyberpunkBox
import com.andyl.iris.ui.components.ApplyFeedbackOverlay
import com.andyl.iris.ui.components.IrisLogo
import com.andyl.iris.ui.components.LoadingScreen
import com.andyl.iris.ui.components.PackSelectorSection
import com.andyl.iris.ui.components.PremiumUpsellSheet
import com.andyl.iris.ui.components.TemperatureRuleSection
import com.andyl.iris.ui.components.RatingDialog
import com.andyl.iris.ui.components.WeatherSection
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.theme.LocalReduceAnimations
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.andyl.iris.billing.BillingManager
import org.koin.compose.koinInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DynamicWallpaperScreen(
    viewModel: DynamicWallpaperViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: (com.andyl.iris.domain.model.Weather?, com.andyl.iris.domain.model.TimeOfDay?, String?, String?, String?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val premiumRepository = koinInject<PremiumRepository>()
    val userPreferencesRepository = koinInject<UserPreferencesRepository>()
    val scope = rememberCoroutineScope()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showUpsellSheet by remember { mutableStateOf(false) }
    var showTransition by remember { mutableStateOf(false) }
    var homeSections by remember { mutableStateOf<List<String>>(emptyList()) }
    var buildMode by remember { mutableStateOf(false) }
    val homeListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = koinInject<com.andyl.iris.ui.sound.SoundManager>()

    LaunchedEffect(Unit) {
        homeSections = userPreferencesRepository.getHomeSectionsOrder()
    }

    LaunchedEffect(state.error, state.successMessage) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(WallpaperEvent.ClearMessages)
        }
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(WallpaperEvent.ClearMessages)
        }
    }

    LaunchedEffect(state.pendingUndoUri) {
        state.pendingUndoUri?.let {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.undo_message),
                actionLabel = context.getString(R.string.undo_action),
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onEvent(WallpaperEvent.OnUndoWallpaper)
            } else {
                viewModel.onEvent(WallpaperEvent.OnClearPendingUndo)
            }
        }
    }


    LaunchedEffect(Unit) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showPermissionDialog = true
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.alarm_permission_title)) },
            text = { Text(stringResource(R.string.alarm_permission_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    viewModel.onEvent(WallpaperEvent.RequestExactAlarmPermission(context))
                }) { Text(stringResource(R.string.alarm_permission_btn_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text(stringResource(R.string.alarm_permission_btn_deny)) }
            }
        )
    }

    if (state.showFirstTimeDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = stringResource(R.string.first_apply_title),
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Text(text = stringResource(R.string.first_apply_text))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showTransition = true
                        viewModel.onEvent(WallpaperEvent.OnConfirmFirstTime)
                        viewModel.onEvent(WallpaperEvent.OnApplyWallpaper)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.first_apply_confirm), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(WallpaperEvent.OnDismissFirstTimeDialog)
                    }
                ) {
                    Text(stringResource(R.string.first_apply_cancel))
                }
            }
        )
    }

    if (state.showRatingDialog) {
        RatingDialog(
            onDismiss = { viewModel.onEvent(WallpaperEvent.OnDismissRatingDialog) },
            onRate = { stars -> viewModel.onEvent(WallpaperEvent.OnRateApp(stars)) }
        )
    }

    AppWallpaperBackground(
        wallpaperUri = state.lastAppliedWallpaper,
        enabled = state.useWallpaperBackground
    ) {
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val wallpaperBg = state.useWallpaperBackground
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (wallpaperBg) Color.Transparent else MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (wallpaperBg) glassSurfaceColor() else Color.Transparent,
                        border = if (wallpaperBg) glassBorder() else null
                    ) {
                        IrisLogo(
                            modifier = Modifier
                                .size(80.dp)
                                .padding(8.dp)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.top_bar_name), fontWeight = FontWeight.Black) },
                actions = {
                    TopBarIconButton(
                        glass = wallpaperBg,
                        onClick = { onNavigateToSearch(null, null, null, null, null) },
                        icon = Icons.Default.Search,
                        contentDescription = "Explorar fondos"
                    )

                    TopBarIconButton(
                        glass = wallpaperBg,
                        onClick = { viewModel.onEvent(WallpaperEvent.OnAddNewPack) },
                        icon = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.add_new_pack)
                    )
                    TopBarIconButton(
                        glass = wallpaperBg,
                        onClick = onNavigateToSettings,
                        icon = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cfg_screen_img_settings_title)
                    )
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                ) {
                    CyberpunkBox(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .fillMaxWidth(),
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        useRounded = true
                    ) {
                        val btnShape = RoundedCornerShape(16.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (state.hapticsEnabled) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    if (state.soundEnabled) {
                                        soundManager.playConfirm()
                                    }
                                    showTransition = true
                                    viewModel.onEvent(WallpaperEvent.OnApplyWallpaper)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = btnShape,
                                enabled = !state.isLoading
                            ) {
                                if (state.isLoading) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(stringResource(R.string.applying_changes), fontWeight = FontWeight.ExtraBold)
                                    }
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(if(state.editingPackId != state.activePackId)stringResource(R.string.activate_package) else stringResource(R.string.apply_wallpaper), fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
            ) {
                PackSelectorSection(
                    state = state,
                    premiumRepository = premiumRepository,
                    onEvent = { viewModel.onEvent(it) },
                    onUpsellClick = { showUpsellSheet = true }
                )

                val reduceAnimations = LocalReduceAnimations.current

                AnimatedContent(
                    targetState = state.editingPackId,
                    transitionSpec = {
                        if (reduceAnimations) {
                            (fadeIn(tween(0)) togetherWith fadeOut(tween(0))).using(SizeTransform(clip = false))
                        } else if (state.slideDirection > 0) {
                            (slideInHorizontally { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()) togetherWith
                                    (slideOutHorizontally { it } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = stringResource(R.string.pack_content_transition),
                    modifier = Modifier.weight(1f)
                ) { id ->
                    key(id) {
                        LazyColumn(
                            state = homeListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 120.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(homeSections, key = { it }) { sectionId ->
                                val enterBuildMode = { buildMode = true }
                                val sectionContent: @Composable () -> Unit = when (sectionId) {
                                    "day" -> ({
                                        DaySelectionSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch)
                                    })
                                    "weather" -> ({
                                        WeatherSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch)
                                    })
                                    "fixed" -> ({
                                        FixedTimeSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch)
                                    })
                                    else -> ({
                                        TemperatureRuleSection(
                                            state = state,
                                            isPremium = premiumRepository.isPremium(),
                                            isExpanded = true,
                                            onToggleExpand = {},
                                            onEvent = { viewModel.onEvent(it) },
                                            onNavigateToSearch = onNavigateToSearch,
                                            onUpsellClick = { showUpsellSheet = true }
                                        )
                                    })
                                }
                                val sectionTitle = when (sectionId) {
                                    "day" -> stringResource(R.string.weekCalendar)
                                    "weather" -> stringResource(R.string.weather_section_config)
                                    "fixed" -> stringResource(R.string.fixed_time_section_title)
                                    else -> stringResource(R.string.temp_by_weather)
                                }
                                val sectionSubtitle = when (sectionId) {
                                    "weather" -> stringResource(R.string.weather_section_tap_config)
                                    "fixed" -> stringResource(R.string.fixed_time_section_subtitle)
                                    else -> stringResource(R.string.temp_by_weather_desc)
                                }

                                CollapsibleSectionCard(
                                    sectionId = sectionId,
                                    title = sectionTitle,
                                    subtitle = sectionSubtitle,
                                    onLongClick = enterBuildMode
                                ) {
                                    sectionContent()
                                }
                            }
                        }
                    }
                }
            }
    }
    }

    if (showUpsellSheet) {
        val billingManager = koinInject<BillingManager>()
        PremiumUpsellSheet(
            billingManager = billingManager,
            onDismiss = { showUpsellSheet = false }
        )
    }

    if (showTransition && !LocalReduceAnimations.current) {
        ApplyFeedbackOverlay(
            onFinished = { showTransition = false }
        )
    }

    if (buildMode) {
        SectionBuildMode(
            sections = homeSections,
            titles = mapOf(
                "day" to stringResource(R.string.weekCalendar),
                "weather" to stringResource(R.string.weather_section_config),
                "fixed" to stringResource(R.string.fixed_time_section_title),
                "temperature" to stringResource(R.string.temp_by_weather)
            ),
            subtitles = mapOf(
                "weather" to stringResource(R.string.weather_section_tap_config),
                "fixed" to stringResource(R.string.fixed_time_section_subtitle),
                "temperature" to stringResource(R.string.temp_by_weather_desc)
            ),
            sectionContent = { sectionId ->
                when (sectionId) {
                    "day" -> DaySelectionSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch)
                    "weather" -> WeatherSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch)
                    "fixed" -> FixedTimeSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch)
                    else -> TemperatureRuleSection(
                        state = state,
                        isPremium = premiumRepository.isPremium(),
                        isExpanded = true,
                        onToggleExpand = {},
                        onEvent = { viewModel.onEvent(it) },
                        onNavigateToSearch = onNavigateToSearch,
                        onUpsellClick = { showUpsellSheet = true }
                    )
                }
            },
            onReorder = { newOrder ->
                homeSections = newOrder
                scope.launch { userPreferencesRepository.setHomeSectionsOrder(newOrder) }
            },
            onExit = { buildMode = false }
        )
    }

    var startupLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        snapshotFlow { state.isLoading }
            .filter { !it }
            .first()
        delay(700)
        startupLoading = false
    }

    if (startupLoading) {
        LoadingScreen(label = "INITIALIZING CORE...")
    }
}
