package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemDerivedKeyBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import io.parity.signer.uniffi.MKeysCard

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/2 15:30.
 */
class DerivedKeyAdapter : ListAdapter<MKeysCard, DerivedKeyAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((MKeysCard) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val mKeysCard = it
            holder.bind(mKeysCard)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(mKeysCard)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MKeysCard>() {

            override fun areContentsTheSame(oldItem: MKeysCard, newItem: MKeysCard): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: MKeysCard, newItem: MKeysCard): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemDerivedKeyBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(mKeysCard: MKeysCard) {
            binding.ivLogo.setImageBitmap(mKeysCard.identicon.toBitmap())
            binding.tvPath.text = mKeysCard.path
            binding.tvAddressKey.text = mKeysCard.addressKey
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_derived_key, parent, false)
                val binding = ListItemDerivedKeyBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}