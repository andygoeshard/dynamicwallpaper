package com.andyl.iris.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.andyl.iris.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

/**
 * Encapsulates the Google Play compliant flow for requesting
 * ACCESS_BACKGROUND_LOCATION: a prominent disclosure dialog must be shown and
 * acknowledged before the permission can be requested.
 *
 * Returns a lambda `(force: Boolean) -> Unit`:
 * - If the disclosure has not been acknowledged yet, it shows the dialog
 *   (regardless of `force`). Only after explicit consent is the permission
 *   requested.
 * - If it has been acknowledged and `force = true`, it requests the permission
 *   directly (e.g. from a settings button). With `force = false` it does nothing
 *   to avoid nagging on every app launch.
 */
@Composable
fun rememberBackgroundLocationConsent(
    userPreferences: UserPreferencesRepository,
    onGranted: (Boolean) -> Unit = {}
): (force: Boolean) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDisclosure by remember { mutableStateOf(false) }

    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onGranted(isGranted)
    }

    fun hasFine(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun hasBackground(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun request(force: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !hasFine() || hasBackground()) return
        scope.launch {
            if (userPreferences.hasBackgroundLocationDisclosureAcknowledged()) {
                if (force) {
                    backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            } else {
                showDisclosure = true
            }
        }
    }

    if (showDisclosure) {
        BackgroundLocationDisclosureDialog(
            onAccept = {
                showDisclosure = false
                scope.launch { userPreferences.setBackgroundLocationDisclosureAcknowledged() }
                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            },
            onDeny = {
                showDisclosure = false
                scope.launch { userPreferences.setBackgroundLocationDisclosureAcknowledged() }
            }
        )
    }

    return ::request
}