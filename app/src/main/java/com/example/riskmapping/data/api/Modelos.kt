package com.example.riskmapping.data.api

// O que o app ENVIA e RECEBE da API. Os nomes dos campos são os mesmos do JSON.

/** Corpo do POST /auth/login */
data class LoginRequest(val email: String, val senha: String)

/** Resposta do login: o token JWT que prova quem somos nas próximas chamadas. */
data class TokenResponse(val token: String)

/** Resposta de GET /usuarios/me (a API nunca devolve a senha). */
data class Usuario(val id: Long, val nome: String, val email: String, val perfil: String)

/** Corpo do POST /auth/registrar */
data class RegistrarRequest(val nome: String, val email: String, val senha: String, val confirmarSenha: String)

/** Tipo de risco (alagamento, buraco...). */
data class Categoria(val id: Long, val nome: String, val descricao: String?)

data class Bairro(val id: Long, val nome: String, val cidade: String, val estado: String)

/** Um risco no mapa. `categoriaId` e `bairroId` apontam para as listas de categorias e bairros. */
data class Risco(
    val id: Long,
    val titulo: String,
    val descricao: String?,
    val latitude: Double,
    val longitude: Double,
    val gravidade: Int, // de 1 (leve) a 5 (gravíssimo)
    val status: String, // ABERTO, CONFIRMADO ou RESOLVIDO
    val categoriaId: Long,
    val bairroId: Long,
    val criadoPorId: Long, // id do usuário dono do risco
    val criadoEm: String?,
)

/** Corpo do POST /riscos */
data class RiscoRequest(
    val titulo: String,
    val descricao: String?,
    val latitude: Double,
    val longitude: Double,
    val gravidade: Int,
    val categoriaId: Long,
    val bairroId: Long,
)

/** Corpo do PATCH /riscos/{id}/status */
data class StatusRequest(val status: String)

data class Comentario(val id: Long, val riscoId: Long, val usuarioId: Long, val texto: String, val criadoEm: String?)

data class ComentarioRequest(val texto: String)

/** Corpo do PUT /riscos/{id}/confirmacoes: "CONFIRMA" (ainda existe) ou "NEGA" (não existe mais). */
data class ConfirmacaoRequest(val tipo: String)

/** Quantas pessoas confirmam e quantas negam o risco. */
data class ResumoConfirmacoes(val confirmacoes: Int, val negacoes: Int)

