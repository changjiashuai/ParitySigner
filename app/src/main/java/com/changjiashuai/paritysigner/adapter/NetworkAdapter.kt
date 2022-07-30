package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemNetworkBinding
import io.parity.signer.uniffi.MmNetwork

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/27 21:46.
 */
class NetworkAdapter : ListAdapter<MmNetwork, NetworkAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((MmNetwork) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val mmNetwork = it
            holder.bind(mmNetwork)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(mmNetwork)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MmNetwork>() {

            override fun areContentsTheSame(oldItem: MmNetwork, newItem: MmNetwork): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: MmNetwork, newItem: MmNetwork): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemNetworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mmNetwork: MmNetwork) {
            when (mmNetwork.logo) {
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
            binding.tvName.text = mmNetwork.title
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_network, parent, false)
                val binding = ListItemNetworkBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}