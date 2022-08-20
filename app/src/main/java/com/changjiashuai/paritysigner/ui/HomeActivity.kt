package com.changjiashuai.paritysigner.ui

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ActivityHomeBinding
import com.changjiashuai.paritysigner.models.AlertState
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.viewmodel.AbsViewModel
import io.parity.signer.uniffi.Action

class HomeActivity : BaseActivity() {

    private val absViewModel by viewModels<AbsViewModel>()
    private lateinit var binding: ActivityHomeBinding

    private var menuStatus: MenuItem? = null
    private var navHostFragment: NavHostFragment? = null

    private var navController: NavController? = null
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val tabIds =
        listOf(R.id.logScreen, R.id.scanScreen, R.id.seedScreen, R.id.settingsScreen)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
//        setupViewModel()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment
        navController = navHostFragment?.navController

        // Setup the bottom navigation view with navController
        val bottomNavigationView = binding.navView
        if (navController != null) {
            bottomNavigationView.setupWithNavController(navController!!)
        }
        bottomNavigationView.setOnItemSelectedListener {
            navController?.navigate(it.itemId)
            return@setOnItemSelectedListener true
        }

        // Setup the ActionBar with navController and 3 top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.logScreen, R.id.scanScreen, R.id.seedScreen, R.id.settingsScreen)
        )
        if (navController != null) {
            setupActionBarWithNavController(navController!!, appBarConfiguration)
        }
        navController?.addOnDestinationChangedListener { _, destination, arguments ->
            if (tabIds.contains(destination.id)) {
                binding.navView.visibility = View.VISIBLE
            } else {
                binding.navView.visibility = View.GONE
            }
            when (destination.id) {
                R.id.log -> {
                    binding.toolbar.title = "Log"
                    absViewModel.doAction(Action.NAVBAR_LOG)
                }
                R.id.scan -> {
                    binding.toolbar.title = "Scan"
                    absViewModel.doAction(Action.NAVBAR_KEYS)
                }
                R.id.keys -> {
                    binding.toolbar.title = "Seed"
                    absViewModel.doAction(Action.NAVBAR_SCAN)
                }
                R.id.settings -> {
                    binding.toolbar.title = "Settings"
                    absViewModel.doAction(Action.NAVBAR_SETTINGS)
                }
            }
        }
        bottomNavigationView.setOnItemSelectedListener {
            val currentId = navController?.currentDestination?.id
            val itemId = it.itemId
            val ids = listOf(R.id.log, R.id.scan, R.id.keys, R.id.settings)
            Log.i(TAG, "tabIds=$tabIds")
            Log.i(TAG, "ids=$ids")
            Log.i(TAG, "currentId=$currentId, itemId=$itemId")
            if (tabIds.contains(currentId)) {
                navController?.navigate(itemId)
                return@setOnItemSelectedListener true
            } else {
                return@setOnItemSelectedListener false
            }
        }
    }

    private fun setupViewModel() {
        val alertState = AirPlaneUtils.getAlertState(this)
        checkNetworkState(alertState)
        showShieldAlertIfNeed()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController?.navigateUp(appBarConfiguration) ?: super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menuStatus = menu?.findItem(R.id.action_status)
        setupViewModel()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_status -> {
                val alertState = AirPlaneUtils.getAlertState(this)
                checkNetworkState(alertState)
                showShieldAlertIfNeed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkNetworkState(alertState: AlertState) {
        when (alertState) {
            AlertState.Active -> {
                //Network connected
                menuStatus?.setIcon(R.drawable.ic_gpp_bad_24)
            }
            AlertState.None -> {
                //Signer is secure
                menuStatus?.setIcon(R.drawable.ic_verified_user_24)
            }
            AlertState.Past -> {
                //Network was connected
                menuStatus?.setIcon(R.drawable.ic_gpp_maybe_24)
            }
            else -> {
                //Network detector failure
                menuStatus?.setIcon(R.drawable.ic_baseline_security_24)
            }

        }
    }

    override fun onAirPlaneModeChanged(isOn: Boolean, alertState: AlertState) {
        super.onAirPlaneModeChanged(isOn, alertState)
        checkNetworkState(alertState)
    }

    override fun onBack() {
//        absViewModel.pushButton(Action.GO_BACK)
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}