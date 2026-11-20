package com.example.riskmapping.ui.cadastro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.data.api.LoginRequest
import com.example.riskmapping.data.api.RegistrarRequest
import com.example.riskmapping.data.api.mensagemDeErro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CadastroEstado(
    val nome: String = "",
    val email: String = "",
    val senha: String = "",
    val confirmarSenha: String = "",
    val carregando: Boolean = false,
    val erro: String? = null,
)

/** Cria a conta (POST /auth/registrar) e já entra com ela (POST /auth/login). */
class CadastroViewModel(aplicacao: Application) : AndroidViewModel(aplicacao) {

    private val app = aplicacao as RiskMappingApp

    private val _estado = MutableStateFlow(CadastroEstado())
    val estado: StateFlow<CadastroEstado> = _estado.asStateFlow()

    fun mudarNome(nome: String) = _estado.update { it.copy(nome = nome, erro = null) }
    fun mudarEmail(email: String) = _estado.update { it.copy(email = email, erro = null) }
    fun mudarSenha(senha: String) = _estado.update { it.copy(senha = senha, erro = null) }
    fun mudarConfirmarSenha(valor: String) = _estado.update { it.copy(confirmarSenha = valor, erro = null) }

    /** Se tudo der certo, a conta é criada, o token é guardado e [aoCadastrar] é chamado. */
    fun cadastrar(aoCadastrar: () -> Unit) {
        val campos = _estado.value
        if (campos.carregando) return

        // As mesmas regras da API, conferidas aqui para avisar antes de chamar o servidor
        if (campos.nome.isBlank()) return mostrarErro("Informe o seu nome")
        if (campos.email.isBlank()) return mostrarErro("Informe o e-mail")
        if (campos.senha.length < 8) return mostrarErro("A senha deve ter pelo menos 8 caracteres")
        if (campos.senha != campos.confirmarSenha) return mostrarErro("As senhas não conferem")

        viewModelScope.launch {
            _estado.update { it.copy(carregando = true, erro = null) }
            try {
                val email = campos.email.trim()
                app.api.registrar(RegistrarRequest(campos.nome.trim(), email, campos.senha, campos.confirmarSenha))
                val resposta = app.api.login(LoginRequest(email, campos.senha)) // já entra com a conta nova
                app.sessao.token = resposta.token
                aoCadastrar()
            } catch (e: Exception) {
                _estado.update { it.copy(erro = mensagemDeErro(e)) }
            } finally {
                _estado.update { it.copy(carregando = false) }
            }
        }
    }

    private fun mostrarErro(mensagem: String) = _estado.update { it.copy(erro = mensagem) }
}
