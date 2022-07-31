package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.adapter.SeedAdapter
import com.changjiashuai.paritysigner.databinding.FragmentSeedBinding
import com.changjiashuai.paritysigner.ext.showSheetStyle1
import com.changjiashuai.paritysigner.ext.showSheetStyle2
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.viewmodel.SeedViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.parity.signer.uniffi.*


class SeedFragment : BaseFragment() {

    private val seedViewModel by viewModels<SeedViewModel>()
    private var _binding: FragmentSeedBinding? = null
    private val binding get() = _binding!!
    private val adapter = SeedAdapter()

    private var recoverSeedPhraseBottomSheetFragment: RecoverSeedPhraseBottomSheetFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeedBinding.inflate(inflater, container, false)
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
        seedViewModel.pushButton(Action.NAVBAR_KEYS)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_keys, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add) {
            seedViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        binding.rvList.adapter = adapter
        adapter.onItemClick = {
            val bundle = bundleOf(Pair(ARG_SEED_NAME, it.seedName))
            findNavController().navigate(R.id.action_seed_to_details, bundle)
        }
    }

    private var seedSheetDialog: BottomSheetDialog? = null

    /**
     * New Seed
     *
     * RIGHT_BUTTON_ACTION -> NEW_SEED
     */
    private fun showAddSeedDialog() {
        //fixme: from new Need back lead to twice show ???
        if (seedSheetDialog?.isShowing == true) {
            seedSheetDialog?.dismiss()
            return
        }
        seedSheetDialog = context?.showSheetStyle2(
            title = "Add Seed(Select seed addition method)",
            actionText = "New seed",
            actionClick = {
                if (context?.let { AirPlaneUtils.getAlertState(it) } == AlertState.None) {
                    findNavController().navigate(R.id.action_seed_to_new)
                } else {
                    seedViewModel.pushButton(Action.SHIELD)
                }
            },
            action2Text = "Recover seed",
            action2Click = {
                if (context?.let { AirPlaneUtils.getAlertState(it) } == AlertState.None) {
                    findNavController().navigate(R.id.action_seed_to_recover)
                } else {
                    seedViewModel.pushButton(Action.SHIELD)
                }
            },
            cancelText = "Cancel",
            cancelClick = {
                seedViewModel.pushButton(Action.GO_BACK)
            }
        )
    }

    private fun setupViewModel() {
        seedViewModel.actionResult.observe(viewLifecycleOwner) {
            Log.i(TAG, "actionResult=$it")
            processActionResult(it)
        }
    }

    override fun processModalData(modalData: ModalData) {
        if (modalData is ModalData.NewSeedMenu) {
            showAddSeedDialog()
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            //Seeds
            is ScreenData.SeedSelector -> {
                val seeds = screenData.f.seedNameCards
                adapter.submitList(seeds)
            }
            else -> {

            }
        }

    }

//    override fun onAirPlaneModeChanged(isOn: Boolean) {
//        seedViewModel.getAlertState()
//    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "SeedFragment"
        const val ARG_SEED_NAME = "seedName"
    }
}