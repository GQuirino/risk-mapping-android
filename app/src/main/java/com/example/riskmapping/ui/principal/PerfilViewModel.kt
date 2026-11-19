package com.example.riskmapping.ui.principal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.data.api.Usuario
import com.example.riskmapping.data.api.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilEstado(
    val carregando: Boolean = true,
    val usuario: Usuario? = null,
    val erro: String? = null,
)

/** Busca os dados do usuário logado (GET /usuarios/me). */
class PerfilViewModel(aplicacao: Application) : AndroidViewModel(aplicacao) {

    private val app = aplicacao as RiskMappingApp

    private val _estado = MutableStateFlow(PerfilEstado())
    val estado: StateFlow<PerfilEstado> = _estado.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            try {
                val usuario = app.api.meusDados()
                _estado.update { it.copy(usuario = usuario) }
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(carregando = false) }
            }
        }
    }
}
