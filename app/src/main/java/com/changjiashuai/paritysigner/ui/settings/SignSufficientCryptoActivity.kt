package com.changjiashuai.paritysigner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.SeedBoxStatus
import com.changjiashuai.paritysigner.adapter.DerivationAdapter
import com.changjiashuai.paritysigner.adapter.SignSufficientCryptoAdapter
import com.changjiashuai.paritysigner.databinding.ActivitySignSufficientCryptoBinding
import com.changjiashuai.paritysigner.ext.showInfoSheet
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import com.changjiashuai.paritysigner.ui.keys.SeedDetailsFragment
import com.changjiashuai.paritysigner.viewmodel.SignSufficientCryptoViewModel
import io.parity.signer.uniffi.*

class SignSufficientCryptoActivity : BaseActivity() {

    private val signSufficientCryptoViewModel by viewModels<SignSufficientCryptoViewModel>()
    private lateinit var binding: ActivitySignSufficientCryptoBinding
    private val adapter = SignSufficientCryptoAdapter()
    private var action: Int? = null
    private val authentication = Authentication {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignSufficientCryptoBinding.inflate(layoutInflater)
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
        action = intent?.extras?.getInt(EXTRAS_ACTION)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.rvList.adapter = adapter
        adapter.onItemClick = {
            //TODO: signature ui
//            SignatureActivity.startActivity(this, it.seedName, it.addressKey, action)
            authentication.authenticate(this) {
                signSufficientCryptoViewModel.signSufficientCrypto(it.seedName, it.addressKey)
            }
        }
    }

    private fun setupViewModel() {
        if (action == Action.SIGN_METADATA.ordinal) {
            signSufficientCryptoViewModel.pushButton(Action.SIGN_METADATA)
        } else if (action == Action.SIGN_NETWORK_SPECS.ordinal) {
            signSufficientCryptoViewModel.pushButton(Action.SIGN_NETWORK_SPECS)
        } else if (action == Action.SIGN_TYPES.ordinal) {
            signSufficientCryptoViewModel.pushButton(Action.SIGN_TYPES)
        }
        signSufficientCryptoViewModel.actionResult.observe(this) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            is ScreenData.SignSufficientCrypto -> {
                val mSignSufficientCrypto = screenData.f
                val rawKeys = mSignSufficientCrypto.identities.sortedBy { it.path }
                adapter.submitList(rawKeys)
            }
            else -> {

            }
        }
    }

    private fun showSignatureSheet(mSufficientCryptoReady: MSufficientCryptoReady) {
        val view = View.inflate(this, R.layout.layout_signature, null)
        val signatureSheet = showInfoSheet(view)
        val ivClose = view.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener {
            signatureSheet.dismiss()
        }

        val ivQrCode = signatureSheet.findViewById<ImageView>(R.id.iv_qr_code)
        ivQrCode?.setImageBitmap(mSufficientCryptoReady.sufficient.toBitmap())

        val ivLogo = signatureSheet.findViewById<ImageView>(R.id.iv_logo)
        val tvSeedNamePath = signatureSheet.findViewById<TextView>(R.id.tv_seed_name_path)
        val tvPublicKey = signatureSheet.findViewById<TextView>(R.id.tv_public_key)
        val rlTypes = signatureSheet.findViewById<RelativeLayout>(R.id.rl_types)
        val rlSpecs = signatureSheet.findViewById<RelativeLayout>(R.id.rl_specs)
        val tvMetadata = signatureSheet.findViewById<TextView>(R.id.tv_metadata)

        val address = mSufficientCryptoReady.authorInfo
        ivLogo?.setImageBitmap(address.identicon.toBitmap())
        if (address.hasPwd) {
            tvSeedNamePath?.text =
                "${address.seedName}${address.path}///{Locked account Icon}"
        } else {
            tvSeedNamePath?.text = "${address.seedName}${address.path}"
        }
        tvPublicKey?.text = address.base58.abbreviateString(8)

        rlTypes?.visibility = View.GONE
        rlSpecs?.visibility = View.GONE
        tvMetadata?.visibility = View.GONE

        when (val c = mSufficientCryptoReady.content) {
            is MscContent.AddSpecs -> {
                //Action.SIGN_NETWORK_SPECS
                rlSpecs?.visibility = View.VISIBLE
                val ivSpecsLogo = signatureSheet.findViewById<ImageView>(R.id.iv_specs_logo)
                val tvSpecsName = signatureSheet.findViewById<TextView>(R.id.tv_specs_name)
                when (c.f.networkLogo) {
                    "polkadot" -> {
                        ivSpecsLogo?.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
                    }
                    "kusama" -> {
                        ivSpecsLogo?.setImageResource(R.drawable.ic_kusama_ksm_logo)
                    }
                    "westend" -> {
                        ivSpecsLogo?.setImageResource(R.drawable.ic_polkadot_dot_logo)
                    }
                }
                tvSpecsName?.text = c.f.networkTitle
            }
            is MscContent.LoadMetadata -> {
                //Action.SIGN_METADATA
                tvMetadata?.visibility = View.VISIBLE
                tvMetadata?.text =
                    "Metadata for name: " + c.name + " with version: " + c.version
            }
            is MscContent.LoadTypes -> {
                //Action.SIGN_TYPES
                rlTypes?.visibility = View.VISIBLE
                val ivTypesLogo = signatureSheet.findViewById<ImageView>(R.id.iv_types_logo)
                val tvTypesName = signatureSheet.findViewById<TextView>(R.id.tv_types_name)
                ivTypesLogo?.setImageBitmap(c.pic.toBitmap())
                tvTypesName?.text = c.types
            }
        }
    }

    override fun processModalData(modalData: ModalData?) {
        when (modalData) {
            is ModalData.SufficientCryptoReady -> {
                val mSufficientCryptoReady = modalData.f
                showSignatureSheet(mSufficientCryptoReady)
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

    override fun finish() {
        signSufficientCryptoViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }

    companion object {

        private const val TAG = "SignSufficientCryptoActivity"
        private const val EXTRAS_ACTION = "action"

        fun startActivity(context: Context, action: Int) {
            val intent = Intent(context, SignSufficientCryptoActivity::class.java).apply {
                putExtra(EXTRAS_ACTION, action)
            }
            context.startActivity(intent)
        }
    }
}