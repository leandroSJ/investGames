package com.example.investgames.data

import android.content.Context

fun checkRecompensas(
    context: Context,
    totalAportado: Float,
    valorMeta: Float,
    onDesbloquear: (Recompensa) -> Unit
) {
    val prefs = context.getSharedPreferences("recompensas", Context.MODE_PRIVATE)


    // 1. Recompensa de registro
    if (totalAportado > 0f && !prefs.getBoolean("recompensa_1_desbloqueada", false)) {
        val recompensa = recompensas.first { it.id == 1 }
        onDesbloquear(recompensa)
        prefs.edit().putBoolean("recompensa_1_desbloqueada", true).apply()
    }

    // 2. Aportes por valor
    val desbloqueios = listOf(
        2 to (totalAportado in 20f..100f),
        3 to (totalAportado in 200f..300f),
        4 to (totalAportado in 350f .. 399f),
        5 to (totalAportado in 400f .. 449f),
        6 to (totalAportado in 450f .. 499f),
        7 to (totalAportado in 500f .. 549f),
        8 to (totalAportado in 550f .. 599f),
        9 to (totalAportado in 600f .. 649f),
        10 to (totalAportado in 650f .. 699f),
        11 to (totalAportado > 700f )
    )

    for ((id, condition) in desbloqueios) {
        val key = "recompensa_${id}_desbloqueada"
        if (condition && !prefs.getBoolean(key, false)) {
            val recompensa = recompensas.first { it.id == id }
            onDesbloquear(recompensa)
            prefs.edit().putBoolean(key, true).apply()
        }
    }

    // 3. Meta atingida — só libera se totalAportado >= valorMeta
    if (totalAportado >= valorMeta && !prefs.getBoolean("recompensa_12_desbloqueada", false)) {
        val recompensa = recompensas.first { it.id == 12 }
        onDesbloquear(recompensa)
        prefs.edit().putBoolean("recompensa_12_desbloqueada", true).apply()
    }
}
