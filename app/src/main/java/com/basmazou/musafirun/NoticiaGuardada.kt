package com.basmazou.musafirun

data class NoticiaGuardada(
    val docId: String,
    val titol: String,
    val enllac: String,
    val urlImatge: String?,
    val font: String?,
    val snippet: String?,
    val pubDateMillis: Long?,
    val sharedBy: String? = null,
    val sharedTo: String? = null,
    val timestamp: Long = 0L
)


