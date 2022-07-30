package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemSeedBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import io.parity.signer.uniffi.SeedNameCard

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/26 20:34.
 */
class SeedAdapter : ListAdapter<SeedNameCard, SeedAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((SeedNameCard) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val seedNameCard = it
            holder.bind(seedNameCard)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(seedNameCard)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<SeedNameCard>() {

            override fun areContentsTheSame(oldItem: SeedNameCard, newItem: SeedNameCard): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: SeedNameCard, newItem: SeedNameCard): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemSeedBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(seedNameCard: SeedNameCard) {
            binding.ivLogo.setImageBitmap(seedNameCard.identicon.toBitmap())
            binding.tvName.text = seedNameCard.seedName
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_seed, parent, false)
                val binding = ListItemSeedBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}