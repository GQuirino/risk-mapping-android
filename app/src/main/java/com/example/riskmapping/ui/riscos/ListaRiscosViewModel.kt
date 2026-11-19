package com.example.riskmapping.ui.riscos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.data.api.Risco
import com.example.riskmapping.data.api.mensagemDeErro
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListaRiscosEstado(
    val carregando: Boolean = true,
    val riscos: List<Risco> = emptyList(),
    val statusFiltrado: String? = null, // null = todos
    val erro: String? = null,
)

/** Busca a lista de riscos (GET /riscos), com filtro por status. */
class ListaRiscosViewModel(aplicacao: Application) : AndroidViewModel(aplicacao) {

    private val app = aplicacao as RiskMappingApp

    private val _estado = MutableStateFlow(ListaRiscosEstado())
    val estado: StateFlow<ListaRiscosEstado> = _estado.asStateFlow()

    private var busca: Job? = null

    init {
        // Recarrega na abertura e sempre que um risco for criado, alterado ou apagado em outra tela.
        viewModelScope.launch {
            app.riscosMudaram.collect { carregar() }
        }
    }

    fun filtrarPorStatus(status: String?) {
        _estado.update { it.copy(statusFiltrado = status) }
        carregar()
    }

    fun carregar() {
        busca?.cancel() // se o usuário trocou de filtro rápido, vale só a última busca
        busca = viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            try {
                app.catalogo.carregar() // nomes de categorias e bairros
                val riscos = app.api.riscos(_estado.value.statusFiltrado)
                _estado.update { it.copy(riscos = riscos) }
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(carregando = false) }
            }
        }
    }

    fun nomeDaCategoria(id: Long): String = app.catalogo.nomeDaCategoria(id)

    fun nomeDoBairro(id: Long): String = app.catalogo.nomeDoBairro(id)
}
