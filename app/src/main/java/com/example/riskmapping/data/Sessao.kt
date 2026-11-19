package com.example.riskmapping.data

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Guarda o token de login no aparelho, para o usuário continuar logado ao fechar o app.
 * Usa SharedPreferences em modo privado: só este app consegue ler o arquivo.
 */
class Sessao(context: Context) {

    private val preferencias = context.getSharedPreferences("sessao", Context.MODE_PRIVATE)

    /** O token salvo, ou null se ninguém estiver logado. */
    var token: String?
        get() = preferencias.getString(CHAVE_TOKEN, null)
        set(valor) {
            val edicao = preferencias.edit()
            if (valor == null) edicao.remove(CHAVE_TOKEN) else edicao.putString(CHAVE_TOKEN, valor)
            edicao.apply()
        }

    fun estaLogado(): Boolean = token != null

    /** Logout: esquece o token. */
    fun sair() {
        token = null
    }

    // Avisa a navegação quando a API recusa o token (venceu ou foi apagado): ela volta para o login.
    private val _expirou = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expirou: SharedFlow<Unit> = _expirou.asSharedFlow()

    /** Chamada pelo ApiClient quando a API responde 401 estando logado. */
    fun expirar() {
        token = null
        _expirou.tryEmit(Unit)
    }

    private companion object {
        const val CHAVE_TOKEN = "token"
    }
}
