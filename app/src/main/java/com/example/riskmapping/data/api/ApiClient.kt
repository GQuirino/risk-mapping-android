package com.example.riskmapping.data.api

import com.example.riskmapping.data.Sessao
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Endereço da API publicada no Render. Termina com "/" (exigência do Retrofit).
private const val URL_BASE = "https://risk-mapping-api.onrender.com/api/v1/"

/** Monta o cliente da API. É chamado uma única vez, em RiskMappingApp. */
fun criarApi(sessao: Sessao): RiskApi {
    val cliente = OkHttpClient.Builder()
        // O Render gratuito "dorme" quando não é usado: a 1ª chamada pode levar cerca de 1 minuto.
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        // Em toda chamada, se estivermos logados, envia o token ("Authorization: Bearer <token>").
        .addInterceptor { cadeia ->
            val pedido = cadeia.request().newBuilder()
            sessao.token?.let { token -> pedido.header("Authorization", "Bearer $token") }
            val resposta = cadeia.proceed(pedido.build())

            // 401 com token enviado = o token venceu (dura 120 minutos): desloga e volta ao login.
            // (O 401 de "senha errada" no login não entra aqui, pois ainda não há token.)
            if (resposta.code() == 401 && sessao.estaLogado()) sessao.expirar()
            resposta
        }
        .build()

    return Retrofit.Builder()
        .baseUrl(URL_BASE)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create()) // JSON <-> classes Kotlin
        .build()
        .create(RiskApi::class.java)
}
