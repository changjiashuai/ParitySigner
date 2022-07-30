package com.changjiashuai.paritysigner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.adapter.MetadataAdapter
import com.changjiashuai.paritysigner.databinding.ActivityNetworkDetailsBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.showSheetStyle2
import com.changjiashuai.paritysigner.models.encodeHex
import com.changjiashuai.paritysigner.viewmodel.NetworkDetailsViewModel
import io.parity.signer.uniffi.*

class NetworkDetailsActivity : BaseActivity() {

    private val networkDetailsViewModel by viewModels<NetworkDetailsViewModel>()
    private lateinit var binding: ActivityNetworkDetailsBinding
    private var key: String = ""
    private val adapter = MetadataAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNetworkDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupExtras()
        setupView()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        networkDetailsViewModel.pushButton(Action.MANAGE_NETWORKS)
        networkDetailsViewModel.pushButton(Action.GO_FORWARD, key)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_network, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.action_network) {
            showNetworkSheet()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupExtras() {
        this.key = intent?.getStringExtra(EXTRA_KEY) ?: ""
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.rvList.adapter = adapter
        adapter.onItemClick = {
            //TODO: manage metadata
            showMetadataSheet(it)
        }
    }

    private fun showNetworkSheet() {
        networkDetailsViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
    }

    private fun showMetadataSheet(mMetadataRecord: MMetadataRecord) {
        networkDetailsViewModel.pushButton(Action.MANAGE_METADATA, mMetadataRecord.specsVersion)
        showSheetStyle2(
            title = "Manage Metadata Used for: (${mMetadataRecord.specname})",
            actionText = "Sign this metadata",
            actionClick = {
                //screenData=SignSufficientCrypto
                //TODO: go to sign sufficient crypto list
//                signerDataModel.pushButton(Action.SIGN_METADATA)
                SignSufficientCryptoActivity.startActivity(this, Action.SIGN_METADATA.ordinal)
            },
            action2Text = "Delete this metadata",
            action2Click = {
                showAlert(
                    title = "Remove metadata?",
                    message = "This metadata will be removed for all networks",
                    cancelText = "Cancel",
                    showCancel = true,
                    confirmText = "Remove metadata",
                    confirmClick = {
                        //screenData=NNetworkDetails
                        networkDetailsViewModel.pushButton(Action.REMOVE_METADATA)
                    }
                )
            },
            cancelText = "Cancel",
            cancelClick = {
                networkDetailsViewModel.pushButton(Action.GO_BACK)
            }
        )
    }

    private fun setupViewModel() {
        networkDetailsViewModel.actionResult.observe(this) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            //MmNetwork 1. sign network specs 2. Delete network
            is ScreenData.NNetworkDetails -> {
                val mNetworkDetails = screenData.f

                val logo = mNetworkDetails.logo
                val name = mNetworkDetails.name

                val base58Prefix = mNetworkDetails.base58prefix
                val decimals = mNetworkDetails.decimals
                val unit = mNetworkDetails.unit
                val genesisHash =
                    mNetworkDetails.genesisHash.toUByteArray().toByteArray().encodeHex()
                val mVerifier = mNetworkDetails.currentVerifier
                val type = mVerifier.ttype
                val details = mVerifier.details

                binding.tvNetworkTitle.text = "Network name: $name"
                binding.tvBase58Prefix.text = "base58 prefix: $base58Prefix"
                binding.tvDecimals.text = "decimals: $decimals"
                binding.tvUnit.text = "unit: $unit"
                binding.tvGenesisHash.text = "genesis hash: $genesisHash"
                binding.tvVerifierCertificate.text = "Verifier certificate: $type"

                binding.tvMetadata.text = "Metadata available:"

                //op: 1. sign this metadata
                //    2. delete this metadata
                val metas = mNetworkDetails.meta
                adapter.submitList(metas)
            }
            is ScreenData.SignSufficientCrypto -> {

            }
            else -> {

            }
        }

    }

    override fun processModalData(modalData: ModalData?) {
        Log.i(TAG, "modalData=$modalData")
        when (modalData) {
            is ModalData.NetworkDetailsMenu -> {
                showSheetStyle2(
                    title = "Manage network",
                    actionText = "Sign network specs",
                    actionClick = {
                        //go to SignSufficientCryptoActivity  -> Preview specs
//                        signerDataModel.pushButton(Action.SIGN_NETWORK_SPECS)
                        SignSufficientCryptoActivity.startActivity(
                            this,
                            Action.SIGN_NETWORK_SPECS.ordinal
                        )
                    },
                    action2Text = "Delete network",
                    action2Click = {
                        showAlert(
                            title = "Remove network?",
                            message = "This network will be removed for whole device",
                            showCancel = true,
                            cancelText = "Cancel",
                            cancelClick = {
                                networkDetailsViewModel.pushButton(Action.GO_BACK)
                            },
                            confirmText = "Remove network",
                            confirmClick = {
                                // screenData=ManageNetworks
                                networkDetailsViewModel.pushButton(Action.REMOVE_NETWORK)
                            }
                        )
                    },
                    cancelText = "Cancel",
                    cancelClick = {
                        networkDetailsViewModel.pushButton(Action.GO_BACK)
                    }
                )
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
        networkDetailsViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }

    companion object {

        private const val TAG = "NetworkDetailsActivity"
        private const val EXTRA_KEY = "key"

        fun startActivity(context: Context, key: String) {
            val intent = Intent(context, NetworkDetailsActivity::class.java).apply {
                putExtra(EXTRA_KEY, key)
            }
            context.startActivity(intent)
        }
    }
}