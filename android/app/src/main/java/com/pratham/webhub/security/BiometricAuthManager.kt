package com.pratham.webhub.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manages biometric authentication for Veyla.
 *
 * Provides device credential fallback and uses a crypto object to ensure
 * the authentication result is tied to a cryptographic operation.
 */
@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "BiometricAuthManager"
        private const val KEY_NAME = "webhub_biometric_key"
        private const val KEYSTORE_ALIAS = "AndroidKeyStore"
    }

    /**
     * Checks whether the device is capable of biometric or device-credential authentication.
     */
    fun canAuthenticate(): Boolean {
        val result = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Returns the [BiometricManager.AuthenticatorStatus] for more detailed checking.
     */
    fun getAuthenticatorStatus(): Int {
        return BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
    }

    /**
     * Generates or retrieves the secret key used for the crypto object.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_ALIAS).apply { load(null) }
        keyStore.getKey(KEY_NAME, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_ALIAS
        )
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the key if the user has enrolled a new biometric or
                // changed their device credential.
                .setInvalidatedByBiometricEnrollment(true)
                .build()
        )
        return keyGenerator.generateKey()
    }

    /**
     * Creates a [Cipher] initialized with the biometric secret key.
     */
    private fun createCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
        ).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }
    }

    /**
     * Launches the biometric / device-credential prompt and suspends until
     * the user authenticates (or cancels).
     *
     * @param activity  The hosting [FragmentActivity] required by [BiometricPrompt].
     * @param promptTitle Title shown on the prompt dialog.
     * @param promptSubtitle Optional subtitle.
     * @return `true` if authentication succeeded, `false` otherwise.
     */
    suspend fun authenticate(
        activity: FragmentActivity,
        promptTitle: String,
        promptSubtitle: String = ""
    ): Boolean = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            val executor = Executor { it.run() }
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(promptTitle)
                .setSubtitle(promptSubtitle.ifBlank { null })
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                            BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            try {
                val cipher = createCipher()
                val cryptoObject = BiometricPrompt.CryptoObject(cipher)

                val prompt = BiometricPrompt(activity, executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                        ) {
                            Log.d(TAG, "Biometric authentication succeeded")
                            if (continuation.isActive) continuation.resume(true)
                        }

                        override fun onAuthenticationFailed() {
                            Log.w(TAG, "Biometric authentication failed (invalid biometric)")
                            // Do NOT resume here – the user can retry.
                        }

                        override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                        ) {
                            Log.w(TAG, "Biometric auth error ($errorCode): $errString")
                            if (continuation.isActive) continuation.resume(false)
                        }
                    }
                )

                prompt.authenticate(promptInfo, cryptoObject)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing biometric prompt", e)
                if (continuation.isActive) continuation.resume(false)
            }
        }
    }
}
