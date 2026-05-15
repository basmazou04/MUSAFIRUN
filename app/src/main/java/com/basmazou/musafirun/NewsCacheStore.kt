package com.basmazou.musafirun

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object NewsCacheStore {
    private const val PREFS = "news_cache"
    private const val KEY_PREFIX = "feed_"

    fun desar(context: Context, codiIdioma: String, items: List<Principal.Noticia>) {
        if (items.isEmpty()) return
        val arr = JSONArray()
        for (n in items) {
            val obj = JSONObject()
                .put("titol", n.titol)
                .put("enllac", n.enllac)
                .put("urlImatge", n.urlImatge ?: "")
                .put("font", n.font ?: "")
                .put("snippet", n.snippet ?: "")
                .put("pubDateMillis", n.pubDateMillis ?: 0L)
            arr.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PREFIX + codiIdioma, arr.toString())
            .putLong(KEY_PREFIX + codiIdioma + "_ts", System.currentTimeMillis())
            .apply()
    }

    fun carregar(context: Context, codiIdioma: String): List<Principal.Noticia> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PREFIX + codiIdioma, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val result = mutableListOf<Principal.Noticia>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                result.add(
                    Principal.Noticia(
                        titol = obj.optString("titol"),
                        enllac = obj.optString("enllac"),
                        urlImatge = obj.optString("urlImatge").takeIf { it.isNotBlank() },
                        font = obj.optString("font").takeIf { it.isNotBlank() },
                        snippet = obj.optString("snippet").takeIf { it.isNotBlank() },
                        pubDateMillis = obj.optLong("pubDateMillis", 0L).takeIf { it > 0L }
                    )
                )
            }
            result
        } catch (_: Exception) {
            emptyList()
        }
    }
}


