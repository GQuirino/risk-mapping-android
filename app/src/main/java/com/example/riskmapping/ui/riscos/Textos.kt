package com.example.riskmapping.ui.riscos

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pequenas funções que transformam os dados da API em texto para a tela.

/** "ABERTO" -> "Aberto" */
fun rotuloDoStatus(status: String): String = when (status) {
    "ABERTO" -> "Aberto"
    "CONFIRMADO" -> "Confirmado"
    "RESOLVIDO" -> "Resolvido"
    else -> status
}

/** "2026-09-03T18:20:00" -> "03/09/2026" */
fun formatarData(iso: String?): String {
    val partes = iso?.take(10)?.split("-") ?: return ""
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else ""
}

/** Cor do texto de cada status. */
@Composable
fun corDoStatus(status: String): Color = when (status) {
    "ABERTO" -> MaterialTheme.colorScheme.error
    "CONFIRMADO" -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary // RESOLVIDO
}
