package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemNetworkBinding
import io.parity.signer.uniffi.MmNetwork
import io.parity.signer.uniffi.Network

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/9 12:14.
 */
class NetworkSelectorAdapter : ListAdapter<Network, NetworkSelectorAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((Network) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val network = it
            holder.bind(network)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(network)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Network>() {

            override fun areContentsTheSame(oldItem: Network, newItem: Network): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: Network, newItem: Network): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemNetworkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(network: Network) {
            when (network.logo) {
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
            binding.tvName.text = network.title
            if (network.selected) {
                binding.ivCheck.visibility = View.VISIBLE
            } else {
                binding.ivCheck.visibility = View.GONE
            }
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