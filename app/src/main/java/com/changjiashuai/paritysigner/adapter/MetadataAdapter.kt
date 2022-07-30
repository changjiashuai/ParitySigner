package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemMetadataBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import io.parity.signer.uniffi.MMetadataRecord

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/4 22:12.
 */
class MetadataAdapter : ListAdapter<MMetadataRecord, MetadataAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((MMetadataRecord) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val mMetadataRecord = it
            holder.bind(mMetadataRecord)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(mMetadataRecord)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MMetadataRecord>() {

            override fun areContentsTheSame(
                oldItem: MMetadataRecord,
                newItem: MMetadataRecord
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: MMetadataRecord,
                newItem: MMetadataRecord
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemMetadataBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(mMetadataRecord: MMetadataRecord) {
            binding.ivLogo.setImageBitmap(mMetadataRecord.metaIdPic.toBitmap())
            binding.tvVersion.text = "version: ${mMetadataRecord.specsVersion}, specname: ${mMetadataRecord.specname}"
            binding.tvHash.text = "hash: ${mMetadataRecord.metaHash}"
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_metadata, parent, false)
                val binding = ListItemMetadataBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}