package com.changjiashuai.paritysigner.ui.keys

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ActivityNewDeriveKeyBinding
import com.changjiashuai.paritysigner.viewmodel.NewDeriveKeyViewModel
import io.parity.signer.uniffi.*

class NewDeriveKeyActivity : BaseActivity() {

    private lateinit var binding: ActivityNewDeriveKeyBinding
    private val newDeriveKeyViewModel by viewModels<NewDeriveKeyViewModel>()
    private var authentication: Authentication = Authentication(setAuth = { })


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewDeriveKeyBinding.inflate(layoutInflater)
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
        //needBack = intent?.getBooleanExtra(EXTRAS_NEED_GO_BACK, false) ?: false
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        newDeriveKeyViewModel.pushButton(Action.NEW_KEY)
    }

    private fun setupViewModel() {
        newDeriveKeyViewModel.actionResult.observe(this) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            is ScreenData.DeriveKey -> {
                val mDeriveKey = screenData.f
                showAddKeyUi(mDeriveKey)
            }
            is ScreenData.Keys -> {
                finish()
            }
            else -> {

            }
        }

    }

    private fun showAddKeyUi(mDeriveKey: MDeriveKey) {
        binding.tvSubtitle.text = "For seed ${mDeriveKey.seedName}"
        when (mDeriveKey.networkLogo) {
            "polkadot" -> {
                binding.ivLogo.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
            }
            "kusama" -> {
                binding.ivLogo.setImageResource(R.drawable.ic_kusama_ksm_logo)
            }
            "westend" -> {
                binding.ivLogo.setImageResource(R.drawable.ic_polkadot_dot_logo)
            }
        }
        binding.tvNetworkTitle.text = mDeriveKey.networkTitle
        binding.tilKey.prefixText = mDeriveKey.seedName
        binding.etKeyPath.doAfterTextChanged {
            if (it?.toString()?.startsWith("/") == false) {
                binding.tilKey.error = "must start with /"
                binding.btnNext.isEnabled = false
            } else {
                val newKey = binding.tilKey.prefixText.toString() + it?.toString()
//                val derivationCheck = signerDataModel.checkPath(
                val derivationCheck = newDeriveKeyViewModel.checkPath(
                    mDeriveKey.seedName,
                    newKey,
                    mDeriveKey.networkSpecsKey
                )
                if (derivationCheck.collision != null) {
                    binding.btnNext.isEnabled = false
                    binding.tilKey.error = "$newKey has exists"
                } else {
                    binding.tilKey.error = null
                    binding.btnNext.isEnabled = true
                }
                binding.btnNext.isEnabled = !it?.toString().isNullOrEmpty()
            }

        }
        binding.btnNext.setOnClickListener {
            val newKey = binding.etKeyPath.text?.toString()
            Log.i(TAG, "newKey=$newKey")

            if (newKey?.isNotEmpty() == true && newKey.startsWith("/")) {
//                val derivationCheck = signerDataModel.checkPath(
                val derivationCheck = newDeriveKeyViewModel.checkPath(
                    mDeriveKey.seedName,
                    newKey,
                    mDeriveKey.networkSpecsKey
                )

                Log.i(TAG, "derivationCheck=$derivationCheck")
                if (derivationCheck.buttonGood) {
                    when (derivationCheck.whereTo) {
                        DerivationDestination.PIN -> {
//                            signerDataModel.addKey(newKey, mDeriveKey.seedName)
                            authentication.authenticate(this) {
                                newDeriveKeyViewModel.addKey(newKey, mDeriveKey.seedName)
                            }
                        }
                        DerivationDestination.PWD -> {
//                            signerDataModel.pushButton(Action.CHECK_PASSWORD, newKey)
                            newDeriveKeyViewModel.pushButton(Action.CHECK_PASSWORD, newKey)
                        }
                        null -> {}
                    }
                } else {
                    //冲突地址
                    val address = derivationCheck.collision
                    Toast.makeText(
                        this,
                        "has exist seedName:${address?.seedName}, path=${address?.path}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //TODO: don't know how to use???
//                derivationCheck.error
            }
        }
    }

    override fun finish() {
        newDeriveKeyViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }

    companion object {
        private const val TAG = "NewDeriveKeyActivity"
        private const val EXTRAS_NEED_GO_BACK = "needGoBack"

        fun startActivity(context: Context, needGoBack: Boolean = false) {
            val intent = Intent(context, NewDeriveKeyActivity::class.java).apply {
                putExtra(EXTRAS_NEED_GO_BACK, needGoBack)
            }
            context.startActivity(intent)
        }
    }
}