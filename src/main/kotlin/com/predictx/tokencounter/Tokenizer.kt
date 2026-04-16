package com.predictx.tokencounter

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingType

object Tokenizer {
    private val encoding: Encoding by lazy {
        Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.O200K_BASE)
    }

    fun count(text: String): Int =
        if (text.isEmpty()) 0 else encoding.countTokens(text)
}
