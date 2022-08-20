package com.changjiashuai.paritysigner.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.adapter.MetadataAdapter
import com.changjiashuai.paritysigner.databinding.FragmentNetworkDetailsBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.showSheetStyle2
import com.changjiashuai.paritysigner.models.encodeHex
import com.changjiashuai.paritysigner.viewmodel.AbsViewModel
import io.parity.signer.uniffi.*

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/28 22:38.
 */
class NetworkDetailsFragment : BaseFragment() {

    private val networkDetailsViewModel by viewModels<AbsViewModel>()
    private var _binding: FragmentNetworkDetailsBinding? = null
    private val binding get() = _binding!!
    private var key: String = ""
    private val adapter = MetadataAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNetworkDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExtras()
        setupView()
        setupViewModel()
    }

    private fun setupExtras() {
        this.key = arguments?.getString(ManageNetworksFragment.ARG_KEY) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_manage_network, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            networkDetailsViewModel.doAction(Action.GO_BACK)
        } else if (item.itemId == R.id.action_network) {
            networkDetailsViewModel.doAction(Action.RIGHT_BUTTON_ACTION)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        binding.rvList.adapter = adapter
        adapter.onItemClick = {
            showMetadataSheet(it)
        }
    }

    private fun showMetadataSheet(mMetadataRecord: MMetadataRecord) {
        networkDetailsViewModel.doAction(Action.MANAGE_METADATA, mMetadataRecord.specsVersion)
        context?.showSheetStyle2(
            title = "Manage Metadata Used for: (${mMetadataRecord.specname})",
            actionText = "Sign this metadata",
            actionClick = {
                //screenData=SignSufficientCrypto
                val bundle = bundleOf(
                    Pair(
                        SignSufficientCryptoFragment.EXTRAS_ACTION,
                        Action.SIGN_METADATA.ordinal
                    )
                )
                findNavController().navigate(
                    R.id.action_networkDetails_to_sign_sufficient_crypto,
                    bundle
                )
            },
            action2Text = "Delete this metadata",
            action2Click = {
                context?.showAlert(
                    title = "Remove metadata?",
                    message = "This metadata will be removed for all networks",
                    cancelText = "Cancel",
                    showCancel = true,
                    confirmText = "Remove metadata",
                    confirmClick = {
                        //screenData=NNetworkDetails
                        networkDetailsViewModel.doAction(Action.REMOVE_METADATA)
                    }
                )
            },
            cancelText = "Cancel",
            cancelClick = {
                networkDetailsViewModel.doAction(Action.GO_BACK)
            }
        )
    }


    private fun setupViewModel() {
        networkDetailsViewModel.doAction(Action.MANAGE_NETWORKS)
        networkDetailsViewModel.doAction(Action.GO_FORWARD, key)
        networkDetailsViewModel.actionResult.observe(viewLifecycleOwner) {
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

    override fun processModalData(modalData: ModalData) {
        Log.i(TAG, "modalData=$modalData")
        when (modalData) {
            is ModalData.NetworkDetailsMenu -> {
                context?.showSheetStyle2(
                    title = "Manage network",
                    actionText = "Sign network specs",
                    actionClick = {
                        //go to SignSufficientCryptoActivity  -> Preview specs
                        val bundle = bundleOf(
                            Pair(
                                SignSufficientCryptoFragment.EXTRAS_ACTION,
                                Action.SIGN_NETWORK_SPECS.ordinal
                            )
                        )
                        findNavController().navigate(
                            R.id.action_networkDetails_to_sign_sufficient_crypto,
                            bundle
                        )
                    },
                    action2Text = "Delete network",
                    action2Click = {
                        context?.showAlert(
                            title = "Remove network?",
                            message = "This network will be removed for whole device",
                            showCancel = true,
                            cancelText = "Cancel",
                            cancelClick = {
                                networkDetailsViewModel.doAction(Action.GO_BACK)
                            },
                            confirmText = "Remove network",
                            confirmClick = {
                                // screenData=ManageNetworks
                                networkDetailsViewModel.doAction(Action.REMOVE_NETWORK)
                            }
                        )
                    },
                    cancelText = "Cancel",
                    cancelClick = {
                        networkDetailsViewModel.doAction(Action.GO_BACK)
                    }
                )
            }
            else -> {

            }
        }
    }

    override fun processAlertData(alertData: AlertData) {
        when (alertData) {
            is AlertData.ErrorData -> {
                val f = alertData.f
                Toast.makeText(context, "error=$f", Toast.LENGTH_LONG).show()
            }
            else -> {

            }
        }
    }

    companion object {
        private const val TAG = "NetworkDetailsFragment"
    }

}