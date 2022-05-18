package com.linecorp.id.research.biometricdemo.util

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.linecorp.id.research.biometricdemo.data.EncryptedMessage
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object CryptographyUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val SECRET_KEY_NAME = "AndroidBiometricDemoKeyName"
    private const val KEY_SIZE = 128
    private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    private fun getOrCreateSecretKey(keyName: String): SecretKey {
        //Get keystore
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed

        //Get secret key based on keyName
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        //Generate keygen parameter
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true) //For biometric
        }

        //Generate keystore
        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)

        //Return new secret key
        return keyGenerator.generateKey()
    }

    fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    //region Encrypt
    fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(SECRET_KEY_NAME)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        return cipher
    }

    fun encryptData(plaintext: String, cipher: Cipher): EncryptedMessage {
        val cipherText = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return EncryptedMessage(cipherText, cipher.iv)
    }
    //endregion

    //region Decrypt
    fun getInitializedCipherForDecryption(
        initializationVector: ByteArray? = null
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(SECRET_KEY_NAME)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(KEY_SIZE, initializationVector)
        )

        return cipher
    }

    fun decryptData(cipherText: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(cipherText)
        return String(plaintext, Charsets.UTF_8)
    }
    //endregion
}
