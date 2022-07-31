package com.changjiashuai.paritysigner.ui.log

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.adapter.LogDetailsAdapter
import com.changjiashuai.paritysigner.adapter.LogEventAdapter
import com.changjiashuai.paritysigner.databinding.FragmentLogDetailsBinding
import com.changjiashuai.paritysigner.models.EventModel
import com.changjiashuai.paritysigner.viewmodel.LogDetailsViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/16 22:23.
 */
class LogDetailsFragment : BaseFragment() {

    private val logDetailsViewModel by viewModels<LogDetailsViewModel>()
    private var _binding: FragmentLogDetailsBinding? = null
    private val binding get() = _binding!!

    //    private val adapter = LogDetailsAdapter()
    private val adapter = LogEventAdapter()
    private var order: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogDetailsBinding.inflate(inflater, container, false)
        setupExtras()
        setupView()
        setupViewModel()
        return binding.root
    }

    private fun setupExtras() {
        order = arguments?.getString(LogFragment.ARG_ORDER) ?: ""
    }

    private fun setupView() {
        binding.rvList.adapter = adapter
    }

    private fun setupViewModel() {
        logDetailsViewModel.pushButton(Action.SHOW_LOG_DETAILS, order)
        logDetailsViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            is ScreenData.LogDetails -> {
                val logDetails = screenData.f
                binding.tvTimestamp.text = logDetails.timestamp
                var events = emptyList<EventModel>()
                logDetails.events.forEachIndexed { index, mEventMaybeDecoded ->
                    Log.i(TAG, "mEventMaybeDecoded=$mEventMaybeDecoded")
                    events = events.plus(
                        EventModel(
                            "$index",
                            logDetails.timestamp,
                            mEventMaybeDecoded.event
                        )
                    )
                }
                adapter.submitList(events)
//                adapter.submitList(logDetails.events)
            }
            else -> {

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "LogDetailsFragment"
    }
}