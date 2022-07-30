package com.changjiashuai.paritysigner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.ListItemLogBinding
import io.parity.signer.uniffi.History

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/26 19:33.
 */
class LogAdapter : ListAdapter<History, LogAdapter.ViewHolder>(DIFF) {

    var onItemClick: ((History) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let {
            val history = it
            holder.bind(history)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(history)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<History>() {

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }
        }
    }

    class ViewHolder(
        private val binding: ListItemLogBinding
    ) : RecyclerView.ViewHolder(binding.root) {


        fun bind(history: History) {
            val timestamp = history.timestamp
            val timestampStr = if (timestamp.length > 16) timestamp.substring(0, 16) else timestamp

            binding.tvTime.text = timestampStr
            binding.tvEvent.text = history.order.toString()
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