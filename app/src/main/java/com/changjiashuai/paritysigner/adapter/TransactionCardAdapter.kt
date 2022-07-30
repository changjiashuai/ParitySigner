package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemTransactionBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.decodeHex
import com.changjiashuai.paritysigner.models.encodeHex
import io.parity.signer.uniffi.Card
import io.parity.signer.uniffi.TransactionCard

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/13 22:40.
 */
class TransactionCardAdapter :
    ListAdapter<TransactionCard, TransactionCardAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((TransactionCard) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val transactionCard = it
            holder.bind(transactionCard)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(transactionCard)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<TransactionCard>() {

            override fun areContentsTheSame(
                oldItem: TransactionCard,
                newItem: TransactionCard
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: TransactionCard,
                newItem: TransactionCard
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transactionCard: TransactionCard) {
            val card = transactionCard.card
            //TODO...
            when (card) {
                is Card.AuthorCard -> {
                    val address = card.f
                    binding.ivLogo.setImageBitmap(address.identicon.toBitmap())
                    binding.tvFrom.text = "From: " + "${address.seedName} ${address.path} "
                    if (address.hasPwd) {
                        binding.tvContent.text = "/// {Password protected account Icon}"
                    }
                    binding.tvContent2.text = "${address.base58}"
                }
                is Card.AuthorPublicKeyCard -> {
                    val mVerifierDetails = card.f
                    binding.ivLogo.setImageBitmap(mVerifierDetails.identicon.toBitmap())
                    binding.tvContent.text =
                        "Signed with ${mVerifierDetails.encryption} ${mVerifierDetails.publicKey}"
                }
                is Card.AuthorPlainCard -> {
                    val mscAuthorPlain = card.f
                    binding.ivLogo.setImageBitmap(mscAuthorPlain.identicon.toBitmap())
                    binding.tvFrom.text = "From ${mscAuthorPlain.base58}"
                }
                is Card.BalanceCard -> {
                    val mscCurrency = card.f
                    binding.tvFrom.text = "${mscCurrency.amount} ${mscCurrency.units}"
                }
                is Card.BitVecCard -> {
                    val name = card.f
                    binding.tvFrom.text = "$name"
                }
                is Card.BlockHashCard -> {
                    val blockHash = card.f
                    binding.tvFrom.text = "Block hash $blockHash"
                }
                is Card.CallCard -> {
                    val mscCall = card.f
                    binding.tvFrom.text = "Method: ${mscCall.methodName}"
                }
                is Card.DefaultCard -> {
                    val defaultCard = card.f
                    binding.tvFrom.text = "$defaultCard"
                }
                is Card.DerivationsCard -> {
                    val derivations = card.f
                    binding.tvFrom.text = "Importing derivations:"
                    var content = ""
                    derivations.forEach { derivation ->
                        content = " $derivation"
                    }
                    binding.tvContent.text = content
                }
                is Card.EnumVariantNameCard -> {
                    val mscEnumVariantName = card.f
                    binding.tvFrom.text = "name: ${mscEnumVariantName.name}"
                }
                is Card.EraImmortalCard -> {
                    binding.tvFrom.text = "Immortal"
                }
                is Card.EraMortalCard -> {
                    val mscEraMortal = card.f
                    binding.tvFrom.text = "phase: ${mscEraMortal.phase}"
                    binding.tvContent.text = "period: ${mscEraMortal.period}"
                }
                is Card.ErrorCard -> {
                    val error = card.f
                    binding.tvFrom.text = "Error! $error"
                }
                is Card.FieldNameCard -> {
                    val mscFieldName = card.f
                    binding.tvFrom.text = "name: ${mscFieldName.name}"
                }
                is Card.FieldNumberCard -> {
                    val mscFieldNumber = card.f
                    binding.tvFrom.text = "number: ${mscFieldNumber.number}"
                }
                is Card.IdCard -> {
                    val mscId = card.f
                    binding.ivLogo.setImageBitmap(mscId.identicon.toBitmap())
                    binding.tvFrom.text = "base58: ${mscId.base58}"
                }
                is Card.IdentityFieldCard -> {
                    val identity = card.f
                    binding.tvFrom.text = "IdentityField: $identity"
                }
                is Card.MetaCard -> {
                    val mMetadataRecord = card.f
                    binding.ivLogo.setImageBitmap(mMetadataRecord.metaIdPic.toBitmap())
                    binding.tvFrom.text =
                        "Add metadata ${mMetadataRecord.specsVersion} ${mMetadataRecord.metaHash}"
                }
                is Card.NameVersionCard -> {
                    val mscNameVersion = card.f
                    binding.tvFrom.text =
                        "name: ${mscNameVersion.name} value:${mscNameVersion.name}"
                }
                is Card.NetworkGenesisHashCard -> {
                    val genesisHash = card.f
                    binding.tvFrom.text = "Genesis hash $genesisHash"
                }
                is Card.NetworkNameCard -> {
                    val networkName = card.f
                    binding.tvFrom.text = "Network name $networkName"
                }
                is Card.NetworkInfoCard -> {
                    val mscNetworkInfo = card.f
                    binding.tvFrom.text = mscNetworkInfo.networkLogo
                    binding.tvContent.text = mscNetworkInfo.networkTitle
                }
                is Card.NewSpecsCard -> {
                    val newSpecs = card.f
                    binding.tvFrom.text = "NEW NETWORK"
                    binding.tvContent.text = "Network name: ${newSpecs.title}" +
                            "base58 prefix:: ${newSpecs.base58prefix}" +
                            "decimals: ${newSpecs.decimals}" +
                            "unit: ${newSpecs.unit}" +
                            "genesis hash: ${newSpecs.genesisHash}" +
                            "crypto: ${newSpecs.encryption}" +
                            "spec name: ${newSpecs.name}"
                    binding.tvContent2.text = "logo: ${newSpecs.logo}" +
                            "default path: ${newSpecs.pathId}"
                }
                is Card.NonceCard -> {
                    val nonce = card.f
                    binding.tvFrom.text = "nonce: $nonce"
                }
                is Card.NoneCard -> {
                    binding.tvFrom.text = "None"
                }
                is Card.PalletCard -> {
                    val pallet = card.f
                    binding.tvFrom.text = "Pallet: $pallet"
                }
                is Card.TextCard -> {
                    val text = card.f
                    binding.tvFrom.text = String(text.decodeHex())
                }
                is Card.TipCard -> {
                    val mscCurrency = card.f
                    binding.tvFrom.text = "Tip: ${mscCurrency.amount} ${mscCurrency.units}"
                }
                is Card.TipPlainCard -> {
                    val tip = card.f
                    binding.tvFrom.text = "Tip: $tip"
                }
                is Card.TxSpecCard -> {
                    val txSpec = card.f
                    binding.tvFrom.text = "TX version: $txSpec"
                }
                is Card.TxSpecPlainCard -> {
                    val mscTxSpecPlain = card.f
                    binding.tvFrom.text = "Unknown network"
                    binding.tvContent.text = "Genesis hash: ${
                        mscTxSpecPlain.networkGenesisHash.toUByteArray().toByteArray().encodeHex()
                    }"
                    binding.tvContent2.text = "Version: ${mscTxSpecPlain.version}" +
                            "Tx Version: ${mscTxSpecPlain.txVersion}"
                }
                is Card.TypesInfoCard -> {
                    val mTypesInfo = card.f
                    binding.ivLogo.setImageBitmap(mTypesInfo.typesIdPic?.toBitmap())
                    binding.tvFrom.text = "Types hash: ${mTypesInfo.typesHash}"
                }
                is Card.VarNameCard -> {
                    val name = card.f
                    binding.tvFrom.text = "$name"
                }
                is Card.VerifierCard -> {
                    val mVerifierDetails = card.f
                    binding.ivLogo.setImageBitmap(mVerifierDetails.identicon.toBitmap())
                    binding.tvFrom.text = "VERIFIER CERTIFICATE"
                    binding.tvContent.text = "key: ${mVerifierDetails.publicKey}"
                    binding.tvContent2.text = "crypto: ${mVerifierDetails.encryption}"
                }
                is Card.WarningCard -> {
                    val warn = card.f
                    binding.tvFrom.text = "Warning! $warn"
                }
            }
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_transaction, parent, false)
                val binding = ListItemTransactionBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}