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
import com.changjiashuai.paritysigner.adapter.NetworkAdapter
import com.changjiashuai.paritysigner.databinding.FragmentManageNetworksBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.showSheetStyle2
import com.changjiashuai.paritysigner.viewmodel.ManageNetworksViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.AlertData
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/27 21:49.
 */
class ManageNetworksFragment : BaseFragment() {

    private val manageNetworksViewModel by viewModels<ManageNetworksViewModel>()
    private val adapter = NetworkAdapter()
    private var _binding: FragmentManageNetworksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageNetworksBinding.inflate(inflater, container, false)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_manage_network, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            manageNetworksViewModel.pushButton(Action.GO_BACK)
            findNavController().navigate(R.id.settingsScreen)
        } else if (item.itemId == R.id.action_network) {
            //Manage Types
            manageNetworksViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        binding.rvList.adapter = adapter
        adapter.onItemClick = {
            val key = it.key
            val bundle = bundleOf(Pair(ARG_KEY, key))
            findNavController().navigate(R.id.action_network_to_details, bundle)
        }
    }

    private fun setupViewModel() {
        manageNetworksViewModel.pushButton(Action.MANAGE_NETWORKS)
        manageNetworksViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
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

    override fun processModalData(modalData: ModalData) {
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
                context?.showSheetStyle2(
                    title = "Manage Types $title",
                    actionText = "Sign Types",
                    actionClick = {
                        val bundle = bundleOf(
                            Pair(
                                SignSufficientCryptoFragment.EXTRAS_ACTION,
                                Action.SIGN_TYPES.ordinal
                            )
                        )
                        findNavController().navigate(
                            R.id.action_network_to_sign_sufficient_crypto,
                            bundle
                        )
                    },
                    action2Text = "Delete types",
                    action2Click = {
                        context?.showAlert(
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

    override fun processAlertData(alertData: AlertData) {
        if (alertData is AlertData.ErrorData) {
            Toast.makeText(context, "Error: ${alertData.f}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ManageNetworksFragment"
        const val ARG_KEY = "key"
    }
}