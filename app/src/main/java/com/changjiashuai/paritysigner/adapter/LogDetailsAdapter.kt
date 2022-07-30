package com.changjiashuai.paritysigner.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemLogBinding
import com.changjiashuai.paritysigner.models.abbreviateString
import com.changjiashuai.paritysigner.models.encodeHex
import io.parity.signer.uniffi.*

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/26 19:33.
 */
class LogDetailsAdapter : ListAdapter<MEventMaybeDecoded, LogDetailsAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it.event)
            //click Action.SHOW_LOG_DETAILS
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MEventMaybeDecoded>() {

            override fun areContentsTheSame(
                oldItem: MEventMaybeDecoded,
                newItem: MEventMaybeDecoded
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: MEventMaybeDecoded,
                newItem: MEventMaybeDecoded
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        val context = binding.tvEvent.context
        val blackColor = ContextCompat.getColor(context, R.color.black)
        val redColor = ContextCompat.getColor(context, R.color.red)

        fun bind(event: Event) {
            Log.i("TAG", "bind event=$event")
            when (event) {
                is Event.DatabaseInitiated -> {
                    //icon: smartphone
                    binding.tvEvent.text = "Database initiated"
                    binding.tvEvent.setTextColor(blackColor)
                }
                is Event.DeviceWasOnline -> {
                    //icon: Dangerous
                    binding.tvEvent.text = "Device was connected to network"
                    binding.tvEvent.setTextColor(redColor)
                }
                is Event.GeneralVerifierSet -> {
                    val hex = event.verifier.v.let {
                        when (it) {
                            is VerifierValue.Standard -> {
                                it.m
                            }
                            else -> listOf()
                        }
                    }
                    //icon: Shield
                    binding.tvEvent.text = "General verifier set"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = hex.getOrElse(0) { "" }
                        .abbreviateString(8) + hex.getOrElse(1) { "" }
                }
                is Event.HistoryCleared -> {
                    //icon: DeleteForever
                    binding.tvEvent.text = "History cleared"
                    binding.tvEvent.setTextColor(blackColor)
                }
                is Event.IdentitiesWiped -> {
                    //icon: Delete
                    binding.tvEvent.text = "All keys were wiped"
                    binding.tvEvent.setTextColor(blackColor)
                }
                is Event.IdentityAdded -> {
                    event.identityHistory.let {
                        //icon: Pattern
                        binding.tvEvent.text = "Key created"
                        binding.tvEvent.setTextColor(blackColor)
                        binding.tvName.text = it.seedName + it.path
                    }
                }
                is Event.IdentityRemoved -> {
                    event.identityHistory.let {
                        //icon: Delete
                        binding.tvEvent.text = "Key removed"
                        binding.tvEvent.setTextColor(blackColor)
                        binding.tvName.text = it.seedName + it.path
                    }
                }
                is Event.MessageSignError -> {
                    //icon: Warning
                    binding.tvEvent.text = "Message signing error!"
                    binding.tvEvent.setTextColor(redColor)
                    binding.tvName.text =
                        "message:" + event.signMessageDisplay.message + " user comment: " + event.signMessageDisplay.userComment
                }
                is Event.MessageSigned -> {
                    //icon: Done
                    binding.tvEvent.text = "Generated signature for message"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text =
                        "message:" + event.signMessageDisplay.message + " user comment: " + event.signMessageDisplay.userComment
                }
                is Event.MetadataAdded -> {
                    event.metaValuesDisplay.let {
                        //icon: QrCodeScanner
                        binding.tvEvent.text = "Metadata added"
                        binding.tvEvent.setTextColor(blackColor)
                        binding.tvName.text = it.name + " version " + it.version
                    }
                }
                is Event.MetadataRemoved -> {
                    event.metaValuesDisplay.let {
                        //icon: Delete
                        binding.tvEvent.text = "Metadata removed"
                        binding.tvEvent.setTextColor(blackColor)
                        binding.tvName.text = it.name + " version " + it.version
                    }
                }
                is Event.NetworkSpecsAdded -> {
                    //icon: QrCodeScanner
                    binding.tvEvent.text = "Network added"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.networkSpecsDisplay.specs.title
                }
                is Event.NetworkSpecsRemoved -> {
                    //icon: Delete
                    binding.tvEvent.text = "Network removed"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.networkSpecsDisplay.specs.title
                }
                is Event.NetworkVerifierSet -> {
                    var line3 =
                        when (val ver = event.networkVerifierDisplay.validCurrentVerifier) {
                            is ValidCurrentVerifier.Custom -> {
                                when (val v = ver.v.v) {
                                    is VerifierValue.Standard -> v.m.getOrElse(0) { "" } + " with encryption " + v.m.getOrElse(
                                        1
                                    ) { "" }
                                    null -> ""
                                }
                            }
                            ValidCurrentVerifier.General -> {
                                when (val v = event.networkVerifierDisplay.generalVerifier.v) {
                                    is VerifierValue.Standard -> "general"
                                    null -> ""
                                }
                            }
                        }

                    line3 += " for network with genesis hash " + event.networkVerifierDisplay.genesisHash.toUByteArray()
                        .toByteArray().encodeHex()
                    //icon: Shield
                    binding.tvEvent.text = "Network verifier set"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = line3
                }
                is Event.ResetDangerRecord -> {
                    //icon: DeleteForever
                    binding.tvEvent.text = "History cleared"
                    binding.tvEvent.setTextColor(redColor)
                }
                is Event.SeedCreated -> {
                    //icon: Pattern
                    binding.tvEvent.text = "Seed created"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.seedCreated
                }
                is Event.SeedNameWasShown -> {
                    //icon: Warning
                    binding.tvEvent.text = "Seed was shown"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.seedNameWasShown
                }
                is Event.NetworkSpecsSigned -> {
                    //icon: Verified
                    binding.tvEvent.text = "Network specs signed"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.networkSpecsExport.specsToSend.title
                }
                is Event.MetadataSigned -> {
                    event.metaValuesExport.let {
                        //icon: Verified
                        binding.tvEvent.text = "Meta signed"
                        binding.tvEvent.setTextColor(blackColor)
                        binding.tvName.text = it.name + it.version
                    }
                }
                is Event.TypesSigned -> {
                    //icon: Verified
                    binding.tvEvent.text = "Types signed"
                    binding.tvEvent.setTextColor(blackColor)
                }
                is Event.SystemEntry -> {
                    //icon: Warning
                    binding.tvEvent.text = "System entry"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.systemEntry
                }
                is Event.TransactionSignError -> {
                    //icon: Dangerous
                    binding.tvEvent.text = "Signing failure"
                    binding.tvEvent.setTextColor(redColor)
                    binding.tvName.text = event.signDisplay.userComment
                }
                is Event.TransactionSigned -> {
//                    Column {
//                        Text("Transaction signed")
//
//                        if (decodedTransaction != null) {
//                            TransactionPreviewField(
//                                cardSet = decodedTransaction
//                            )
//                        }
//                        Text("Signed by:")
//                        Row {
//                            Identicon(
//                                identicon = signedBy?.identicon ?: listOf()
//                            )
//                            Column {
//                                Text(verifierDetails?.publicKey ?: "")
//                                Text(
//                                    verifierDetails?.encryption ?: ""
//                                )
//                            }
//                        }
//                        Text("In network")
//                        Text(eventVal.signDisplay.networkName)
//                        Text("Comment:")
//                        Text(
//                            eventVal.signDisplay.userComment
//                        )
//                    }
                }
                is Event.TypesAdded -> {
                    //icon: QrCodeScanner
                    binding.tvEvent.text = "New types info loaded"
                    binding.tvEvent.setTextColor(blackColor)
                }
                is Event.TypesRemoved -> {
                    //icon: Remove
                    binding.tvEvent.text = "Types info removed"
                    binding.tvEvent.setTextColor(redColor)
                }
                is Event.UserEntry -> {
                    //icon: Note
                    binding.tvEvent.text = "User entry"
                    binding.tvEvent.setTextColor(blackColor)
                    binding.tvName.text = event.userEntry
                }
                is Event.Warning -> {
                    //icon: Warning
                    binding.tvEvent.text = "Warning!"
                    binding.tvEvent.setTextColor(redColor)
                    binding.tvName.text = event.warning
                }
                is Event.WrongPassword -> {
                    //icon: Warning
                    binding.tvEvent.text = "Wrong password entered"
                    binding.tvEvent.setTextColor(redColor)
                    binding.tvName.text = "operation declined"
                }
                is Event.SeedRemoved -> {
                    //TODO:
                }
            }
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_log, parent, false)
                val binding = ListItemLogBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}