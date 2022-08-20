package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
import com.changjiashuai.paritysigner.databinding.FragmentRecoverSeedPhraseBinding
import com.changjiashuai.paritysigner.viewmodel.SeedViewModel
import com.google.android.material.chip.Chip
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/24 13:17.
 */
class RecoverSeedPhraseFragment : BaseFragment() {

    private val recoverSeedPhraseViewModel by viewModels<SeedViewModel>()
    private var _binding: FragmentRecoverSeedPhraseBinding? = null
    private val binding get() = _binding!!
    private val authentication = Authentication()
    private var seedName = ""
    private var phrases = listOf<String>()
    private var readySeed: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoverSeedPhraseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExtras()
        setupView()
        setupViewModel()
    }

    private fun setupExtras() {
        this.seedName = arguments?.getString(RecoverSeedFragment.ARG_SEED_NAME) ?: ""
    }

    private fun setupView() {
        binding.tvSeedName.text = "Seed Name: $seedName"
        binding.tvSeedPhrase.text = "Seed Phrase: "
        binding.etPhrase.doAfterTextChanged {
            val phrase = it?.toString()
            if (!phrase.isNullOrEmpty()) {
                recoverSeedPhraseViewModel.doAction(Action.TEXT_ENTRY, phrase)
            }
        }
        binding.etPhrase.setOnKeyListener { _, _, keyEvent ->
            if (keyEvent.keyCode == KeyEvent.KEYCODE_DEL && keyEvent.action == KeyEvent.ACTION_DOWN) {
                val phrase = binding.etPhrase.text
                if (phrase.isNullOrEmpty()) {
                    recoverSeedPhraseViewModel.doAction(Action.TEXT_ENTRY)
                }
            }
            return@setOnKeyListener false
        }
        binding.btnNext.setOnClickListener {
            activity?.let { activity ->
                authentication.authenticate(activity) {
                    if (!readySeed.isNullOrEmpty()) {
                        val createRootKeys = binding.cbCreateRoot.isChecked
                        recoverSeedPhraseViewModel.addSeed(seedName, readySeed!!, createRootKeys)
                    }
                }
            }
        }
    }

    private fun updateSeedPhrase(guessPhrases: List<String>) {
        binding.cgSeedPhrase.removeAllViews()
        guessPhrases.forEach {
            val phrase = it
            val chip = layoutInflater.inflate(
                R.layout.list_item_seed_phrase,
                binding.cgSeedPhrase,
                false
            ) as Chip
            chip.text = phrase
            chip.isCloseIconVisible = true
            chip.setOnClickListener {
                recoverSeedPhraseViewModel.doAction(Action.PUSH_WORD, phrase)
                binding.etPhrase.setText("")
            }
            binding.cgSeedPhrase.addView(chip)
        }
    }

    private fun setupViewModel() {
        recoverSeedPhraseViewModel.doAction(Action.GO_FORWARD, seedName)
        recoverSeedPhraseViewModel.actionResult.observe(viewLifecycleOwner) {
            Log.i(TAG, "actionResult=$it")
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            //Recover Seed Phrase
            is ScreenData.RecoverSeedPhrase -> {
                updateSeedPhrase(screenData.f.guessSet)
                phrases = screenData.f.draft
                binding.tvPhrase.text = phrases.joinToString(" ")
                this.readySeed = screenData.f.readySeed
                binding.btnNext.isEnabled = this.readySeed != null
            }
            is ScreenData.Keys -> {
                //go to seed details
                val bundle = bundleOf(Pair(SeedFragment.ARG_SEED_NAME, screenData.f.root.seedName))
                findNavController().navigate(R.id.action_newseed_to_details, bundle)
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
//        recoverSeedPhraseViewModel.getAlertState()
//    }

    companion object {
        private const val TAG = "RecoverSeedPhraseFragment"
    }
}