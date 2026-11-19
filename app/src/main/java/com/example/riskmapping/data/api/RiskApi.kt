package com.example.riskmapping.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * As rotas da API. Cada função é uma chamada HTTP; o Retrofit escreve o código de rede.
 * O endereço de cada rota é relativo à URL base definida em ApiClient.kt.
 */
interface RiskApi {

    // ---------- conta ----------

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @POST("auth/registrar")
    suspend fun registrar(@Body request: RegistrarRequest): Usuario

    // Rota protegida: o ApiClient envia o token sozinho no cabeçalho Authorization.
    @GET("usuarios/me")
    suspend fun meusDados(): Usuario

    // ---------- categorias e bairros (listas fixas, só leitura) ----------

    @GET("categorias")
    suspend fun categorias(): List<Categoria>

    @GET("bairros")
    suspend fun bairros(): List<Bairro>

    // ---------- riscos ----------

    // status = null lista todos; ou "ABERTO", "CONFIRMADO", "RESOLVIDO"
    @GET("riscos")
    suspend fun riscos(@Query("status") status: String?): List<Risco>

    @GET("riscos/{id}")
    suspend fun risco(@Path("id") id: Long): Risco

    @POST("riscos")
    suspend fun criarRisco(@Body request: RiscoRequest): Risco

    @PUT("riscos/{id}")
    suspend fun atualizarRisco(@Path("id") id: Long, @Body request: RiscoRequest): Risco

    @PATCH("riscos/{id}/status")
    suspend fun mudarStatus(@Path("id") id: Long, @Body request: StatusRequest): Risco

    @DELETE("riscos/{id}")
    suspend fun apagarRisco(@Path("id") id: Long)

    // ---------- comentários ----------

    @GET("riscos/{id}/comentarios")
    suspend fun comentarios(@Path("id") riscoId: Long): List<Comentario>

    @POST("riscos/{id}/comentarios")
    suspend fun comentar(@Path("id") riscoId: Long, @Body request: ComentarioRequest): Comentario

    @DELETE("comentarios/{id}")
    suspend fun apagarComentario(@Path("id") comentarioId: Long)

    // ---------- confirmações ("ainda existe" / "não existe mais") ----------

    @GET("riscos/{id}/confirmacoes")
    suspend fun confirmacoes(@Path("id") riscoId: Long): ResumoConfirmacoes

    // Cada usuário tem um voto por risco: votar de novo troca o voto anterior.
    @PUT("riscos/{id}/confirmacoes")
    suspend fun votar(@Path("id") riscoId: Long, @Body request: ConfirmacaoRequest): ResumoConfirmacoes
}
