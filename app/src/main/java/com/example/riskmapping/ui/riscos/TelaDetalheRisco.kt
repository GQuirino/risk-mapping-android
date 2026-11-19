package com.example.riskmapping.ui.riscos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.riskmapping.ui.componentes.BarraSuperior

/** Tela de um risco: dados, votos, status, comentários. */
@Composable
fun TelaDetalheRisco(
    aoVoltar: () -> Unit,
    aoEditar: () -> Unit,
    viewModel: DetalheRiscoViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsState()
    val risco = estado.risco

    // Depois de apagar o risco, não há mais o que mostrar: volta para a lista.
    LaunchedEffect(estado.apagado) {
        if (estado.apagado) aoVoltar()
    }

    Scaffold(topBar = { BarraSuperior("Risco", aoVoltar) }) { espacoDaBarra ->
        when {
            risco != null -> Conteudo(
                estado = estado,
                viewModel = viewModel,
                aoEditar = aoEditar,
                modifier = Modifier.padding(espacoDaBarra),
            )

            estado.carregando -> Column(
                modifier = Modifier.fillMaxSize().padding(espacoDaBarra),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { CircularProgressIndicator() }

            else -> Column(
                modifier = Modifier.fillMaxSize().padding(espacoDaBarra).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(estado.erro ?: "Risco não encontrado", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = viewModel::carregar) { Text("Tentar de novo") }
            }
        }
    }
}

@Composable
private fun Conteudo(
    estado: DetalheEstado,
    viewModel: DetalheRiscoViewModel,
    aoEditar: () -> Unit,
    modifier: Modifier,
) {
    val risco = estado.risco!!
    var confirmandoExclusao by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ---------- dados do risco ----------
        Text(risco.titulo, style = MaterialTheme.typography.headlineSmall)
        Text(
            "${viewModel.nomeDaCategoria(risco.categoriaId)} • ${viewModel.nomeDoBairro(risco.bairroId)}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Gravidade ${risco.gravidade} de 5", style = MaterialTheme.typography.titleSmall)
            Text(rotuloDoStatus(risco.status), style = MaterialTheme.typography.titleSmall, color = corDoStatus(risco.status))
        }
        if (!risco.descricao.isNullOrBlank()) Text(risco.descricao, style = MaterialTheme.typography.bodyMedium)
        Text(
            "Registrado em ${formatarData(risco.criadoEm)} pelo usuário ${risco.criadoPorId}\n" +
                "Local: ${risco.latitude}, ${risco.longitude}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // ---------- votos ----------
        HorizontalDivider()
        Text("Este risco ainda existe?", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { viewModel.votar("CONFIRMA") },
                enabled = !estado.ocupado,
                modifier = Modifier.weight(1f),
            ) { Text("Ainda existe (${estado.resumo.confirmacoes})") }
            OutlinedButton(
                onClick = { viewModel.votar("NEGA") },
                enabled = !estado.ocupado,
                modifier = Modifier.weight(1f),
            ) { Text("Não existe mais (${estado.resumo.negacoes})") }
        }

        // ---------- só para o dono do risco ou ADMIN ----------
        if (estado.podeAlterarRisco) {
            HorizontalDivider()
            Text("Status", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ABERTO", "CONFIRMADO", "RESOLVIDO").forEach { status ->
                    FilterChip(
                        selected = risco.status == status,
                        onClick = { viewModel.mudarStatus(status) },
                        enabled = !estado.ocupado,
                        label = { Text(rotuloDoStatus(status)) },
                    )
                }
            }
            Row {
                TextButton(onClick = aoEditar, enabled = !estado.ocupado) { Text("Editar risco") }
                TextButton(onClick = { confirmandoExclusao = true }, enabled = !estado.ocupado) {
                    Text("Apagar risco", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // ---------- comentários ----------
        HorizontalDivider()
        Text("Comentários (${estado.comentarios.size})", style = MaterialTheme.typography.titleSmall)
        if (estado.comentarios.isEmpty()) {
            Text("Nenhum comentário ainda", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        estado.comentarios.forEach { comentario ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Usuário ${comentario.usuarioId} • ${formatarData(comentario.criadoEm)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(comentario.texto, style = MaterialTheme.typography.bodyMedium)
                }
                if (estado.podeApagarComentario(comentario)) {
                    IconButton(onClick = { viewModel.apagarComentario(comentario.id) }, enabled = !estado.ocupado) {
                        Icon(Icons.Default.Delete, contentDescription = "Apagar comentário")
                    }
                }
            }
        }
        OutlinedTextField(
            value = estado.textoComentario,
            onValueChange = viewModel::mudarTextoComentario,
            label = { Text("Escreva um comentário") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = viewModel::enviarComentario,
            enabled = !estado.ocupado && estado.textoComentario.isNotBlank(),
        ) { Text("Enviar") }

        estado.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    // Confirmação antes de apagar (não dá para desfazer)
    if (confirmandoExclusao) {
        AlertDialog(
            onDismissRequest = { confirmandoExclusao = false },
            title = { Text("Apagar este risco?") },
            text = { Text("Os comentários e votos dele também serão apagados.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoExclusao = false
                    viewModel.apagarRisco()
                }) { Text("Apagar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmandoExclusao = false }) { Text("Cancelar") } },
        )
    }
}
