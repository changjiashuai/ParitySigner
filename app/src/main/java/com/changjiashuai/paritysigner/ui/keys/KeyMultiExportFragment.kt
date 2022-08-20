package com.changjiashuai.paritysigner.ui.keys

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.viewModels
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentKeyMultiExportBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import com.changjiashuai.paritysigner.viewmodel.AbsViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.MKeyDetailsMulti
import io.parity.signer.uniffi.ScreenData
import kotlin.math.abs

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/8/20 15:16.
 */
class KeyMultiExportFragment : BaseFragment() {

    private val viewModel by viewModels<AbsViewModel>()
    private var _binding: FragmentKeyMultiExportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeyMultiExportBinding.inflate(inflater, container, false)
        setupView()
        setupViewModel()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "item.id=${item.itemId}")
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        viewModel.pushButton(Action.GO_BACK)
        super.onBackPressed()
    }

    private fun setupView() {
    }

    private fun setupViewModel() {
        //export[Action.EXPORT_MULTI_SELECT]: screenData=KeyDetailsMulti
        viewModel.pushButton(Action.EXPORT_MULTI_SELECT)
        viewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        Log.i(TAG, "screenData=$screenData")
        when (screenData) {
            is ScreenData.KeyDetailsMulti -> {
                val mKeyDetailsMulti = screenData.f
                showKeyMultiExportUi(mKeyDetailsMulti)
            }
            else -> {

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showKeyMultiExportUi(mKeyDetailsMulti: MKeyDetailsMulti) {
        val address = mKeyDetailsMulti.keyDetails.address
        binding.ivLogo.setImageBitmap(address.identicon.toBitmap())
        if (address.hasPwd) {
            binding.tvSeedNamePath.text =
                "${address.seedName} ${address.path}///{Locked account icon}"
        } else {
            binding.tvSeedNamePath.text = "${address.seedName} ${address.path}"
        }
        binding.tvPublicKey.text = address.base58.abbreviateString(8)

        val networkInfo = mKeyDetailsMulti.keyDetails.networkInfo
        when (networkInfo.networkLogo) {
            "polkadot" -> {
                binding.ivSpecsLogo.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
            }
            "kusama" -> {
                binding.ivSpecsLogo.setImageResource(R.drawable.ic_kusama_ksm_logo)
            }
            "westend" -> {
                binding.ivSpecsLogo.setImageResource(R.drawable.ic_polkadot_dot_logo)
            }
        }
        binding.tvSpecsName.text = networkInfo.networkTitle

        //qr list
        binding.ivQrCode.setImageBitmap(mKeyDetailsMulti.keyDetails.qr.toBitmap())
        val currentIndex = mKeyDetailsMulti.currentNumber.toInt()
        val total = mKeyDetailsMulti.outOf.toInt()
        binding.tvKeysIndex.text = "key $currentIndex out of $total"

        if (total == 1) {
            binding.btnPrev.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
        } else if (total > 1) {
            if (currentIndex == 1) {
                binding.btnPrev.visibility = View.GONE
                binding.btnNext.visibility = View.VISIBLE
            } else if (currentIndex == total) {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.GONE
            } else {
                binding.btnPrev.visibility = View.VISIBLE
                binding.btnNext.visibility = View.VISIBLE
            }
        }

        binding.btnPrev.setOnClickListener { viewModel.pushButton(Action.PREVIOUS_UNIT) }
        binding.btnNext.setOnClickListener { viewModel.pushButton(Action.NEXT_UNIT) }
    }

    private fun swipeLeft() {
        Log.i(TAG, "left")
        viewModel.pushButton(Action.NEXT_UNIT)
    }

    private fun swipeRight() {
        Log.i(TAG, "right")
        viewModel.pushButton(Action.PREVIOUS_UNIT)
    }

    private fun swipeUp() {
        Log.i(TAG, "up")
    }

    private fun swipeDown() {
        Log.i(TAG, "down")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "KeyMultiExportFragment"
    }
}