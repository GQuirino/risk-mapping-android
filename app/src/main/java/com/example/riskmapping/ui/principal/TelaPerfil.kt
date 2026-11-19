package com.example.riskmapping.ui.principal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

/** Mostra quem está logado e tem o botão Sair. */
@Composable
fun TelaPerfil(
    aoSair: () -> Unit,
    viewModel: PerfilViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            estado.carregando -> CircularProgressIndicator()

            estado.usuario != null -> {
                Text(estado.usuario!!.nome, style = MaterialTheme.typography.headlineMedium)
                Text(estado.usuario!!.email, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Perfil: ${estado.usuario!!.perfil}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            estado.erro != null -> {
                Text(estado.erro!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::carregar) { Text("Tentar de novo") }
            }
        }

        Spacer(Modifier.height(32.dp))
        OutlinedButton(onClick = aoSair) { Text("Sair") }
    }
}
