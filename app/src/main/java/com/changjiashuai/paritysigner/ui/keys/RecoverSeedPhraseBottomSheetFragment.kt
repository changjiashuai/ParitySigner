package com.changjiashuai.paritysigner.ui.keys

import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.viewmodel.SeedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.MRecoverSeedPhrase
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/3 15:35.
 */
class RecoverSeedPhraseBottomSheetFragment : BottomSheetDialogFragment() {

    private var etPhrase: EditText? = null
    private var cgSeedPhrase: ChipGroup? = null
    private val recoverSeedPhraseViewModel by viewModels<SeedViewModel>()
    var mRecoverSeedPhrase: MRecoverSeedPhrase? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheetInternal =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheetInternal?.minimumHeight = Resources.getSystem().displayMetrics.heightPixels
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recover_seed_phrase, container, false)
        etPhrase = view.findViewById(R.id.et_phrase)
        cgSeedPhrase = view.findViewById(R.id.cg_seed_phrase)
        etPhrase?.doAfterTextChanged {
            val phrase = it?.toString()
            if (!phrase.isNullOrEmpty()) {
                recoverSeedPhraseViewModel.doAction(Action.TEXT_ENTRY, phrase)
            }
        }
        if (mRecoverSeedPhrase?.guessSet != null) {
            updateSeedPhrase(mRecoverSeedPhrase?.guessSet!!)
        }

        setupViewModel()
        return view
    }

    private fun updateSeedPhrase(guessPhrases: List<String>) {
        guessPhrases.forEach {
            val phrase = it
            val chip =
                layoutInflater.inflate(R.layout.list_item_seed_phrase, cgSeedPhrase, false) as Chip
            chip.text = phrase
            chip.isCloseIconVisible = true
            chip.setOnClickListener {
                Toast.makeText(context, "click=$phrase", Toast.LENGTH_SHORT).show()
            }
            cgSeedPhrase?.addView(chip)
        }
    }

    private fun setupViewModel() {
        recoverSeedPhraseViewModel.actionResult?.observe(viewLifecycleOwner) {
            processScreenData(it.screenData)
        }
    }

    private fun processScreenData(screenData: ScreenData) {
        if (screenData is ScreenData.RecoverSeedPhrase) {
            updateSeedPhrase(screenData.f.guessSet)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        recoverSeedPhraseViewModel.doAction(Action.GO_BACK)
    }

    companion object {
        const val TAG = "RecoverSeedPhraseBottomSheetFragment"

        fun newInstance() = RecoverSeedPhraseBottomSheetFragment()
    }
}