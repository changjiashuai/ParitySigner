package com.changjiashuai.paritysigner.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.changjiashuai.paritysigner.models.encodeHex
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import io.parity.signer.uniffi.Action
import io.parity.signer.uniffi.qrparserGetPacketsTotal
import io.parity.signer.uniffi.qrparserTryDecodeQrSequence

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/19 21:02.
 */
class ScanViewModel : AbsViewModel() {

    // Camera stuff
    internal var bucket = arrayOf<String>()
    internal var payload: String = ""

    private val _total = MutableLiveData<Int?>(null)
    val total: LiveData<Int?> = _total

    private val _captured = MutableLiveData<Int?>(null)
    val captured: LiveData<Int?> = _captured

    private val _progress = MutableLiveData(0.0f)
    val progress: LiveData<Float> = _progress

    /**
     * Barcode detecting function.
     * This uses experimental features
     */
    @SuppressLint("UnsafeOptInUsageError")
    fun processFrame(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        if (imageProxy.image == null) return
        val inputImage = InputImage.fromMediaImage(
            imageProxy.image!!,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.forEach {
                    val payloadString = it?.rawBytes?.encodeHex()
                    if (!(bucket.contains(payloadString) || payloadString.isNullOrEmpty())) {
                        if (total.value == null) {
                            try {
                                val proposeTotal =
                                    qrparserGetPacketsTotal(payloadString, true).toInt()
                                if (proposeTotal == 1) {
                                    try {
                                        payload = qrparserTryDecodeQrSequence(
                                            "[\"$payloadString\"]",
                                            true
                                        )
                                        resetScanValues()
                                        pushButton(Action.TRANSACTION_FETCHED, payload)
                                    } catch (e: Exception) {
                                        Log.e("Single frame decode failed", e.toString())
                                    }
                                } else {
                                    bucket += payloadString
                                    _total.value = proposeTotal
                                }
                            } catch (e: Exception) {
                                Log.e("QR sequence length estimation", e.toString())
                            }
                        } else {
                            bucket += payloadString
                            if ((bucket.size + 1) >= (total.value ?: 0)) {
                                try {
                                    payload = qrparserTryDecodeQrSequence(
                                        "[\"" + bucket.joinToString(separator = "\",\"") + "\"]",
                                        true
                                    )
                                    if (payload.isNotEmpty()) {
                                        resetScanValues()
                                        pushButton(Action.TRANSACTION_FETCHED, payload)
                                    }
                                } catch (e: Exception) {
                                    Log.e("failed to parse sequence", e.toString())
                                }
                            }
                            _captured.value = bucket.size
                            _progress.value =
                                ((captured.value ?: 0).toFloat() / ((total.value ?: 1).toFloat()))
                            Log.d("captured", captured.value.toString())
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("Scan failed", it.message.toString())
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }


    /**
     * Clears camera progress
     */
    fun resetScanValues() {
        bucket = arrayOf()
        _captured.value = null
        _total.value = null
        _progress.value = 0.0f
    }

}