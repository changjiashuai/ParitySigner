package com.changjiashuai.paritysigner.ui.settings

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentSettingsBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import com.changjiashuai.paritysigner.viewmodel.SettingsViewModel
import io.parity.signer.uniffi.*

class SettingsFragment : BaseFragment() {

    private val settingsViewModel by viewModels<SettingsViewModel>()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val authentication = Authentication()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.doAction(Action.NAVBAR_SETTINGS)
    }

    private fun setupView() {
        binding.llAppearance.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_appearance)
        }
        binding.llNetworks.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_networks)
        }

        //Backup keys
        /**
         * button=GO_FORWARD,
         * details=01124ece5a06c3043461727fb8daa062d21776fb8187891a23252936caad689454,
         * seedPhrase=wedding burst bridge brush detail mail safe range paper across quick custom cave comfort unit guide script roast excite flash tape rare dance eight,
         */
        binding.llBackupKeys.setOnClickListener {
            if (context?.let { it1 -> AirPlaneUtils.getAlertState(it1) } == AlertState.None) {
                findNavController().navigate(R.id.action_settings_to_backup_seed)
            } else {
                settingsViewModel.doAction(Action.SHIELD)
            }
        }

        ///Verifier certificate
        /**
         * button=VIEW_GENERAL_VERIFIER, details=, seedPhrase
         */
        binding.llVerifierCertificate.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_verifier_certificate)
        }

        binding.llWipeSigner.setOnClickListener {
            context?.showAlert(
                title = "Wipe ALL data?",
                message = "Factory reset the Signer app. This operation can not be reverted!",
                showCancel = true,
                cancelText = "Cancel",
                cancelClick = {

                },
                confirmText = "Wipe",
                confirmClick = {
                    activity?.let { activity ->
                        authentication.authenticate(activity) {
                            settingsViewModel.wipeToFactory()
                        }
                    }
                }
            )
        }

        //About
        /**
         * button=SHOW_DOCUMENTS, details=, seedPhrase,
         */
        binding.llAbout.setOnClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
        }

        binding.tvProtectionValue.text = "${isStrongBoxProtected()}"
        binding.tvVersionValue.text = "${getAppVersion()}"
    }

    private fun setupViewModel() {
        settingsViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            is ScreenData.Settings -> {
                val mVerifierDetails = screenData.f
                binding.ivVerifier.setImageBitmap(mVerifierDetails.identicon?.toBitmap())
                binding.tvVerifierEncryption.text = "encryption: ${mVerifierDetails.encryption}"
            }
            else -> {

            }
        }
    }

    private fun isStrongBoxProtected(): Boolean {
        return PrefsUtils.isStrongBoxProtected()
    }

    private fun getAppVersion(): String? {
        return context?.packageName?.let {
            context?.packageManager?.getPackageInfo(
                it,
                0
            )?.versionName
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}