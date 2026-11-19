package com.example.riskmapping.ui.riscos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.data.api.Comentario
import com.example.riskmapping.data.api.ComentarioRequest
import com.example.riskmapping.data.api.ConfirmacaoRequest
import com.example.riskmapping.data.api.ResumoConfirmacoes
import com.example.riskmapping.data.api.Risco
import com.example.riskmapping.data.api.StatusRequest
import com.example.riskmapping.data.api.Usuario
import com.example.riskmapping.data.api.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalheEstado(
    val carregando: Boolean = true,
    val risco: Risco? = null,
    val comentarios: List<Comentario> = emptyList(),
    val resumo: ResumoConfirmacoes = ResumoConfirmacoes(0, 0),
    val usuario: Usuario? = null, // quem está logado
    val textoComentario: String = "",
    val ocupado: Boolean = false, // true enquanto uma ação (votar, comentar...) está em andamento
    val erro: String? = null,
    val apagado: Boolean = false, // o risco foi apagado: a tela deve fechar
) {
    private val ehAdmin get() = usuario?.perfil == "ADMIN"

    /** Só o dono do risco ou um ADMIN pode mudar o status ou apagar. */
    val podeAlterarRisco get() = risco != null && usuario != null && (usuario.id == risco.criadoPorId || ehAdmin)

    /** Só o autor do comentário ou um ADMIN pode apagá-lo. */
    fun podeApagarComentario(comentario: Comentario) = usuario != null && (usuario.id == comentario.usuarioId || ehAdmin)
}

/** Busca e altera um risco: dados, votos ("ainda existe"/"não existe mais"), comentários, status. */
class DetalheRiscoViewModel(
    aplicacao: Application,
    savedStateHandle: SavedStateHandle, // traz o "id" que a navegação recebeu
) : AndroidViewModel(aplicacao) {

    private val app = aplicacao as RiskMappingApp
    private val riscoId: Long = savedStateHandle["id"] ?: error("A tela de detalhe precisa do id do risco")

    private val _estado = MutableStateFlow(DetalheEstado())
    val estado: StateFlow<DetalheEstado> = _estado.asStateFlow()

    init {
        // Carrega na abertura e de novo sempre que um risco muda (por exemplo, depois de editar).
        viewModelScope.launch {
            app.riscosMudaram.collect { if (!_estado.value.apagado) carregar() }
        }
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            try {
                app.catalogo.carregar()
                val risco = app.api.risco(riscoId)
                val comentarios = app.api.comentarios(riscoId)
                val resumo = app.api.confirmacoes(riscoId)
                val usuario = app.api.meusDados()
                _estado.update { it.copy(risco = risco, comentarios = comentarios, resumo = resumo, usuario = usuario) }
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(carregando = false) }
            }
        }
    }

    fun nomeDaCategoria(id: Long): String = app.catalogo.nomeDaCategoria(id)

    fun nomeDoBairro(id: Long): String = app.catalogo.nomeDoBairro(id)

    // ---------- ações do usuário ----------

    /** [tipo] é "CONFIRMA" (ainda existe) ou "NEGA" (não existe mais). */
    fun votar(tipo: String) = executar {
        val resumo = app.api.votar(riscoId, ConfirmacaoRequest(tipo))
        _estado.update { it.copy(resumo = resumo) }
    }

    fun mudarStatus(status: String) = executar {
        val risco = app.api.mudarStatus(riscoId, StatusRequest(status))
        _estado.update { it.copy(risco = risco) }
        app.riscosMudaram.update { it + 1 } // a lista de riscos se atualiza
    }

    fun apagarRisco() = executar {
        app.api.apagarRisco(riscoId)
        _estado.update { it.copy(apagado = true) } // antes do aviso, para esta tela não tentar recarregar
        app.riscosMudaram.update { it + 1 }
    }

    fun mudarTextoComentario(texto: String) = _estado.update { it.copy(textoComentario = texto) }

    fun enviarComentario() {
        val texto = _estado.value.textoComentario.trim()
        if (texto.isEmpty()) return
        executar {
            val novo = app.api.comentar(riscoId, ComentarioRequest(texto))
            _estado.update { it.copy(comentarios = it.comentarios + novo, textoComentario = "") }
        }
    }

    fun apagarComentario(comentarioId: Long) = executar {
        app.api.apagarComentario(comentarioId)
        _estado.update { estado -> estado.copy(comentarios = estado.comentarios.filter { it.id != comentarioId }) }
    }

    /** Roda uma ação que fala com a API: marca "ocupado" enquanto roda e mostra o erro, se houver. */
    private fun executar(acao: suspend () -> Unit) {
        if (_estado.value.ocupado) return
        viewModelScope.launch {
            _estado.update { it.copy(ocupado = true, erro = null) }
            try {
                acao()
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(ocupado = false) }
            }
        }
    }
}
