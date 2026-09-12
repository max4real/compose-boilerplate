package com.max4real.compose_boilerplate.shared.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.max4real.compose_boilerplate.shared.util.mylog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    @Volatile
    private var prefs: SharedPreferences? = null

    fun saveTokens(
        accessToken: String,
        refreshToken: String
    ) {
        runCatching {
            prefs().edit {
                putString(ACCESS_TOKEN, accessToken)
                putString(REFRESH_TOKEN, refreshToken)
            }
        }.onFailure {
            recoverEncryptedPrefs(it)
            prefs().edit {
                putString(ACCESS_TOKEN, accessToken)
                putString(REFRESH_TOKEN, refreshToken)
            }
        }
    }

    fun saveFCMTokens(
        accessToken: String,
    ) {
        runCatching {
            prefs().edit {
                putString(FCM_TOKEN, accessToken)
            }
        }.onFailure {
            recoverEncryptedPrefs(it)
            prefs().edit {
                putString(FCM_TOKEN, accessToken)
            }
        }
    }

    fun getAccessToken(): String? {
        return getToken(ACCESS_TOKEN)
    }

    fun getRefreshToken(): String? {
        return getToken(REFRESH_TOKEN)
    }

    fun getFCMToken(): String? {
        return getToken(FCM_TOKEN)
    }

    fun hasTokens(): Boolean {
        return !getAccessToken().isNullOrBlank() &&
                !getRefreshToken().isNullOrBlank()
    }

    fun clearTokens() {
        runCatching {
            prefs().edit {
                remove(ACCESS_TOKEN)
                remove(REFRESH_TOKEN)
                remove(FCM_TOKEN)
            }
        }
    }

    /**
     * Snapshots the currently live tokens into [accountId]'s vault, so they survive
     * being overwritten by [saveTokens] when a different account signs in.
     */
    fun snapshotCurrentTokensFor(accountId: String) {
        val accessToken = getAccessToken()
        val refreshToken = getRefreshToken()
        if (!accessToken.isNullOrBlank() && !refreshToken.isNullOrBlank()) {
            saveTokensFor(accountId, accessToken, refreshToken)
        }
    }

    /** Makes [accountId]'s vaulted tokens the live tokens. Returns false if none were stored. */
    fun activateStoredTokensFor(accountId: String): Boolean {
        val accessToken = getToken(accountKey(ACCESS_TOKEN, accountId))
        val refreshToken = getToken(accountKey(REFRESH_TOKEN, accountId))
        if (accessToken.isNullOrBlank() || refreshToken.isNullOrBlank()) return false

        saveTokens(accessToken, refreshToken)
        return true
    }

    fun hasStoredTokensFor(accountId: String): Boolean {
        return !getToken(accountKey(ACCESS_TOKEN, accountId)).isNullOrBlank() &&
                !getToken(accountKey(REFRESH_TOKEN, accountId)).isNullOrBlank()
    }

    fun clearStoredTokensFor(accountId: String) {
        runCatching {
            prefs().edit {
                remove(accountKey(ACCESS_TOKEN, accountId))
                remove(accountKey(REFRESH_TOKEN, accountId))
            }
        }
    }

    private fun saveTokensFor(
        accountId: String,
        accessToken: String,
        refreshToken: String
    ) {
        runCatching {
            prefs().edit {
                putString(accountKey(ACCESS_TOKEN, accountId), accessToken)
                putString(accountKey(REFRESH_TOKEN, accountId), refreshToken)
            }
        }.onFailure {
            recoverEncryptedPrefs(it)
            prefs().edit {
                putString(accountKey(ACCESS_TOKEN, accountId), accessToken)
                putString(accountKey(REFRESH_TOKEN, accountId), refreshToken)
            }
        }
    }

    private fun accountKey(base: String, accountId: String): String {
        return "${base}_$accountId"
    }

    private fun getToken(key: String): String? {
        return runCatching {
            prefs().getString(key, null)
        }.getOrElse {
            recoverEncryptedPrefs(it)
            null
        }
    }

    private fun prefs(): SharedPreferences {
        prefs?.let { return it }

        return synchronized(this) {
            prefs ?: createEncryptedPrefs().also { prefs = it }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        return runCatching {
            encryptedPrefs()
        }.getOrElse {
            recoverEncryptedPrefs(it)
            encryptedPrefs()
        }
    }

    private fun encryptedPrefs(): SharedPreferences {
        val masterKey = createMasterKey()

        return EncryptedSharedPreferences.create(
            appContext,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun recoverEncryptedPrefs(error: Throwable) {
        mylog("Resetting encrypted token storage after crypto failure. $error")
        prefs = null
        appContext.deleteSharedPreferences(PREF_NAME)

        runCatching {
            KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
                if (containsAlias(MasterKey.DEFAULT_MASTER_KEY_ALIAS)) {
                    deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                }
            }
        }
    }

    private fun createMasterKey(): MasterKey {
        return MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREF_NAME = "secure_prefs"
        private const val ACCESS_TOKEN = "access_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val FCM_TOKEN = "fcm_token"

    }
}
