package com.changjiashuai.paritysigner.ui.log

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.adapter.LogEventAdapter
import com.changjiashuai.paritysigner.databinding.FragmentLogBinding
import com.changjiashuai.paritysigner.ext.showAlert
import com.changjiashuai.paritysigner.ext.showSheetStyle2
import com.changjiashuai.paritysigner.models.EventModel
import com.changjiashuai.paritysigner.viewmodel.LogViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.parity.signer.uniffi.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class LogFragment : BaseFragment() {

    private val logViewModel by viewModels<LogViewModel>()
    private var _binding: FragmentLogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val adapter = LogEventAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
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
        inflater.inflate(R.menu.menu_log, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_more) {
            logViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        logViewModel.pushButton(Action.NAVBAR_LOG)
    }

    private fun setupView() {
        binding.rvList.adapter = adapter
        adapter.onItemClick = { event ->
            val bundle = bundleOf(Pair(ARG_ORDER, event.order))
            findNavController().navigate(R.id.action_log_to_details, bundle)
        }
    }


    private fun showLogBottomSheet(mLogRight: MLogRight) {
        context?.let {
            it.showSheetStyle2(
                title = "Log: [checksum: ${mLogRight.checksum}]",
                actionText = "Add note",
                actionClick = {
                    logViewModel.pushButton(Action.CREATE_LOG_COMMENT)
                },
                action2Text = "Clear log",
                action2Click = {
                    logViewModel.pushButton(Action.GO_BACK)
                    context?.showAlert(
                        title = "Clear log?",
                        message = "Do you want this Signer to forget all logged events? This is not reversible.",
                        showCancel = true,
                        cancelText = "Cancel",
                        cancelClick = {
                            logViewModel.pushButton(Action.GO_BACK)
                        },
                        confirmText = "Confirm",
                        confirmClick = {
                            logViewModel.pushButton(Action.CLEAR_LOG)
                        }
                    )
                },
                cancelText = "Cancel",
                cancelClick = {
                    logViewModel.pushButton(Action.GO_BACK)
                }
            )
        }
    }

    private fun setupViewModel() {
        logViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            //Log
            is ScreenData.Log -> {
                val histories = screenData.f.log
                var events = emptyList<EventModel>()
                histories.forEach { history ->
                    history.events.forEach { event ->
                        Log.i(TAG, "event=$event")
                        events =
                            events.plus(EventModel("${history.order}", history.timestamp, event))
                    }
                }
                adapter.submitList(events)
//                binding.rvList.smoothScrollToPosition(0)
            }
            else -> {

            }
        }
    }

    override fun processModalData(modalData: ModalData) {
        Log.i(TAG, "modalData=$modalData")
        if (modalData is ModalData.LogRight) {
            showLogBottomSheet(modalData.f)
        } else if (modalData is ModalData.LogComment) {
            showAddLogDialog()
        }
    }

    private fun showAddLogDialog() {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Comment")
                .setView(R.layout.dialog_add_log)
                .setPositiveButton("Confirm") { dialog, _ ->
                    val etLog: EditText? = (dialog as AlertDialog).findViewById(R.id.et_log)
                    etLog?.text?.toString()?.let { comment ->
                        logViewModel.pushButton(Action.GO_FORWARD, comment)
                    }
                }
                .setNegativeButton("Cancel") { _, _ ->
                    logViewModel.pushButton(Action.GO_BACK)
                }
                .show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "LogFragment"

        const val ARG_ORDER = "order"
    }
}