package com.changjiashuai.paritysigner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.databinding.ActivityVerifierDetailsBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.viewmodel.VerifierDetailsViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ScreenData

class VerifierDetailsActivity : BaseActivity() {

    private val verifierDetailsViewModel by viewModels<VerifierDetailsViewModel>()
    private lateinit var binding: ActivityVerifierDetailsBinding
    private val authentication = Authentication {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifierDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    private fun setupViewModel() {
        verifierDetailsViewModel.pushButton(Action.VIEW_GENERAL_VERIFIER)
        verifierDetailsViewModel.actionResult.observe(this) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        super.processScreenData(screenData)
        Log.i(TAG, "screenData=$screenData")
        if (screenData is ScreenData.VVerifier) {
            val mVerifierDetails = screenData.f
            binding.ivLogo.setImageBitmap(mVerifierDetails.identicon.toBitmap())
            binding.tvPublicKey.text = mVerifierDetails.publicKey
            binding.tvEncryption.text = "encryption: ${mVerifierDetails.encryption}"
            binding.btnRemove.setOnClickListener {
                showAlert(
                    title = "Wipe ALL data?",
                    message = "Remove all data and set general verifier blank so that it could be set later. This operation can not be reverted. Do not proceed unless you absolutely know what you are doing, there is no need to use this procedure in most cases. Misusing this feature may lead to loss of funds!",
                    showCancel = true,
                    cancelText = "Cancel",
                    cancelClick = {

                    },
                    confirmText = "I understand",
                    confirmClick = {
                        authentication.authenticate(this) {
                            verifierDetailsViewModel.wipeToJailbreak()
                        }
                    }
                )
            }
        }
    }

    override fun finish() {
        verifierDetailsViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }

    companion object {

        private const val TAG = "VerifierDetailsActivity"

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, VerifierDetailsActivity::class.java))
        }
    }
}