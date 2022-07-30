package com.changjiashuai.paritysigner.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.adapter.NetworkAdapter
import com.changjiashuai.paritysigner.databinding.ActivityManageNetworksBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.showSheetStyle1
import com.changjiashuai.paritysigner.ext.showSheetStyle2
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.ui.HomeActivity
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.viewmodel.ManageNetworksViewModel
import io.parity.signer.uniffi.*

class ManageNetworksActivity : BaseActivity() {

    private val manageNetworksViewModel by viewModels<ManageNetworksViewModel>()
    private lateinit var binding: ActivityManageNetworksBinding
    private val adapter = NetworkAdapter()
    private var menuStatus: MenuItem? = null
    private var isMenuStatusClick = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageNetworksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    override fun onResume() {
        super.onResume()
        manageNetworksViewModel.pushButton(Action.MANAGE_NETWORKS)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_network, menu)
        menuStatus = menu?.findItem(R.id.action_status)
        setupViewModel()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.action_network) {
            showTypeSheet()
        } else if (item.itemId == R.id.action_status) {
//            isMenuStatusClick = true
//            manageNetworksViewModel.getAlertState()
            val alertState = AirPlaneUtils.getAlertState(this)
            checkNetworkState(alertState)
            showShieldAlertIfNeed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showTypeSheet() {
        manageNetworksViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.rvList.adapter = adapter
        //Manage Types
//        Action.RIGHT_BUTTON_ACTION
        adapter.onItemClick = {
            val key = it.key
            NetworkDetailsActivity.startActivity(this, key)
        }
    }

    private fun setupViewModel() {
//        manageNetworksViewModel.getAlertState()
//        manageNetworksViewModel.alertState.observe(this) {
//            checkNetworkState(it)
//            if (isMenuStatusClick) {
//                isMenuStatusClick = false
//                showShieldAlert(it)
//            }
//        }
        val alertState = AirPlaneUtils.getAlertState(this)
        checkNetworkState(alertState)
        showShieldAlertIfNeed()
        manageNetworksViewModel.actionResult.observe(this) {
            processActionResult(it)
        }
    }

    private fun checkNetworkState(alertState: AlertState) {
        when (alertState) {
            AlertState.Active -> {
                //Network connected
                menuStatus?.setIcon(R.drawable.ic_gpp_bad_24)
            }
            AlertState.None -> {
                //Signer is secure
                menuStatus?.setIcon(R.drawable.ic_verified_user_24)
            }
            AlertState.Past -> {
                //Network was connected
                menuStatus?.setIcon(R.drawable.ic_gpp_maybe_24)
            }
            else -> {
                //Network detector failure
                menuStatus?.setIcon(R.drawable.ic_baseline_security_24)
            }

        }
    }


    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            //MmNetwork 1. sign types  2. Delete types
            is ScreenData.ManageNetworks -> {
                val networks = screenData.f.networks
                adapter.submitList(networks)
            }
            else -> {

            }
        }

    }

    override fun processModalData(modalData: ModalData?) {
        Log.i(TAG, "modalData=$modalData")
        when (modalData) {
            is ModalData.TypesInfo -> {
                val typeInfo = modalData.f
                var title = ""
                if (typeInfo.typesOnFile) {
//                    Row {
//                        Identicon(identicon = typesInfo.typesIdPic?:listOf())
//                        Text(typesInfo.typesHash ?: "")
//                    }
//                    title = "${typeInfo.typesIdPic?: listOf()}"
                    title = typeInfo.typesHash ?: ""
                } else {
                    title = "Pre-v14 types not installed"
                }
                showSheetStyle2(
                    title = "Manage Types $title",
                    actionText = "Sign Types",
                    actionClick = {
//                        signerDataModel.pushButton(Action.SIGN_TYPES)
                        SignSufficientCryptoActivity.startActivity(this, Action.SIGN_TYPES.ordinal)
                    },
                    action2Text = "Delete types",
                    action2Click = {
                        showAlert(
                            title = "Remove types?",
                            message = "Types information needed for support of pre-v14 metadata will be removed. Are you sure?",
                            showCancel = true,
                            cancelText = "Cancel",
                            cancelClick = {
                                manageNetworksViewModel.pushButton(Action.GO_BACK)
                            },
                            confirmText = "Remove types",
                            confirmClick = {
                                //alertData=ErrorData(f=Could not find types information.)
                                manageNetworksViewModel.pushButton(Action.REMOVE_TYPES)
                            }
                        )
                    },
                    cancelText = "Cancel",
                    cancelClick = {
                        manageNetworksViewModel.pushButton(Action.GO_BACK)
                    }
                )
            }
            else -> {

            }
        }
    }

    override fun processAlertData(alertData: AlertData?) {
        if (alertData is AlertData.ErrorData) {
            Toast.makeText(this, "Error: ${alertData.f}", Toast.LENGTH_SHORT).show()
        }
    }

//    override fun onAirPlaneModeChanged(isOn: Boolean) {
//        manageNetworksViewModel.getAlertState()
//    }

    override fun finish() {
        manageNetworksViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }

    companion object {
        private const val TAG = "ManageNetworksActivity"
    }
}