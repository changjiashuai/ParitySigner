package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentNewDeriveKeyBinding
import com.changjiashuai.paritysigner.viewmodel.NewDeriveKeyViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.DerivationDestination
import io.parity.signer.uniffi.MDeriveKey
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/30 19:57.
 */
class NewDeriveKeyFragment : BaseFragment() {

    private val newDeriveKeyViewModel by viewModels<NewDeriveKeyViewModel>()
    private var authentication: Authentication = Authentication()
    private var _binding: FragmentNewDeriveKeyBinding? = null
    private val binding get() = _binding!!
    private var isBackClick = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewDeriveKeyBinding.inflate(inflater, container, false)
        setupView()
        setupViewModel()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            isBackClick = true
            newDeriveKeyViewModel.pushButton(Action.GO_BACK)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
    }

    private fun setupViewModel() {
        newDeriveKeyViewModel.pushButton(Action.NEW_KEY)
        newDeriveKeyViewModel.actionResult.observe(viewLifecycleOwner) {
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
                // add key success
                if (!isBackClick) {
                    isBackClick = false
                    findNavController().navigateUp()
                }
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
                            activity?.let { activity ->
                                authentication.authenticate(activity) {
                                    newDeriveKeyViewModel.addKey(newKey, mDeriveKey.seedName)
                                }
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
                        context,
                        "has exist seedName:${address?.seedName}, path=${address?.path}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //TODO: don't know how to use???
//                derivationCheck.error
            }
        }
    }

    companion object {
        private const val TAG = "NewDeriveKeyFragment"
    }
}