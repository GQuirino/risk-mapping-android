package com.example.riskmapping.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.data.api.LoginRequest
import com.example.riskmapping.data.api.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tudo o que a tela de login precisa mostrar. A tela só desenha este estado. */
data class LoginEstado(
    val email: String = "",
    val senha: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
)

/** A lógica do login. Guarda o estado e fala com a API; não sabe nada de como a tela é desenhada. */
class LoginViewModel(aplicacao: Application) : AndroidViewModel(aplicacao) {

    private val app = aplicacao as RiskMappingApp

    private val _estado = MutableStateFlow(LoginEstado())
    val estado: StateFlow<LoginEstado> = _estado.asStateFlow()

    fun mudarEmail(email: String) = _estado.update { it.copy(email = email, erro = null) }

    fun mudarSenha(senha: String) = _estado.update { it.copy(senha = senha, erro = null) }

    /** Chama a API. Se o login der certo, guarda o token e avisa a tela por [aoLogar]. */
    fun entrar(aoLogar: () -> Unit) {
        val atual = _estado.value
        if (atual.carregando) return
        if (atual.email.isBlank() || atual.senha.isBlank()) {
            _estado.update { it.copy(erro = "Preencha o e-mail e a senha") }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            try {
                val resposta = app.api.login(LoginRequest(atual.email.trim(), atual.senha))
                app.sessao.token = resposta.token
                aoLogar()
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(carregando = false) }
            }
        }
    }
}
