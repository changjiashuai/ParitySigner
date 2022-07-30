package com.changjiashuai.paritysigner.ext

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.changjiashuai.paritysigner.R
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/3 22:16.
 */
fun Context.showInfoSheet(
    contentView: View,
    onDismissListener: ((DialogInterface) -> Unit)? = null
): BottomSheetDialog {
    val bottomSheetDialog = BottomSheetDialog(this)
    bottomSheetDialog.setContentView(contentView)
    bottomSheetDialog.setOnDismissListener {
        onDismissListener?.invoke(it)
    }
    bottomSheetDialog.setCancelable(false)
    bottomSheetDialog.setCanceledOnTouchOutside(false)
    bottomSheetDialog.show()
    return bottomSheetDialog
}

fun Context.showWarnSheet(
    title: String? = null,
    actionText: String = "",
    actionClick: ((DialogInterface) -> Unit)? = null,
    onDismissListener: ((DialogInterface) -> Unit)? = null
): BottomSheetDialog {
    val bottomSheetDialog = BottomSheetDialog(this)
    val oneActionView = View.inflate(this, R.layout.sheet_warn_action, null)
    val tvTitle = oneActionView.findViewById<TextView>(R.id.tv_title)
    val vDivider = oneActionView.findViewById<View>(R.id.v_divider)
    val btnAction = oneActionView.findViewById<Button>(R.id.btn_action)
    if (title == null) {
        tvTitle.visibility = View.GONE
        vDivider.visibility = View.GONE
    } else {
        tvTitle.visibility = View.VISIBLE
        vDivider.visibility = View.VISIBLE
    }
    tvTitle.text = title
    btnAction.text = actionText
    btnAction.setOnClickListener {
        actionClick?.invoke(bottomSheetDialog)
    }
    bottomSheetDialog.setContentView(oneActionView)
    bottomSheetDialog.setOnDismissListener {
        onDismissListener?.invoke(it)
    }
    bottomSheetDialog.show()
    return bottomSheetDialog
}

fun Context.showErrorSheet() {

}

fun Context.showDebugSheet() {

}

//one action with cancel
fun Context.showSheetStyle1(
    title: String? = null,
    actionText: String = "",
    actionClick: (() -> Unit)? = null,
    cancelText: String = "",
    cancelClick: (() -> Unit)? = null,
    onDismissListener: ((DialogInterface) -> Unit)? = null
) {
    val bottomSheetDialog = BottomSheetDialog(this)
    val oneActionView = View.inflate(this, R.layout.sheet_one_action, null)
    val tvTitle = oneActionView.findViewById<TextView>(R.id.tv_title)
    val vDivider = oneActionView.findViewById<View>(R.id.v_divider)
    val btnAction = oneActionView.findViewById<Button>(R.id.btn_action1)
    val btnCancel = oneActionView.findViewById<Button>(R.id.btn_cancel)
    if (title == null) {
        tvTitle.visibility = View.GONE
        vDivider.visibility = View.GONE
    } else {
        tvTitle.visibility = View.VISIBLE
        vDivider.visibility = View.VISIBLE
    }
    tvTitle.text = title
    btnAction.text = actionText
    btnCancel.text = cancelText
    btnAction.setOnClickListener {
        bottomSheetDialog.dismiss()
        actionClick?.invoke()
    }
    btnCancel.setOnClickListener {
        bottomSheetDialog.dismiss()
        cancelClick?.invoke()
    }
    bottomSheetDialog.setContentView(oneActionView)
    bottomSheetDialog.setOnDismissListener {
        onDismissListener?.invoke(it)
    }
    bottomSheetDialog.setCanceledOnTouchOutside(false)
    bottomSheetDialog.setCancelable(false)
    bottomSheetDialog.show()
}

//two action with cancel
fun Context.showSheetStyle2(
    title: String? = null,
    actionText: String = "",
    actionClick: (() -> Unit)? = null,
    action2Text: String = "",
    action2Click: (() -> Unit)? = null,
    cancelText: String = "",
    cancelClick: (() -> Unit)? = null,
    onDismissListener: ((DialogInterface) -> Unit)? = null
): BottomSheetDialog {
    val bottomSheetDialog = BottomSheetDialog(this)
    val oneActionView = View.inflate(this, R.layout.sheet_two_action, null)
    val tvTitle = oneActionView.findViewById<TextView>(R.id.tv_title)
    val vDivider = oneActionView.findViewById<View>(R.id.v_divider)
    val btnAction = oneActionView.findViewById<Button>(R.id.btn_action1)
    val btnAction2 = oneActionView.findViewById<Button>(R.id.btn_action2)
    val btnCancel = oneActionView.findViewById<Button>(R.id.btn_cancel)
    if (title == null) {
        tvTitle.visibility = View.GONE
        vDivider.visibility = View.GONE
    } else {
        tvTitle.visibility = View.VISIBLE
        vDivider.visibility = View.VISIBLE
    }
    tvTitle.text = title
    btnAction.text = actionText
    btnAction2.text = action2Text
    btnCancel.text = cancelText
    btnAction.setOnClickListener {
        bottomSheetDialog.dismiss()
        actionClick?.invoke()
    }
    btnAction2.setOnClickListener {
        bottomSheetDialog.dismiss()
        action2Click?.invoke()
    }
    btnCancel.setOnClickListener {
        bottomSheetDialog.dismiss()
        cancelClick?.invoke()
    }
    bottomSheetDialog.setContentView(oneActionView)
    bottomSheetDialog.setOnDismissListener {
        onDismissListener?.invoke(it)
    }
    bottomSheetDialog.setCancelable(false)
    bottomSheetDialog.setCanceledOnTouchOutside(false)
    bottomSheetDialog.show()
    return bottomSheetDialog
}

//three action with cancel
fun Context.showSheetStyle3(
    title: String? = null,
    actionText: String = "",
    actionClick: (() -> Unit)? = null,
    action2Text: String = "",
    action2Click: (() -> Unit)? = null,
    action3Text: String = "",
    action3Click: (() -> Unit)? = null,
    cancelText: String = "",
    cancelClick: (() -> Unit)? = null,
    onDismissListener: ((DialogInterface) -> Unit)? = null
) {
    val bottomSheetDialog = BottomSheetDialog(this)
    val oneActionView = View.inflate(this, R.layout.sheet_three_action, null)
    val tvTitle = oneActionView.findViewById<TextView>(R.id.tv_title)
    val vDivider = oneActionView.findViewById<View>(R.id.v_divider)
    val btnAction = oneActionView.findViewById<Button>(R.id.btn_action1)
    val btnAction2 = oneActionView.findViewById<Button>(R.id.btn_action2)
    val btnAction3 = oneActionView.findViewById<Button>(R.id.btn_action3)
    val btnCancel = oneActionView.findViewById<Button>(R.id.btn_cancel)
    if (title == null) {
        tvTitle.visibility = View.GONE
        vDivider.visibility = View.GONE
    } else {
        tvTitle.visibility = View.VISIBLE
        vDivider.visibility = View.VISIBLE
    }
    tvTitle.text = title
    btnAction.text = actionText
    btnAction2.text = action2Text
    btnAction3.text = action3Text
    btnCancel.text = cancelText
    btnAction.setOnClickListener {
        bottomSheetDialog.dismiss()
        actionClick?.invoke()
    }
    btnAction2.setOnClickListener {
        bottomSheetDialog.dismiss()
        action2Click?.invoke()
    }
    btnAction3.setOnClickListener {
        bottomSheetDialog.dismiss()
        action3Click?.invoke()
    }
    btnCancel.setOnClickListener {
        bottomSheetDialog.dismiss()
        cancelClick?.invoke()
    }
    bottomSheetDialog.setContentView(oneActionView)
    bottomSheetDialog.setOnDismissListener {
        onDismissListener?.invoke(it)
    }
    bottomSheetDialog.setCancelable(false)
    bottomSheetDialog.setCanceledOnTouchOutside(false)
    bottomSheetDialog.show()
}