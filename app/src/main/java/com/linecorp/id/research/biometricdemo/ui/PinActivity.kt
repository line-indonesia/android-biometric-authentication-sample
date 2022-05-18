package com.linecorp.id.research.biometricdemo.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.linecorp.id.research.biometricdemo.R
import com.linecorp.id.research.biometricdemo.data.EncryptedMessage
import com.linecorp.id.research.biometricdemo.databinding.ActivityPinBinding
import com.linecorp.id.research.biometricdemo.util.BiometricUtil
import com.linecorp.id.research.biometricdemo.util.CryptographyUtil
import com.linecorp.id.research.biometricdemo.util.PreferenceUtil
import javax.crypto.Cipher

class PinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinBinding
    private val encryptBiometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            binding.textViewBiometricsResult.text = resources.getString(
                R.string.authentication_error,
                errorCode,
                errString.toString()
            )
        }

        override fun onAuthenticationFailed() {
            binding.textViewBiometricsResult.setText(R.string.authentication_failed)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            result.cryptoObject?.cipher?.let {
                val message = binding.editTextPin.text.toString().trim()
                encryptAndSave(message, it)
                binding.textViewBiometricsResult.setText(R.string.authentication_success)
            }
        }
    }

    private val decryptBiometricCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            binding.textViewPin.text = resources.getString(
                R.string.authentication_error,
                errorCode,
                errString.toString()
            )
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            binding.textViewPin.setText(R.string.authentication_failed)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)

            result.cryptoObject?.cipher?.let {
                val message = getAndDecrypt(it)
                binding.textViewPin.text = message
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initListeners()
    }

    private fun initListeners() {
        binding.buttonSave.setOnClickListener {
            if (binding.editTextPin.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Pin must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showBiometricPromptToEncrypt()
        }

        binding.buttonGetPin.setOnClickListener {
            val encryptedMessage = PreferenceUtil.getEncryptedMessage(applicationContext, PIN_KEY)
            if (encryptedMessage == null) {
                Toast.makeText(this, "Pin not found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showBiometricPromptToDecrypt(encryptedMessage)
        }
    }

    //region Encrypt
    private fun showBiometricPromptToEncrypt() {
        //Create crypto object
        val cryptoObject = BiometricPrompt.CryptoObject(
            CryptographyUtil.getInitializedCipherForEncryption()
        )

        BiometricUtil.showBiometricPrompt(
            activity = this,
            listener = encryptBiometricCallback,
            cryptoObject = cryptoObject
        )
    }

    private fun encryptAndSave(plainTextMessage: String, cipher: Cipher) {
        val encryptedMessage = CryptographyUtil.encryptData(plainTextMessage, cipher)

        //Save to shared preference
        PreferenceUtil.storeEncryptedMessage(
            applicationContext,
            prefKey = PIN_KEY,
            encryptedMessage = encryptedMessage
        )
    }
    //endregion

    //region Decrypt
    private fun showBiometricPromptToDecrypt(encryptedMessage: EncryptedMessage) {
        //Create crypto object
        val cryptoObject = BiometricPrompt.CryptoObject(
            CryptographyUtil.getInitializedCipherForDecryption(
                encryptedMessage.initializationVector
            )
        )

        BiometricUtil.showBiometricPrompt(
            activity = this,
            listener = decryptBiometricCallback,
            cryptoObject = cryptoObject
        )
    }

    private fun getAndDecrypt(cipher: Cipher): String {
        val encryptedMessage = PreferenceUtil.getEncryptedMessage(
            applicationContext,
            PIN_KEY
        ) ?: return ""

        return CryptographyUtil.decryptData(
            encryptedMessage.cipherText,
            cipher
        )
    }
    //endregion

    companion object {
        private const val PIN_KEY = "pin_key"
    }
}
