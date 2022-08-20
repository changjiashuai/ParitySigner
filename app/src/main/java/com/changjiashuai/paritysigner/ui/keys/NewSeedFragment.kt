package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentNewSeedBinding
import com.changjiashuai.paritysigner.ext.showInfoSheet
import com.changjiashuai.paritysigner.viewmodel.NewSeedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.MNewSeedBackup
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/23 15:55.
 */
class NewSeedFragment : BaseFragment() {

    private val newSeedViewModel by viewModels<NewSeedViewModel>()
    private var _binding: FragmentNewSeedBinding? = null
    private val binding get() = _binding!!
    private val authentication = Authentication()
    private var backupPhraseSheetDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewSeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupViewModel()
    }

    private fun setupView() {
        binding.etSeed.doAfterTextChanged {
            binding.btnGenerate.isEnabled = !it?.toString().isNullOrEmpty()
        }
        binding.btnGenerate.setOnClickListener {
            val seedNames = newSeedViewModel.seedNames.value ?: emptyArray()
            val seedName = binding.etSeed.text?.toString()
            if (!seedNames.contains(seedName)) {
                newSeedViewModel.pushButton(Action.GO_FORWARD, seedName!!)
            } else {
                //This seed name already exists
                binding.tilSeed.error = "This seed name already exists"
            }
        }
    }

    private fun setupViewModel() {
        newSeedViewModel.pushButton(Action.NEW_SEED)
        newSeedViewModel.actionResult.observe(viewLifecycleOwner) {
            Log.i(TAG, "actionResult=$it")
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            //New Seed
            is ScreenData.Keys -> {
                backupPhraseSheetDialog?.dismiss()
                val bundle = bundleOf(Pair(SeedFragment.ARG_SEED_NAME, screenData.f.root.seedName))
                findNavController().navigate(R.id.action_newseed_to_details, bundle)
            }
            else -> {

            }
        }
    }

    override fun processModalData(modalData: ModalData) {
        when (modalData) {
            is ModalData.NewSeedMenu -> {

            }
            //New Seed step 2: backup phrase
            is ModalData.NewSeedBackup -> {
                val newSeedBackup = modalData.f
                backupPhraseSheetDialog = showBackupPhraseSheet(newSeedBackup)
            }
            else -> {

            }
        }
    }

    private fun showBackupPhraseSheet(mNewSeedBackup: MNewSeedBackup): BottomSheetDialog? {
        context?.let { context ->
            val backupPhraseSheet = View.inflate(context, R.layout.sheet_new_seed_backup, null)
            val tvSeedName = backupPhraseSheet.findViewById<TextView>(R.id.tv_seed_name)
            val tvSeedPhrase = backupPhraseSheet.findViewById<TextView>(R.id.tv_seed_phrase)
            val cbConfirm = backupPhraseSheet.findViewById<CheckBox>(R.id.cb_confirm)
            val cbCreateRoot = backupPhraseSheet.findViewById<CheckBox>(R.id.cb_create_root)
            val btnNext = backupPhraseSheet.findViewById<Button>(R.id.btn_next)
            tvSeedName.text = mNewSeedBackup.seed
            tvSeedPhrase.text = mNewSeedBackup.seedPhrase
            cbConfirm.setOnCheckedChangeListener { _, checked ->
                btnNext.isEnabled = checked
            }
            btnNext.setOnClickListener {
                if (cbConfirm.isChecked) {
                    activity?.let { activity ->
                        authentication.authenticate(activity) {
                            newSeedViewModel.addSeed(
                                seedName = mNewSeedBackup.seed,
                                seedPhrase = mNewSeedBackup.seedPhrase,
                                createRoots = cbCreateRoot.isChecked
                            )
                        }
                    }
                }
            }
            return context.showInfoSheet(backupPhraseSheet)
        }
        return null
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "NewSeedFragment"
    }
}