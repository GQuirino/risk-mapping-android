package com.example.riskmapping.data.api

import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

/** Transforma qualquer erro de uma chamada à API em um texto para mostrar ao usuário. */
fun mensagemDeErro(erro: Throwable): String = when (erro) {
    // A API respondeu com erro (400, 401, 409, 429...). Ela explica no campo "detail".
    is HttpException -> detalheDaApi(erro) ?: "Erro ${erro.code()} no servidor"
    // Nem chegou na API: sem internet, servidor fora do ar ou demorou demais.
    is IOException -> "Sem conexão com o servidor. Verifique a internet e tente de novo."
    else -> "Algo deu errado. Tente de novo."
}

private fun detalheDaApi(erro: HttpException): String? {
    val corpo = erro.response()?.errorBody()?.string() ?: return null
    return try {
        JsonParser.parseString(corpo).asJsonObject.get("detail")?.asString
    } catch (e: Exception) {
        null // o corpo não era o JSON esperado
    }
}
