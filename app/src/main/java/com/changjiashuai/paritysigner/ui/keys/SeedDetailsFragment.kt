package com.changjiashuai.paritysigner.ui.keys

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.*
import com.changjiashuai.paritysigner.adapter.DerivationAdapter
import com.changjiashuai.paritysigner.adapter.DerivedKeyAdapter
import com.changjiashuai.paritysigner.adapter.NetworkSelectorAdapter
import com.changjiashuai.paritysigner.databinding.FragmentSeedDetailsBinding
import com.changjiashuai.paritysigner.ext.*
import com.changjiashuai.paritysigner.models.*
import com.changjiashuai.paritysigner.utils.AirPlaneUtils
import com.changjiashuai.paritysigner.viewmodel.SeedDetailsViewModel
import io.parity.signer.uniffi.*

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/17 00:20.
 */
class SeedDetailsFragment : BaseFragment() {

    private val seedDetailsViewModel by viewModels<SeedDetailsViewModel>()
    private val authentication = Authentication()
    private var _binding: FragmentSeedDetailsBinding? = null
    private val binding get() = _binding!!
    private var seedName: String = ""
    private val adapter = DerivedKeyAdapter()
    private var isLongPress = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeedDetailsBinding.inflate(inflater, container, false)
        setupExtras()
        setupView()
        setupViewModel()
        return binding.root
    }

    private fun setupExtras() {
        seedName = arguments?.getString(SeedFragment.ARG_SEED_NAME) ?: ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (isLongPress) {
            inflater.inflate(R.menu.menu_seed_select_mode, menu)
        } else {
            inflater.inflate(R.menu.menu_seed, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        } else if (item.itemId == R.id.action_seed) {
            seedDetailsViewModel.pushButton(Action.RIGHT_BUTTON_ACTION)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        seedDetailsViewModel.pushButton(Action.GO_BACK)
        findNavController().navigateUp()
    }

    private fun showSeedSheet(seedMenu: ModalData.SeedMenu) {
        context?.showSheetStyle3(
            title = "Seed Menu",
            actionText = "Backup",
            actionClick = {
                val alertState = context?.let { AirPlaneUtils.getAlertState(it) }
                if (alertState == AlertState.None) {
                    seedDetailsViewModel.pushButton(Action.BACKUP_SEED)
                } else {
//                    seedDetailsViewModel.pushButton(Action.SHIELD)
                    showShieldAlert(alertState)
                }
            },
            action2Text = "Derive new key",
            action2Click = {
                val alertState = context?.let { AirPlaneUtils.getAlertState(it) }
                if (alertState == AlertState.None) {
                    goToNewDeriveKey()
                } else {
//                    seedDetailsViewModel.pushButton(Action.SHIELD)
                    showShieldAlert(alertState)
                }
            },
            action3Text = "Forget this seed forever",
            action3Click = {
                context?.showAlert(
                    title = "Forget this seed forever?",
                    message = "This seed will be removed for all networks. This is not reversible. Are you sure?",
                    showCancel = true,
                    cancelText = "Cancel",
                    cancelClick = {
                        seedDetailsViewModel.pushButton(Action.GO_BACK)
                    },
                    confirmText = "Remove seed",
                    confirmClick = {
                        val seedName = seedMenu.f.seed
                        activity?.let {
                            authentication.authenticate(it) {
                                seedDetailsViewModel.removeSeed(seedName)
                            }
                        }
                    }
                )
            },
            cancelText = "Cancel",
            cancelClick = {
                seedDetailsViewModel.pushButton(Action.GO_BACK)
            }
        )
    }

    private var actionMode: ActionMode? = null
    private var tracker: SelectionTracker<Long>? = null

    private fun setupView() {
        binding.rvList.adapter = adapter

        initTracker()
        addTrackerObserver()
        adapter.tracker = tracker
        adapter.onItemClick = { mKeysCard ->
            if (actionMode == null) {
                goToKeyDetails(KEY_TYPE_DERIVED, mKeysCard.addressKey)
            }
        }
    }

    private fun initTracker() {
        tracker = SelectionTracker.Builder<Long>(
            "derivedKey-selection",
            binding.rvList,
            LongKeyProvider(binding.rvList),
            DerivedKeyAdapter.DerivedKeyItemDetailsLookup(binding.rvList),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
    }

    private fun addTrackerObserver() {
        tracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                Log.i(TAG, "onSelectionChanged")
                val size = tracker?.selection?.size()
                if (size == 0) {
                    actionMode?.title = "请选择"
                    actionMode?.finish()
                } else {
                    if (actionMode == null) {
                        startActionMode()
                    } else {
                        actionMode?.title = "已选择($size)"
                    }
                }
            }

            override fun onItemStateChanged(key: Long, selected: Boolean) {
                val mKeysCard = adapter.currentList.getOrNull(key.toInt())
                Log.i(
                    TAG,
                    "onItemStateChanged, selected=$selected, key=$key, path=${mKeysCard?.path}"
                )
                mKeysCard?.let {
                    seedDetailsViewModel.pushButton(Action.LONG_TAP, mKeysCard.addressKey)
                }
            }
        })
    }

    /**
     * start action mode.
     */
    private fun startActionMode() {
        actionMode = activity?.startActionMode(object : ActionMode.Callback {
            override fun onCreateActionMode(
                mode: ActionMode?,
                menu: Menu?
            ): Boolean {
                mode?.menuInflater?.inflate(R.menu.menu_seed_select_mode, menu)
                return true
            }

            override fun onPrepareActionMode(
                mode: ActionMode?,
                menu: Menu?
            ): Boolean {
                return false
            }

            override fun onActionItemClicked(
                mode: ActionMode?,
                menuItem: MenuItem?
            ): Boolean {
                when (menuItem?.itemId) {
                    R.id.action_delete -> {
                        showDeleteKeyAlert()
                        return true
                    }
                    R.id.action_export -> {
                        findNavController().navigate(R.id.action_seedDetails_to_keyMultiExport)
                        actionMode?.finish()
                        return true
                    }
                }
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
                seedDetailsViewModel.pushButton(Action.GO_BACK)
                tracker?.clearSelection()
                actionMode = null
            }
        })
    }

    /**
     * Show delete key alert.
     */
    private fun showDeleteKeyAlert() {
        context?.showAlert(
            title = "Delete keys?",
            message = "You are about to delete selected keys",
            showCancel = true,
            cancelText = "Cancel",
            cancelClick = {

            },
            confirmText = "Delete",
            confirmClick = {
                // delete[Action.REMOVE_KEY] refresh ui
                seedDetailsViewModel.pushButton(Action.REMOVE_KEY)
                actionMode?.finish()
            }
        )
    }

    /**
     * Go to key details.
     */
    private fun goToKeyDetails(type: Int, addressKey: String) {
        if (addressKey.isNotBlank()) {
            val bundle = bundleOf(
                Pair(EXTRAS_TYPE, type),
                Pair(EXTRAS_ADDRESS_KEY, addressKey)
            )
            findNavController().navigate(R.id.action_seedDetails_to_keyDetails, bundle)
        }
    }

    private fun setupViewModel() {
        seedDetailsViewModel.pushButton(Action.SELECT_SEED, seedName)
        seedDetailsViewModel.actionResult.observe(viewLifecycleOwner) {
            processActionResult(it)
        }
    }

    override fun processScreenData(screenData: ScreenData) {
        when (screenData) {
            is ScreenData.Keys -> {
                val mKeys = screenData.f
                val actionResult = seedDetailsViewModel.actionResult.value
                if (actionResult?.rightButton == null) {
                    Toast.makeText(context, "show backup sheet", Toast.LENGTH_SHORT).show()
                } else if (actionResult.rightButton == RightButton.BACKUP) {
                    showSeedDetailsUi(mKeys)
                } else if (actionResult.rightButton == RightButton.MULTI_SELECT) {
                    //multi mode
//                    adapter.notifyDataSetChanged()
                }


            }
            is ScreenData.KeyDetailsMulti -> {
                val mKeyDetailsMulti = screenData.f
            }
            else -> {

            }
        }

    }

    private fun showSeedDetailsUi(mKeys: MKeys) {
        //root seed
        binding.ivLogo.setImageBitmap(mKeys.root.identicon.toBitmap())
        binding.tvRootSeed.text = mKeys.root.seedName
        binding.tvRootAddressKey.text = mKeys.root.base58.abbreviateString(8)
        binding.rlRootSeed.setOnClickListener {
            goToKeyDetails(KEY_TYPE_SEED, mKeys.root.addressKey)
        }

        //network
        when (mKeys.network.logo) {
            "polkadot" -> {
                binding.ivNetworkLogo.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
            }
            "kusama" -> {
                binding.ivNetworkLogo.setImageResource(R.drawable.ic_kusama_ksm_logo)
            }
            "westend" -> {
                binding.ivNetworkLogo.setImageResource(R.drawable.ic_polkadot_dot_logo)
            }
        }
        binding.tvNetworkTitle.text = mKeys.network.title
        binding.ivNetworkArrowDown.setOnClickListener {
            //change network
            seedDetailsViewModel.pushButton(Action.NETWORK_SELECTOR)
        }

        //derived keys
        binding.tvDerivedKeys.text = "Derived Keys"
        binding.ivAddKey.setOnClickListener {
            //add derived key
            if (context?.let { AirPlaneUtils.getAlertState(it) } == AlertState.None) {
                goToNewDeriveKey()
            } else {
                seedDetailsViewModel.pushButton(Action.SHIELD)
            }
        }
        adapter.submitList(mKeys.set)
    }

    private fun goToNewDeriveKey() {
        findNavController().navigate(R.id.action_seedDetails_to_newDeriveKey)
    }

    private fun showNetworkSelector(mNetworkMenu: MNetworkMenu) {
        val networkSheet = View.inflate(context, R.layout.layout_network, null)
        val ivClose = networkSheet.findViewById<ImageView>(R.id.iv_close)
        val rvList = networkSheet.findViewById<RecyclerView>(R.id.rv_list)
        val bottomSheet = context?.showInfoSheet(contentView = networkSheet)
        ivClose.setOnClickListener {
            seedDetailsViewModel.pushButton(Action.GO_BACK)
            bottomSheet?.dismiss()
        }
        val adapter = NetworkSelectorAdapter()
        adapter.onItemClick = {
            bottomSheet?.dismiss()
            seedDetailsViewModel.pushButton(Action.CHANGE_NETWORK, it.key)
        }
        rvList.adapter = adapter
        adapter.submitList(mNetworkMenu.networks)

    }

    override fun processModalData(modalData: ModalData) {
        if (modalData is ModalData.SeedMenu) {
            showSeedSheet(modalData)
        } else if (modalData is ModalData.NetworkSelector) {
            val mNetworkMenu = modalData.f
            showNetworkSelector(mNetworkMenu)
        } else if (modalData is ModalData.Backup) {
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
        tvSeedPhrase.text = "Seed Phrase (倒计时60s 助记词消失不可见)"
        val tvSeedPhraseValue = view.findViewById<TextView>(R.id.tv_seed_phrase_value)

        //fixme
        activity?.let { activity ->
            authentication.authenticate(activity) {
                seedDetailsViewModel.getSeedForBackup(activity, seedName, { seedPhrase ->
                    //TODO： 倒计时1分钟 隐藏可以备份的助记词
                    tvSeedPhraseValue.text = seedPhrase
                }, { seedBoxStatus ->
                    Log.i(TAG, "seedBoxStatus=$seedBoxStatus")
                    if (seedBoxStatus == SeedBoxStatus.Seed) {
                        startCountDownTimer(onTick = {
                            val second = if (it == 0L) 0 else it / 1000
                            tvSeedPhrase.text = "Seed Phrase (倒计时 ${second}s 助记词消失不可见)"
                        }, onFinish = {
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SeedDetailsFragment"

        const val EXTRAS_TYPE = "type"
        const val EXTRAS_ADDRESS_KEY = "addressKey"
        const val KEY_TYPE_SEED = 0
        const val KEY_TYPE_DERIVED = 1
    }
}