package com.changjiashuai.paritysigner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.databinding.ActivitySignatureBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import com.changjiashuai.paritysigner.viewmodel.SignatureViewModel
import io.parity.signer.uniffi.*

class SignatureActivity : BaseActivity() {

    private val signatureViewModel by viewModels<SignatureViewModel>()
    private val authentication = Authentication {}
    private lateinit var binding: ActivitySignatureBinding
    private var seedName: String = ""
    private var addressKey: String = ""
    private var action: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupExtras()
        setupView()
        setupViewModel()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupExtras() {
        seedName = intent?.getStringExtra(EXTRAS_SEED_NAME) ?: ""
        addressKey = intent?.getStringExtra(EXTRAS_ADDRESS_KEY) ?: ""
        action = intent?.extras?.getInt(EXTRAS_ACTION)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        authentication.authenticate(this) {
            signatureViewModel.signSufficientCrypto(seedName, addressKey)
        }
    }

    private fun setupViewModel() {
        signatureViewModel.actionResult.observe(this) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            else -> {

            }
        }

    }

    override fun processModalData(modalData: ModalData?) {
        Log.i(TAG, "modalData=$modalData")
        when (modalData) {
            is ModalData.SufficientCryptoReady -> {
                val mSufficientCryptoReady = modalData.f
                binding.ivQrCode.setImageBitmap(mSufficientCryptoReady.sufficient.toBitmap())

                val address = mSufficientCryptoReady.authorInfo
                binding.ivLogo.setImageBitmap(address.identicon.toBitmap())
                if (address.hasPwd) {
                    binding.tvSeedNamePath.text =
                        "${address.seedName}${address.path}///{Locked account Icon}"
                } else {
                    binding.tvSeedNamePath.text = "${address.seedName}${address.path}"
                }
                binding.tvPublicKey.text = address.base58.abbreviateString(8)

                when (val c = mSufficientCryptoReady.content) {
                    is MscContent.AddSpecs -> {
                        //Action.SIGN_NETWORK_SPECS
//                        Text("Specs")
//                        NetworkCard(c.f)
                    }
                    is MscContent.LoadMetadata -> {
                        //Action.SIGN_METADATA
                        binding.tvVersion.text =
                            "Metadata for " + c.name + " with version " + c.version
                    }
                    is MscContent.LoadTypes -> {
                        //Action.SIGN_TYPES
//                        Text("types " + c.types)
//                        Identicon(identicon = c.pic)
                    }
                }
            }
            else -> {

            }
        }
    }

    override fun processAlertData(alertData: AlertData?) {
        when (alertData) {
            is AlertData.ErrorData -> {
                val f = alertData.f
                Toast.makeText(this, "error=$f", Toast.LENGTH_LONG).show()
            }
            else -> {

            }
        }
    }

    companion object {

        private const val TAG = "SignatureActivity"
        private const val EXTRAS_SEED_NAME = "seedName"
        private const val EXTRAS_ADDRESS_KEY = "addressKey"
        private const val EXTRAS_ACTION = "action"

        fun startActivity(
            context: Context,
            seedName: String,
            addressKey: String,
            action: Int?
        ) {
            val intent = Intent(context, SignatureActivity::class.java).apply {
                putExtra(EXTRAS_SEED_NAME, seedName)
                putExtra(EXTRAS_ADDRESS_KEY, addressKey)
                putExtra(EXTRAS_ACTION, action)
            }
            context.startActivity(intent)
        }
    }
}