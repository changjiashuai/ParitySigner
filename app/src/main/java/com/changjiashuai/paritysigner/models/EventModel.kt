package com.changjiashuai.paritysigner.models

import io.parity.signer.uniffi.Event

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/6/28 23:56.
 */
data class EventModel(
    val order: String,
    val timestamp: String,
    val event: Event
)
