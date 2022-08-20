package com.changjiashuai.paritysigner.ui.scan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import com.changjiashuai.paritysigner.BaseActivity
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.adapter.TransactionCardAdapter
import com.changjiashuai.paritysigner.databinding.ActivityTransactionPreviewBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import com.changjiashuai.paritysigner.viewmodel.AbsViewModel
import io.parity.signer.uniffi.*

class TransactionPreviewActivity : BaseActivity() {

    private val transactionPreviewViewModel by viewModels<AbsViewModel>()
    private lateinit var binding: ActivityTransactionPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        setupViewModel()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupViewModel() {
//        transactionPreviewViewModel.pushButton(Action.TRANSACTION_FETCHED, payload)
//        transactionPreviewViewModel.actionResult.observe(this) {
//            processActionResult(it)
//        }
        mTransaction?.let { processTransaction(it) }
    }

    override fun processScreenData(screenData: ScreenData) {
        //Transaction
        when (screenData) {
            is ScreenData.Transaction -> {
                val mTransaction = screenData.f
                processTransaction(mTransaction)
            }
            else -> {

            }
        }
    }

    private fun processTransaction(mTransaction: MTransaction) {
        //TransactionCardSet
        val content = mTransaction.content
        processTransactionCardSet(content)

        //Address
        val authorInfo = mTransaction.authorInfo
        processAddress(authorInfo)

        //NetworkCard TODO: add network -> approval , decline
        val networkInfo = mTransaction.networkInfo
        processNetworkInfo(networkInfo)

        val transactionType = mTransaction.ttype
        processTransactionType(transactionType)
    }

    private fun processTransactionCardSet(transactionCardSet: TransactionCardSet) {
        //list
        val authorAdapter = TransactionCardAdapter()
        binding.rvAuthor.adapter = authorAdapter
        authorAdapter.submitList(transactionCardSet.author)

        val errorAdapter = TransactionCardAdapter()
        binding.rvError.adapter = errorAdapter
        errorAdapter.submitList(transactionCardSet.error)

        val extensionsAdapter = TransactionCardAdapter()
        binding.rvExtensions.adapter = extensionsAdapter
        extensionsAdapter.submitList(transactionCardSet.extensions)

        val importingDerivationsAdapter = TransactionCardAdapter()
        binding.rvImportingDerivations.adapter = importingDerivationsAdapter
        importingDerivationsAdapter.submitList(transactionCardSet.importingDerivations)

        val messageAdapter = TransactionCardAdapter()
        binding.rvMessage.adapter = messageAdapter
        messageAdapter.submitList(transactionCardSet.message)

        val metaAdapter = TransactionCardAdapter()
        binding.rvMeta.adapter = messageAdapter
        metaAdapter.submitList(transactionCardSet.meta)

        val methodAdapter = TransactionCardAdapter()
        binding.rvMethod.adapter = methodAdapter
        methodAdapter.submitList(transactionCardSet.method)

        val newSpecsAdapter = TransactionCardAdapter()
        binding.rvNewSpecs.adapter = newSpecsAdapter
        newSpecsAdapter.submitList(transactionCardSet.newSpecs)

        val verifierAdapter = TransactionCardAdapter()
        binding.rvVerifier.adapter = verifierAdapter
        verifierAdapter.submitList(transactionCardSet.verifier)

        val warningAdapter = TransactionCardAdapter()
        binding.rvWarning.adapter = warningAdapter
        warningAdapter.submitList(transactionCardSet.warning)

        val typesInfoAdapter = TransactionCardAdapter()
        binding.rvTypesInfo.adapter = typesInfoAdapter
        typesInfoAdapter.submitList(transactionCardSet.typesInfo)
    }

    private fun processAddress(address: Address?) {
        address?.let {
            binding.ivLogo.setImageBitmap(it.identicon.toBitmap())
            binding.tvSeedName.text = it.seedName
            binding.tvPath.text = it.path

            if (it.hasPwd) {
                binding.tvHasPwd.text = "/// {Locked Icon}"
            }

            binding.tvBase58.text = it.base58.abbreviateString(8)
        }
    }

    private fun processNetworkInfo(networkInfo: MscNetworkInfo?) {
        if (networkInfo != null) {
            binding.rlNetwork.root.visibility = View.VISIBLE
            when (networkInfo.networkLogo) {
                "polkadot" -> {
                    binding.rlNetwork.ivLogo.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
                }
                "kusama" -> {
                    binding.rlNetwork.ivLogo.setImageResource(R.drawable.ic_kusama_ksm_logo)
                }
                "westend" -> {
                    binding.rlNetwork.ivLogo.setImageResource(R.drawable.ic_polkadot_dot_logo)
                }
            }
            binding.rlNetwork.tvName.text = networkInfo.networkTitle
            binding.rlNetwork.ivCheck.visibility = View.GONE
        } else {
            binding.rlNetwork.root.visibility = View.GONE
        }
    }

    private fun processTransactionType(transactionType: TransactionType) {
        when (transactionType) {
            TransactionType.SIGN -> {
//                Text(
//                    "LOG NOTE",
//                    style = MaterialTheme.typography.overline,
//                    color = MaterialTheme.colors.Text400
//                )
//
//                SingleTextInput(
//                    content = comment,
//                    update = { comment.value = it },
//                    onDone = { },
//                    focusManager = focusManager,
//                    focusRequester = focusRequester
//                )
//
//                Text(
//                    "visible only on this device",
//                    style = MaterialTheme.typography.subtitle1,
//                    color = MaterialTheme.colors.Text400
//                )
//
//                BigButton(
//                    text = "Unlock key and sign",
//                    action = {
//                        signTransaction(
//                            comment.value, transaction.authorInfo?.seedName ?: ""
//                        )
//                    }
//                )
//                BigButton(
//                    text = "Decline",
//                    action = {
//                        button(Action.GO_BACK, "", "")
//                    }
//                )
            }
            TransactionType.DONE -> {
//                BigButton(
//                    text = "Done",
//                    action = {
//                        button(Action.GO_BACK, "", "")
//                    }
//                )
            }
            TransactionType.STUB -> {
//                BigButton(
//                    text = "Approve",
//                    action = {
//                        button(Action.GO_FORWARD, "", "")
//                    }
//                )
//                BigButton(
//                    text = "Decline",
//                    action = {
//                        button(Action.GO_BACK, "", "")
//                    }
//                )
            }
            TransactionType.READ -> {
//                BigButton(
//                    text = "Back",
//                    action = {
//                        button(Action.GO_BACK, "", "")
//                    }
//                )
            }
            TransactionType.IMPORT_DERIVATIONS -> {
//                BigButton(
//                    text = "Select seed",
//                    action = {
//                        button(Action.GO_FORWARD, "", "")
//                    }
//                )
//                BigButton(
//                    text = "Decline",
//                    action = {
//                        button(Action.GO_BACK, "", "")
//                    }
//                )
            }
        }
    }

    override fun finish() {
        transactionPreviewViewModel.doAction(Action.GO_BACK)
        super.finish()
    }

    companion object {

        private const val TAG = "TransactionPreviewActivity"

        var mTransaction: MTransaction? = null

        //payload too large can not put extras
        fun startActivity(context: Context) {
            val intent = Intent(context, TransactionPreviewActivity::class.java)
            context.startActivity(intent)
        }
    }
}