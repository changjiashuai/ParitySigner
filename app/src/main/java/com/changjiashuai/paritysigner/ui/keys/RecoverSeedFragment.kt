package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentRecoverSeedBinding
import com.changjiashuai.paritysigner.viewmodel.RecoverSeedViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/23 22:27.
 */
class RecoverSeedFragment : BaseFragment() {

    private val recoverSeedViewModel by viewModels<RecoverSeedViewModel>()
    private var _binding: FragmentRecoverSeedBinding? = null
    private val binding get() = _binding!!
    private var seedName = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverSeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExtras()
        setupView()
        setupViewModel()
    }

    private fun setupExtras() {
        this.seedName = arguments?.getString("") ?: ""
    }

    private fun setupView() {
        binding.tvSubtitle.text = "Display Name"
        binding.etSeedName.setText(seedName)
        binding.etSeedName.doAfterTextChanged {
            val seedName = it?.toString()
            val seedNames = recoverSeedViewModel.seedNames.value
            binding.btnNext.isEnabled =
                !seedName.isNullOrEmpty() && seedNames?.contains(seedName) == false

        }
        binding.btnNext.setOnClickListener {
            val seedName = binding.etSeedName.text?.toString()
            val seedNames = recoverSeedViewModel.seedNames.value
            Log.i(TAG, "seedName=$seedName")

            if (seedName?.isNotEmpty() == true && seedNames?.contains(seedName) == false) {
                val bundle = bundleOf(Pair(ARG_SEED_NAME, seedName))
                findNavController().navigate(R.id.action_recoverseed_to_phrase, bundle)
            }
        }
    }

    private fun setupViewModel() {
        recoverSeedViewModel.pushButton(Action.RECOVER_SEED)
        recoverSeedViewModel.actionResult.observe(viewLifecycleOwner) {
            Log.i(TAG, "actionResult=$it")
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            //Recover Seed
            is ScreenData.RecoverSeedName -> {
            }
            else -> {

            }
        }
    }

    override fun processModalData(modalData: ModalData) {
        when (modalData) {
            else -> {

            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

//    override fun onAirPlaneModeChanged(isOn: Boolean) {
//        recoverSeedViewModel.getAlertState()
//    }

    companion object {
        private const val TAG = "RecoverSeedFragment"
        const val ARG_SEED_NAME = "seedName"
    }
}