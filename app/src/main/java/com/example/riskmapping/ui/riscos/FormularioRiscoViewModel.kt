package com.example.riskmapping.ui.riscos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.data.api.Bairro
import com.example.riskmapping.data.api.Categoria
import com.example.riskmapping.data.api.RiscoRequest
import com.example.riskmapping.data.api.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** O que o formulário de risco mostra: os campos digitados e as opções de categoria e bairro. */
data class FormularioRiscoEstado(
    val titulo: String = "",
    val descricao: String = "",
    val categoriaId: Long? = null,
    val bairroId: Long? = null,
    val gravidade: Int = 3,
    val latitude: String = "", // texto: só vira número ao salvar
    val longitude: String = "",
    val categorias: List<Categoria> = emptyList(),
    val bairros: List<Bairro> = emptyList(),
    val editando: Boolean = false, // true = editando um risco que já existe; false = criando
    val carregandoOpcoes: Boolean = true,
    val enviando: Boolean = false,
    val erro: String? = null,
)

/**
 * Serve para criar e para editar um risco: se a navegação trouxer um "id", é edição (o formulário
 * abre preenchido e salva com PUT); sem id, é um risco novo (salva com POST).
 */
class FormularioRiscoViewModel(
    aplicacao: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(aplicacao) {

    private val app = aplicacao as RiskMappingApp
    private val riscoId: Long? = savedStateHandle["id"] // null = risco novo

    private val _estado = MutableStateFlow(FormularioRiscoEstado(editando = riscoId != null))
    val estado: StateFlow<FormularioRiscoEstado> = _estado.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregandoOpcoes = true, erro = null) }
            try {
                app.catalogo.carregar()
                _estado.update { it.copy(categorias = app.catalogo.categorias, bairros = app.catalogo.bairros) }

                // Editando: busca o risco e põe os valores dele nos campos
                if (riscoId != null) {
                    val risco = app.api.risco(riscoId)
                    _estado.update {
                        it.copy(
                            titulo = risco.titulo,
                            descricao = risco.descricao ?: "",
                            categoriaId = risco.categoriaId,
                            bairroId = risco.bairroId,
                            gravidade = risco.gravidade,
                            latitude = risco.latitude.toString(),
                            longitude = risco.longitude.toString(),
                        )
                    }
                }
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(carregandoOpcoes = false) }
            }
        }
    }

    // Cada campo do formulário tem a sua função. Mexer num campo limpa a mensagem de erro.
    fun mudarTitulo(valor: String) = _estado.update { it.copy(titulo = valor, erro = null) }
    fun mudarDescricao(valor: String) = _estado.update { it.copy(descricao = valor, erro = null) }
    fun escolherCategoria(id: Long) = _estado.update { it.copy(categoriaId = id, erro = null) }
    fun escolherBairro(id: Long) = _estado.update { it.copy(bairroId = id, erro = null) }
    fun escolherGravidade(valor: Int) = _estado.update { it.copy(gravidade = valor, erro = null) }
    fun mudarLatitude(valor: String) = _estado.update { it.copy(latitude = valor, erro = null) }
    fun mudarLongitude(valor: String) = _estado.update { it.copy(longitude = valor, erro = null) }

    /** Confere os campos e, se estiver tudo certo, cria (ou atualiza) o risco. Ao terminar, chama [aoSalvar]. */
    fun salvar(aoSalvar: () -> Unit) {
        val campos = _estado.value
        if (campos.enviando) return

        // Cada "?: return" pára aqui e mostra o problema; o que passa já é válido.
        if (campos.titulo.isBlank()) return mostrarErro("Informe o título")
        val categoriaId = campos.categoriaId ?: return mostrarErro("Escolha a categoria")
        val bairroId = campos.bairroId ?: return mostrarErro("Escolha o bairro")
        val latitude = paraNumero(campos.latitude, -90.0, 90.0)
            ?: return mostrarErro("Latitude inválida: use um número entre -90 e 90 (ex: -23.55)")
        val longitude = paraNumero(campos.longitude, -180.0, 180.0)
            ?: return mostrarErro("Longitude inválida: use um número entre -180 e 180 (ex: -46.63)")

        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, erro = null) }
            try {
                val pedido = RiscoRequest(
                    titulo = campos.titulo.trim(),
                    descricao = campos.descricao.trim().ifEmpty { null },
                    latitude = latitude,
                    longitude = longitude,
                    gravidade = campos.gravidade,
                    categoriaId = categoriaId,
                    bairroId = bairroId,
                )
                if (riscoId == null) app.api.criarRisco(pedido) else app.api.atualizarRisco(riscoId, pedido)
                app.riscosMudaram.update { it + 1 } // a lista de riscos se atualiza
                aoSalvar()
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(enviando = false) }
            }
        }
    }

    private fun mostrarErro(mensagem: String) = _estado.update { it.copy(erro = mensagem) }

    /** "-23,55" ou "-23.55" -> -23.55; devolve null se não for número ou estiver fora do intervalo. */
    private fun paraNumero(texto: String, minimo: Double, maximo: Double): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it in minimo..maximo }
}
