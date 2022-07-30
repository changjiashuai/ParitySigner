package com.changjiashuai.paritysigner.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.databinding.ActivityAboutBinding
import com.changjiashuai.paritysigner.viewmodel.AbsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import io.parity.signer.uniffi.*

class AboutActivity : BaseActivity() {

    private val absViewModel by viewModels<AbsViewModel>()
    private lateinit var binding: ActivityAboutBinding
    private val fragments = listOf(TOSFragment(), PPFragment())
    private val tabs = listOf("Terms of service", "Privacy policy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupViewModel()
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

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return fragments.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position: Int ->
            tab.text = tabs[position]
        }.attach()
    }

    private fun setupViewModel() {
        absViewModel.pushButton(Action.SHOW_DOCUMENTS)
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            is ScreenData.Documents -> {
                //TODO: show toc
            }
            else -> {

            }
        }

    }

    override fun processModalData(modalData: ModalData?) {
        when (modalData) {
            is ModalData.TypesInfo -> {
//                Action.SIGN_TYPES -->screenData==>SignSufficientCrypto
            }
            else -> {

            }
        }
    }

    override fun processAlertData(alertData: AlertData?) {
        when (alertData) {
            is AlertData.ErrorData -> {
                val f = alertData.f
                Toast.makeText(this, "error=$f", Toast.LENGTH_LONG).show()
            }
            else -> {

            }
        }
    }

    override fun finish() {
        absViewModel.pushButton(Action.GO_BACK)
        super.finish()
    }

    companion object {
        private const val TAG = "AboutActivity"
    }
}