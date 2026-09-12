package com.max4real.compose_boilerplate.shared.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class InternetConnectionStatus {
    Connected,
    Connecting,
    WaitingForConnection
}

@Singleton
class InternetConnectionService @Inject constructor(
    @ApplicationContext context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _status = MutableStateFlow(currentStatus())
    val status: StateFlow<InternetConnectionStatus> = _status.asStateFlow()

    val isConnected: Boolean
        get() = currentStatus() == InternetConnectionStatus.Connected

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateStatus()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            updateStatus(networkCapabilities)
        }

        override fun onLost(network: Network) {
            updateStatus()
        }

        override fun onUnavailable() {
            _status.value = InternetConnectionStatus.WaitingForConnection
        }
    }

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun refresh() {
        updateStatus()
    }

    private fun updateStatus(capabilities: NetworkCapabilities? = null) {
        _status.value = if (capabilities != null) {
            statusFromCapabilities(capabilities)
        } else {
            currentStatus()
        }
    }

    private fun currentStatus(): InternetConnectionStatus {
        val activeNetwork = connectivityManager.activeNetwork
            ?: return InternetConnectionStatus.WaitingForConnection

        return statusFromCapabilities(connectivityManager.getNetworkCapabilities(activeNetwork))
    }

    private fun statusFromCapabilities(
        capabilities: NetworkCapabilities?
    ): InternetConnectionStatus {
        if (capabilities == null) {
            return InternetConnectionStatus.WaitingForConnection
        }

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val hasNetworkTransport =
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

        return if (hasInternet && isValidated) {
            InternetConnectionStatus.Connected
        } else if (hasNetworkTransport) {
            InternetConnectionStatus.Connecting
        } else {
            InternetConnectionStatus.WaitingForConnection
        }
    }
}
