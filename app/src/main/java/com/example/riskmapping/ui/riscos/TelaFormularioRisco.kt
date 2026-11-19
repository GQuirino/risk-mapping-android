package com.example.riskmapping.ui.riscos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.riskmapping.ui.componentes.BarraSuperior

/** Formulário de risco: serve para criar um novo ou editar um existente. Ao salvar, chama [aoSalvar]. */
@Composable
fun TelaFormularioRisco(
    aoVoltar: () -> Unit,
    aoSalvar: () -> Unit,
    viewModel: FormularioRiscoViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(topBar = { BarraSuperior(if (estado.editando) "Editar risco" else "Novo risco", aoVoltar) }) { espacoDaBarra ->
        Column(
            modifier = Modifier
                .padding(espacoDaBarra)
                .imePadding() // sobe o formulário quando o teclado abre
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = viewModel::mudarTitulo,
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = estado.descricao,
                onValueChange = viewModel::mudarDescricao,
                label = { Text("Descrição (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Categoria", style = MaterialTheme.typography.titleSmall)
            estado.categorias.forEach { categoria ->
                Opcao(
                    texto = categoria.nome,
                    marcada = estado.categoriaId == categoria.id,
                    aoEscolher = { viewModel.escolherCategoria(categoria.id) },
                )
            }

            Text("Bairro", style = MaterialTheme.typography.titleSmall)
            estado.bairros.forEach { bairro ->
                Opcao(
                    texto = "${bairro.nome} (${bairro.cidade}/${bairro.estado})",
                    marcada = estado.bairroId == bairro.id,
                    aoEscolher = { viewModel.escolherBairro(bairro.id) },
                )
            }

            Text("Gravidade (1 = leve, 5 = gravíssimo)", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { nivel ->
                    FilterChip(
                        selected = estado.gravidade == nivel,
                        onClick = { viewModel.escolherGravidade(nivel) },
                        label = { Text("$nivel") },
                    )
                }
            }

            Text("Local", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = estado.latitude,
                    onValueChange = viewModel::mudarLatitude,
                    label = { Text("Latitude") },
                    placeholder = { Text("-23.55") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = estado.longitude,
                    onValueChange = viewModel::mudarLongitude,
                    label = { Text("Longitude") },
                    placeholder = { Text("-46.63") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            estado.erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { viewModel.salvar(aoSalvar) },
                enabled = !estado.enviando && !estado.carregandoOpcoes,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (estado.enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Salvar")
                }
            }
        }
    }
}

/** Uma linha com bolinha de seleção e texto. Tocar em qualquer parte da linha escolhe a opção. */
@Composable
private fun Opcao(texto: String, marcada: Boolean, aoEscolher: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = marcada, onClick = aoEscolher, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = marcada, onClick = null)
        Text(texto, modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp))
    }
}
