package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentKeyDetailsBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.showWarnSheet
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import com.changjiashuai.paritysigner.viewmodel.KeyDetailsViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/17 14:00.
 */
class KeyDetailsFragment : BaseFragment() {

    private val keyDetailsViewModel by viewModels<KeyDetailsViewModel>()
    private var _binding: FragmentKeyDetailsBinding? = null
    private val binding get() = _binding!!
    private var type: Int = SeedDetailsFragment.KEY_TYPE_SEED
    private var addressKey: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeyDetailsBinding.inflate(inflater, container, false)
        setupExtras()
        setupView()
        setupViewModel()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    private fun setupExtras() {
        this.type =
            arguments?.getInt(SeedDetailsFragment.EXTRAS_TYPE, SeedDetailsFragment.KEY_TYPE_SEED)
                ?: SeedDetailsFragment.KEY_TYPE_SEED
        this.addressKey = arguments?.getString(SeedDetailsFragment.EXTRAS_ADDRESS_KEY, "") ?: ""
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_seed, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            keyDetailsViewModel.pushButton(Action.GO_BACK)
        } else if (item.itemId == R.id.action_seed) {
            keyDetailsViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        if (type == SeedDetailsFragment.KEY_TYPE_SEED) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Seed key"
        } else if (type == SeedDetailsFragment.KEY_TYPE_DERIVED) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Derived key"
        }
    }

    private fun setupViewModel() {
        keyDetailsViewModel.pushButton(Action.SELECT_KEY, addressKey)
        keyDetailsViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            //After remove key success.
            is ScreenData.Log -> {
                //fixme: switch to log tab ???
                findNavController().navigate(R.id.logScreen)
            }
            is ScreenData.KeyDetails -> {
                val keyDetails = screenData.f
                keyDetails.address.seedName
                binding.ivLogo.setImageBitmap(keyDetails.address.identicon.toBitmap())
                if (type == SeedDetailsFragment.KEY_TYPE_SEED) {
                    binding.tvRootSeed.text = keyDetails.address.seedName
                } else if (type == SeedDetailsFragment.KEY_TYPE_DERIVED) {
                    binding.tvRootSeed.text = keyDetails.address.seedName + keyDetails.address.path
                }
                binding.tvRootAddressKey.text = keyDetails.address.base58.abbreviateString(8)

                when (keyDetails.networkInfo.networkLogo) {
                    "polkadot" -> {
                        binding.ivNetworkLogo.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
                    }
                    "kusama" -> {
                        binding.ivNetworkLogo.setImageResource(R.drawable.ic_kusama_ksm_logo)
                    }
                    "westend" -> {
                        binding.ivNetworkLogo.setImageResource(R.drawable.ic_polkadot_dot_logo)
                    }
                }
                binding.tvNetworkTitle.text = keyDetails.networkInfo.networkTitle
                binding.ivNetworkArrowDown.visibility = View.GONE

                binding.ivQrCode.setImageBitmap(keyDetails.qr.toBitmap())

                binding.tvBase58.text = keyDetails.address.base58
                binding.tvHexKey.text = keyDetails.pubkey
                binding.tvSeedName.text = "Seed name: ${keyDetails.address.seedName}"
            }
            else -> {

            }
        }

    }

    override fun processModalData(modalData: ModalData) {
        Log.i(TAG, "modalData=$modalData")
        if (modalData is ModalData.KeyDetailsAction) {
            context?.showWarnSheet(
                title = "Key Menu",
                actionText = "Forget this key forever",
                actionClick = {
                    it.dismiss()
                    context?.showAlert(
                        title = "Forget this key?",
                        message = "This key will be removed for this network. Are you sure?",
                        showCancel = true,
                        cancelText = "Cancel",
                        cancelClick = {
                            keyDetailsViewModel.pushButton(Action.GO_BACK)
                        },
                        confirmText = "Remove key",
                        confirmClick = {
                            keyDetailsViewModel.pushButton(Action.REMOVE_KEY)
                        }
                    )
                },
                onDismissListener = {
                    keyDetailsViewModel.pushButton(Action.GO_BACK)
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "KeyDetailsFragment"
    }
}