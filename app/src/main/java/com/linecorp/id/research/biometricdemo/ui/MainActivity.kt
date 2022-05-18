package com.linecorp.id.research.biometricdemo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.linecorp.id.research.biometricdemo.R
import com.linecorp.id.research.biometricdemo.databinding.ActivityMainBinding
import com.linecorp.id.research.biometricdemo.util.BiometricUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val biometricCallback = object : BiometricPrompt.AuthenticationCallback() {
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
            binding.textViewBiometricsResult.setText(R.string.authentication_success)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showBiometricLoginOption()
        initListeners()
    }

    private fun initListeners() {
        binding.buttonBiometricsLogin.setOnClickListener {
            BiometricUtil.showBiometricPrompt(activity = this, listener = biometricCallback)
        }

        binding.buttonBiometricsWithCryptography.setOnClickListener {
            startActivity(Intent(this, PinActivity::class.java))
        }
    }

    private fun showBiometricLoginOption() {
        if (BiometricUtil.isBiometricReady(this)) {
            binding.buttonBiometricsLogin.visibility = View.VISIBLE
            binding.buttonBiometricsWithCryptography.visibility = View.VISIBLE
            binding.textViewNoBiometricsLogin.visibility = View.GONE
        } else {
            binding.buttonBiometricsLogin.visibility = View.GONE
            binding.buttonBiometricsWithCryptography.visibility = View.GONE
            binding.textViewNoBiometricsLogin.visibility = View.VISIBLE
        }
    }
}
