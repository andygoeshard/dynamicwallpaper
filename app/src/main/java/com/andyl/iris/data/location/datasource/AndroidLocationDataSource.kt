package com.andyl.iris.data.location.datasource

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.andyl.iris.domain.model.GeoLocation
import com.andyl.iris.domain.repository.UserPreferencesRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
class AndroidLocationDataSource(
    context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): GeoLocation =
        suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geo = GeoLocation(location.latitude, location.longitude)
                        Log.d("LocationDS", "Ubicación GPS ok: ${geo.latitude}")
                        cont.resume(geo)
                    } else {
                        Log.w("LocationDS", "GPS null, buscando en SharedPreferences...")
                        val saved = kotlinx.coroutines.runBlocking {
                            preferencesRepository.getLastLocation()
                        }
                        cont.resume(saved ?: GeoLocation(-34.6037, -58.3816))
                    }
                }
                .addOnFailureListener {
                    val saved = kotlinx.coroutines.runBlocking { preferencesRepository.getLastLocation() }
                    cont.resume(saved ?: GeoLocation(-34.6037, -58.3816))
                }
        }
}