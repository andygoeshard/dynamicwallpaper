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
import com.andyl.iris.ui.components.CyberpunkBox
import com.andyl.iris.ui.components.CyberpunkLoadingBar
import com.andyl.iris.ui.components.IrisLogo
import com.andyl.iris.ui.components.PackSelectorSection
import com.andyl.iris.ui.components.PremiumUpsellSheet
import com.andyl.iris.ui.components.TemperatureRuleSection
import com.andyl.iris.ui.components.RatingDialog
import com.andyl.iris.ui.components.WeatherSection
import com.andyl.iris.ui.event.WallpaperEvent
import com.andyl.iris.ui.theme.LocalReduceAnimations
import com.andyl.iris.ui.viewmodel.DynamicWallpaperViewModel
import com.andyl.iris.domain.repository.PremiumRepository
import com.andyl.iris.billing.BillingManager
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicWallpaperScreen(
    viewModel: DynamicWallpaperViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToSearch: (com.andyl.iris.domain.model.Weather?, com.andyl.iris.domain.model.TimeOfDay?, String?, String?, String?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val premiumRepository = koinInject<PremiumRepository>()
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showUpsellSheet by remember { mutableStateOf(false) }
    var isTemperatureExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    val soundManager = koinInject<com.andyl.iris.ui.sound.SoundManager>()

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

                        Button(
                            onClick = {
                                if (state.hapticsEnabled) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                if (state.soundEnabled) {
                                    soundManager.playConfirm()
                                }
                                viewModel.onEvent(WallpaperEvent.OnApplyWallpaper)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
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
    ) { padding ->
        if (state.isLoading && state.rules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                CyberpunkLoadingBar(
                    progress = null,
                    label = "INITIALIZING CORE..."
                )
            }
        } else {
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
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 16.dp,
                                bottom = 120.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { BoxContainer { DaySelectionSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch) } }
                            item { BoxContainer { WeatherSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch) } }
                            item { BoxContainer { FixedTimeSection(state = state, onEvent = { viewModel.onEvent(it) }, onNavigateToSearch = onNavigateToSearch) } }
                            item {
                                BoxContainer {
                                    TemperatureRuleSection(
                                        state = state,
                                        isPremium = premiumRepository.isPremium(),
                                        isExpanded = isTemperatureExpanded,
                                        onToggleExpand = { isTemperatureExpanded = !isTemperatureExpanded },
                                        onEvent = { viewModel.onEvent(it) },
                                        onNavigateToSearch = onNavigateToSearch,
                                        onUpsellClick = { showUpsellSheet = true }
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

    if (showUpsellSheet) {
        val billingManager = koinInject<BillingManager>()
        PremiumUpsellSheet(
            billingManager = billingManager,
            onDismiss = { showUpsellSheet = false }
        )
    }
}
