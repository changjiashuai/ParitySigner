package com.changjiashuai.paritysigner.ui.settings

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.SeedBoxStatus
import com.changjiashuai.paritysigner.adapter.DerivationAdapter
import com.changjiashuai.paritysigner.adapter.SeedAdapter
import com.changjiashuai.paritysigner.databinding.FragmentBackupSeedBinding
import com.changjiashuai.paritysigner.ext.showInfoSheet
import com.changjiashuai.paritysigner.viewmodel.BackupSeedViewModel
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.MBackup
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/30 19:03.
 */
class BackupSeedFragment : BaseFragment() {

    private val backupSeedViewModel by viewModels<BackupSeedViewModel>()
    private var _binding: FragmentBackupSeedBinding? = null
    private val binding get() = _binding!!
    private val adapter = SeedAdapter()
    private val authentication = Authentication {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupSeedBinding.inflate(inflater, container, false)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            backupSeedViewModel.pushButton(Action.GO_BACK)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        adapter.onItemClick = {
            backupSeedViewModel.pushButton(Action.BACKUP_SEED, it.seedName)
        }
        binding.rvList.adapter = adapter
    }

    private fun setupViewModel() {
        backupSeedViewModel.pushButton(Action.BACKUP_SEED)
        backupSeedViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        super.processScreenData(screenData)
        if (screenData is ScreenData.SelectSeedForBackup) {
            val mSeeds = screenData.f
            adapter.submitList(mSeeds.seedNameCards)
        }
    }

    override fun processModalData(modalData: ModalData) {
        super.processModalData(modalData)
        if (modalData is ModalData.Backup) {
            val mBackup = modalData.f
            showBackupSheet(mBackup)
        }
    }

    private fun showBackupSheet(mBackup: MBackup) {
        val seedName = mBackup.seedName
        val view = View.inflate(context, R.layout.layout_backup_seed, null)
        val backupSheet = context?.showInfoSheet(view)
        val ivClose = view.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener {
            backupSheet?.dismiss()
        }
        val tvSeedName = view.findViewById<TextView>(R.id.tv_seed_name)
        tvSeedName.text = "Seed Name: $seedName"
        val tvSeedPhrase = view.findViewById<TextView>(R.id.tv_seed_phrase)
        var phraseTips = "<font color='#ff0000'>Seed Phrase (倒计时60s 助记词消失不可见)</font>"
        tvSeedPhrase.text = Html.fromHtml(phraseTips, Html.FROM_HTML_MODE_COMPACT)
        val tvSeedPhraseValue = view.findViewById<TextView>(R.id.tv_seed_phrase_value)

        //fixme
        activity?.let { activity ->
            authentication.authenticate(activity) {
                backupSeedViewModel.getSeedForBackup(seedName, { seedPhrase ->
                    //TODO： 倒计时1分钟 隐藏可以备份的助记词
                    tvSeedPhraseValue.text = seedPhrase
                }, { seedBoxStatus ->
                    Log.i(TAG, "seedBoxStatus=$seedBoxStatus")
                    if (seedBoxStatus == SeedBoxStatus.Seed) {
                        startCountDownTimer(onTick = {
                            val second = if (it == 0L) 0 else it / 1000
                            phraseTips =
                                "<font color='#ff0000'>Seed Phrase (倒计时${second}s 助记词消失不可见)</font>"
                            tvSeedPhrase.text =
                                Html.fromHtml(phraseTips, Html.FROM_HTML_MODE_COMPACT)
                            if (tvSeedPhrase.isGone) {
                                tvSeedPhrase.isGone = false
                            }
                        }, onFinish = {
                            tvSeedPhrase.visibility = View.GONE
                            tvSeedPhraseValue.text =
                                "Time out. Come back again to see the seed phrase!"
                        })
                    }
                })
            }
        }

        val rvList = view.findViewById<RecyclerView>(R.id.rv_list)
        val adapter = DerivationAdapter()
        rvList.adapter = adapter
        adapter.submitList(mBackup.derivations)
        adapter.onItemClick = {

        }
    }

    private fun startCountDownTimer(onTick: (Long) -> Unit, onFinish: () -> Unit) {
        val countDownTimer = object : CountDownTimer(60 * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                onTick(millisUntilFinished)
            }

            override fun onFinish() {
                onFinish()
            }

        }
        countDownTimer.start()
    }

    companion object {
        private const val TAG = "BackupSeedFragment"
    }
}