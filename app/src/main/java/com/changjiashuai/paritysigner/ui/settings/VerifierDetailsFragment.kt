package com.changjiashuai.paritysigner.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.databinding.FragmentVerifierDetailsBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.viewmodel.VerifierDetailsViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/30 18:28.
 */
class VerifierDetailsFragment : BaseFragment() {

    private val verifierDetailsViewModel by viewModels<VerifierDetailsViewModel>()
    private var _binding: FragmentVerifierDetailsBinding? = null
    private val binding get() = _binding!!
    private val authentication = Authentication()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifierDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            verifierDetailsViewModel.pushButton(Action.GO_BACK)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
    }

    private fun setupViewModel() {
        verifierDetailsViewModel.pushButton(Action.VIEW_GENERAL_VERIFIER)
        verifierDetailsViewModel.actionResult.observe(viewLifecycleOwner) {
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
                context?.showAlert(
                    title = "Wipe ALL data?",
                    message = "Remove all data and set general verifier blank so that it could be set later. This operation can not be reverted. Do not proceed unless you absolutely know what you are doing, there is no need to use this procedure in most cases. Misusing this feature may lead to loss of funds!",
                    showCancel = true,
                    cancelText = "Cancel",
                    cancelClick = {

                    },
                    confirmText = "I understand",
                    confirmClick = {
                        activity?.let { activity ->
                            authentication.authenticate(activity) {
                                verifierDetailsViewModel.wipeToJailbreak()
                            }
                        }
                    }
                )
            }
        }
    }

    companion object {
        private const val TAG = "VerifierDetailsFragment"
    }
}