package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemDerivedKeyBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import io.parity.signer.uniffi.MKeysCard

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/2 15:30.
 */
class DerivedKeyAdapter : ListAdapter<MKeysCard, DerivedKeyAdapter.ViewHolder>(DIFF) {

    var tracker: SelectionTracker<Long>? = null
    var onItemClick: ((MKeysCard) -> Unit)? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val mKeysCard = it
            holder.bind(
                mKeysCard,
                tracker?.isSelected(position.toLong()) ?: false,
                tracker?.hasSelection() ?: false
            )
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

        fun bind(
            mKeysCard: MKeysCard,
            isActivated: Boolean = false,
            showCheckButton: Boolean = false
        ) {
            itemView.isActivated = isActivated
            if (showCheckButton) {
                binding.cbCheck.visibility = View.VISIBLE
            } else {
                binding.cbCheck.visibility = View.GONE
            }
            binding.cbCheck.isChecked = isActivated
            binding.ivLogo.setImageBitmap(mKeysCard.identicon.toBitmap())
            binding.tvPath.text = mKeysCard.path
            binding.tvAddressKey.text = mKeysCard.base58.abbreviateString(8)
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> = object :
            ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int {
                return adapterPosition
            }

            override fun getSelectionKey(): Long? {
                return itemId
            }
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

    class DerivedKeyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as DerivedKeyAdapter.ViewHolder).getItemDetails()
            }
            return null
        }

    }
}