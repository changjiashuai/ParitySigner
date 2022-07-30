package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemDerivationBinding
import io.parity.signer.uniffi.DerivationPack

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/7 22:07.
 */
class DerivationAdapter : ListAdapter<DerivationPack, DerivationAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((DerivationPack) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val derivationPack = it
            holder.bind(derivationPack)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(derivationPack)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DerivationPack>() {

            override fun areContentsTheSame(
                oldItem: DerivationPack,
                newItem: DerivationPack
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: DerivationPack,
                newItem: DerivationPack
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemDerivationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var paths = listOf<String>()

        fun bind(derivationPack: DerivationPack) {
            when (derivationPack.networkLogo) {
                "polkadot" -> {
                    binding.ivLogo.setImageResource(R.drawable.ic_polkadot_new_dot_logo)
                }
                "kusama" -> {
                    binding.ivLogo.setImageResource(R.drawable.ic_kusama_ksm_logo)
                }
                "westend" -> {
                    binding.ivLogo.setImageResource(R.drawable.ic_polkadot_dot_logo)
                }
            }
            binding.tvName.text = derivationPack.networkTitle

            val networkDerivations = derivationPack.idSet.sortedBy { it.path }

            for (record in networkDerivations) {
                if (record.path.isBlank()) {
                    paths = paths.plus("seed key")
                } else {
                    paths = paths.plus(record.path)
                    if (record.hasPwd) {
                        binding.tvPwd.text = "/// Password protected {Lock Icon}"
                    }
                }
            }
            binding.tvSeedPath.text = paths.joinToString("\n")
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_derivation, parent, false)
                val binding = ListItemDerivationBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}