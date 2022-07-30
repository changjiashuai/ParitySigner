package com.changjiashuai.paritysigner.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.Authentication
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.SeedBoxStatus
import com.changjiashuai.paritysigner.adapter.DerivationAdapter
import com.changjiashuai.paritysigner.adapter.SeedAdapter
import com.changjiashuai.paritysigner.databinding.ActivityBackupSeedBinding
import com.changjiashuai.paritysigner.ext.showInfoSheet
import com.changjiashuai.paritysigner.models.pushButton
import com.changjiashuai.paritysigner.viewmodel.BackupSeedViewModel
import io.parity.signer.models.getSeedForBackup
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.MBackup
import io.parity.signer.uniffi.ModalData
import io.parity.signer.uniffi.ScreenData

/**
 * 备份助记词： 手抄助记词保存
 */
class BackupSeedActivity : BaseActivity() {

    private val backupSeedViewModel by viewModels<BackupSeedViewModel>()
    private lateinit var binding: ActivityBackupSeedBinding
    private val adapter = SeedAdapter()
    private var authentication = Authentication {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupSeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        backupSeedViewModel.pushButton(Action.BACKUP_SEED)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        adapter.onItemClick = {
            backupSeedViewModel.pushButton(Action.BACKUP_SEED, it.seedName)
        }
        binding.rvList.adapter = adapter
    }

    private fun setupViewModel() {
        backupSeedViewModel.actionResult.observe(this) {
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

    override fun processModalData(modalData: ModalData?) {
        super.processModalData(modalData)
        if (modalData is ModalData.Backup) {
            val mBackup = modalData.f
            showBackupSheet(mBackup)
        }
    }

    private fun showBackupSheet(mBackup: MBackup) {
        val seedName = mBackup.seedName
        val view = View.inflate(this, R.layout.layout_backup_seed, null)
        val backupSheet = showInfoSheet(view)
        val ivClose = view.findViewById<ImageView>(R.id.iv_close)
        ivClose.setOnClickListener {
            backupSheet.dismiss()
        }
        val tvSeedName = view.findViewById<TextView>(R.id.tv_seed_name)
        tvSeedName.text = "Seed Name: $seedName"
        val tvSeedPhrase = view.findViewById<TextView>(R.id.tv_seed_phrase)
        tvSeedPhrase.text = "Seed Phrase (倒计时60s 助记词消失不可见)"
        val tvSeedPhraseValue = view.findViewById<TextView>(R.id.tv_seed_phrase_value)

        //fixme
        authentication.authenticate(this) {
            backupSeedViewModel.getSeedForBackup(seedName, { seedPhrase ->
                //TODO： 倒计时1分钟 隐藏可以备份的助记词
                tvSeedPhraseValue.text = seedPhrase
            }, { seedBoxStatus ->
                Log.i(TAG, "seedBoxStatus=$seedBoxStatus")
                if (seedBoxStatus == SeedBoxStatus.Seed) {
                    startCountDownTimer(onTick = {
                        val second = if (it == 0L) 0 else it / 1000
                        tvSeedPhrase.text = "Seed Phrase (倒计时 ${second}s 助记词消失不可见)"
                    }, onFinish = {
                        tvSeedPhraseValue.text = "Time out. Come back again to see the seed phrase!"
                    })
                }
            })
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

    override fun finish() {
        backupSeedViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }


    companion object {

        private const val TAG = "BackupSeedActivity"

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, BackupSeedActivity::class.java))
        }
    }
}