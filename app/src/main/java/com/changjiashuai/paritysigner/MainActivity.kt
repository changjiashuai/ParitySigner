package com.changjiashuai.paritysigner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.*
import com.changjiashuai.paritysigner.databinding.ActivityMainBinding
import com.changjiashuai.paritysigner.ui.HomeActivity
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.utils.PrefsUtils
import com.changjiashuai.paritysigner.viewmodel.MainViewModel
import io.parity.signer.uniffi.ActionResult
import io.parity.signer.uniffi.ScreenData

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainViewModel by viewModels<MainViewModel>()
    private val authentication = Authentication {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            applyInsets(windowInsets)
        }
        setupView()
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        unLockApp()
    }

    private fun setupView() {
        binding.btnUnlock.setOnClickListener {
            unLockApp()
        }
    }

    private fun setupViewModel() {
        mainViewModel.onBoardingState.observe(this) {
            processOnBoarding(it)
        }
    }

    private fun unLockApp() {
        authentication.authenticate(this) {
            PrefsUtils.initEncryptedPrefs(applicationContext)
            if (!AirPlaneUtils.isAirplaneOn(this)) {
                mainViewModel.deviceWasOnline()
            }
            mainViewModel.totalRefresh()
        }
    }

    private fun processOnBoarding(onBoardingState: OnBoardingState) {
        Log.i(TAG, "[START:----> processOnBoarding onBoardingState=$onBoardingState")
        when (onBoardingState) {
            // first enter
            OnBoardingState.InProgress -> {

            }
            // after auth success
            OnBoardingState.No -> {
                mainViewModel.onBoard()
            }
            //
            OnBoardingState.Yes -> {
                goToHome()
            }
        }
        Log.i(TAG, "END:<----- processOnBoarding]")
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
    }

    override fun processActionResult(actionResult: ActionResult) {
        Log.i(TAG, "actionResult=$actionResult")
        when (actionResult.screenData) {
            //Term of service
            is ScreenData.Documents -> {

            }
            else -> {

            }
        }
    }

    private val currentInsetTypeMask =
        WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars()

    private fun applyInsets(currentWindowInsets: WindowInsetsCompat): WindowInsetsCompat {
        val insets = currentWindowInsets.getInsets(currentInsetTypeMask)
        binding.root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            updateMargins(insets.left, insets.top, insets.right, insets.bottom)
        }
        return WindowInsetsCompat.Builder()
            .setInsets(currentInsetTypeMask, insets)
            .build()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}