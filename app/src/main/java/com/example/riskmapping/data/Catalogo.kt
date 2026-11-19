package com.example.riskmapping.data

import com.example.riskmapping.data.api.Bairro
import com.example.riskmapping.data.api.Categoria
import com.example.riskmapping.data.api.RiskApi

/**
 * As listas de categorias e de bairros, que quase nunca mudam. São buscadas uma vez e guardadas
 * na memória: os riscos só trazem o número (categoriaId, bairroId) e as telas usam isto para
 * mostrar o nome.
 */
class Catalogo(private val api: RiskApi) {

    var categorias: List<Categoria> = emptyList()
        private set

    var bairros: List<Bairro> = emptyList()
        private set

    /** Busca as listas se ainda não tiver feito. Pode ser chamada à vontade. */
    suspend fun carregar() {
        if (categorias.isEmpty()) categorias = api.categorias()
        if (bairros.isEmpty()) bairros = api.bairros()
    }

    fun nomeDaCategoria(id: Long): String = categorias.firstOrNull { it.id == id }?.nome ?: "Categoria $id"

    fun nomeDoBairro(id: Long): String = bairros.firstOrNull { it.id == id }?.nome ?: "Bairro $id"
}
