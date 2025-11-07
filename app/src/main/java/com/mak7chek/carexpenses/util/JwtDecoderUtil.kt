// util/JwtDecoderUtil.kt
package com.mak7chek.carexpenses.util

import android.util.Base64
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JwtDecoderUtil @Inject constructor() {

    fun isTokenExpired(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payload = parts[1]

            val json = String(Base64.decode(payload, Base64.URL_SAFE), Charsets.UTF_8)

            val jsonObject = JSONObject(json)

            val expTimestamp = jsonObject.getLong("exp") * 1000L
            val expirationDate = Date(expTimestamp)

            expirationDate.before(Date())

        } catch (e: Exception) {
            e.printStackTrace()
            true
        }
    }
}