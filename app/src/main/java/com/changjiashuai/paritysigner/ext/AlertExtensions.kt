package com.changjiashuai.paritysigner.ext

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.AdapterView
import androidx.annotation.ArrayRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/29 13:21.
 */
fun Context?.showAlert(
    title: String,
    message: String = "",
    cancelText: String = "",
    showCancel: Boolean = false,
    cancelClick: (() -> Unit) = {},
    confirmText: String = "",
    confirmClick: (() -> Unit) = {},
    onDismissListener: ((DialogInterface) -> Unit)? = null
) {
    this?.let {
        MaterialAlertDialogBuilder(it)
            .setTitle(title)
            .setMessage(message)
            .apply {
                if (showCancel) {
                    this.setNegativeButton(cancelText) { dialog, which ->
                        // Respond to negative button press
                        cancelClick.invoke()
                    }
                }
            }
            .setPositiveButton(confirmText) { dialog, which ->
                // Respond to positive button press
                confirmClick.invoke()
            }
            .setCancelable(false)
            .setOnDismissListener { onDismissListener?.invoke(it) }
            .show()
    }
}

fun Context?.showSingleChoiceAlert(
    title: String,
    @ArrayRes itemsId: Int,
    checkedItem: Int,
    cancelText: String = "",
    cancelClick: (() -> Unit) = {},
    listener: DialogInterface.OnClickListener? = null
) {
    this?.let {
        MaterialAlertDialogBuilder(it)
            .setTitle(title)
            .setSingleChoiceItems(itemsId, checkedItem, listener)
//            .setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parent: AdapterView<*>?,
//                    view: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//                }
//            })
            .setNegativeButton(cancelText) { dialog, which ->
                cancelClick.invoke()
            }
            .setCancelable(false)
            .show()
    }
}