package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemSignSufficientCryptoBinding
import com.changjiashuai.paritysigner.ext.toBitmap
import com.changjiashuai.paritysigner.models.abbreviateString
import io.parity.signer.uniffi.MRawKey

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/4 22:12.
 */
class SignSufficientCryptoAdapter :
    ListAdapter<MRawKey, SignSufficientCryptoAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((MRawKey) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val mRawKey = it
            holder.bind(mRawKey)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(mRawKey)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MRawKey>() {

            override fun areContentsTheSame(
                oldItem: MRawKey,
                newItem: MRawKey
            ): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(
                oldItem: MRawKey,
                newItem: MRawKey
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemSignSufficientCryptoBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(mRawKey: MRawKey) {
            binding.ivLogo.setImageBitmap(mRawKey.identicon.toBitmap())
            val path = if (mRawKey.hasPwd) {
                "${mRawKey.seedName}${mRawKey.path}/// Locked account {Lock Icon}"
            } else {
                "${mRawKey.seedName}${mRawKey.path}"
            }
            binding.tvSeedNamePath.text = path
            binding.tvPublicKey.text = "${mRawKey.publicKey.abbreviateString(8)}"
        }

        companion object {

            fun create(parent: ViewGroup): ViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_item_sign_sufficient_crypto, parent, false)
                val binding = ListItemSignSufficientCryptoBinding.bind(view)
                return ViewHolder(binding)
            }
        }
    }

}