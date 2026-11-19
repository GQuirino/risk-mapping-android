package com.example.riskmapping.ui.riscos

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Os filtros da lista: o texto do botão e o status que ele pede à API (null = todos).
private val FILTROS = listOf(
    "Todos" to null,
    "Abertos" to "ABERTO",
    "Confirmados" to "CONFIRMADO",
    "Resolvidos" to "RESOLVIDO",
)

/** Aba "Riscos": a lista de riscos, com filtro por status e botão para criar um novo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRiscos(
    aoAbrirRisco: (Long) -> Unit,
    aoCriarRisco: () -> Unit,
    viewModel: ListaRiscosViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Filtro por status
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FILTROS.forEach { (texto, status) ->
                    FilterChip(
                        selected = estado.statusFiltrado == status,
                        onClick = { viewModel.filtrarPorStatus(status) },
                        label = { Text(texto) },
                    )
                }
            }

            when {
                // Primeira busca: ainda não há nada para mostrar
                estado.carregando && estado.riscos.isEmpty() -> Centralizado { CircularProgressIndicator() }

                estado.erro != null -> Centralizado {
                    Text(estado.erro!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::carregar) { Text("Tentar de novo") }
                }

                estado.riscos.isEmpty() -> Centralizado { Text("Nenhum risco encontrado") }

                else -> PullToRefreshBox( // puxar a lista para baixo atualiza
                    isRefreshing = estado.carregando,
                    onRefresh = viewModel::carregar,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp, // sobra espaço para o botão "+"
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(estado.riscos, key = { it.id }) { risco ->
                            Card(onClick = { aoAbrirRisco(risco.id) }, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(risco.titulo, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${viewModel.nomeDaCategoria(risco.categoriaId)} • ${viewModel.nomeDoBairro(risco.bairroId)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text("Gravidade ${risco.gravidade} de 5", style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            rotuloDoStatus(risco.status),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = corDoStatus(risco.status),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Botão "+": abre a tela de novo risco
        FloatingActionButton(
            onClick = aoCriarRisco,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo risco")
        }
    }
}

/** Coloca o conteúdo no meio da área disponível (usado nos estados de carregando, erro e vazio). */
@Composable
private fun Centralizado(conteudo: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        conteudo()
    }
}
