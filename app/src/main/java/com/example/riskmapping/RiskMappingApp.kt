package com.example.riskmapping

import android.app.Application
import com.example.riskmapping.data.Catalogo
import com.example.riskmapping.data.Sessao
import com.example.riskmapping.data.api.RiskApi
import com.example.riskmapping.data.api.criarApi
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Roda uma vez quando o app abre. Cria a sessão e o cliente da API, que as telas
 * (ViewModels) reutilizam: `getApplication<RiskMappingApp>().api`.
 */
class RiskMappingApp : Application() {

    lateinit var sessao: Sessao
        private set

    lateinit var api: RiskApi
        private set

    lateinit var catalogo: Catalogo
        private set

    // Sobe de número quando um risco é criado, alterado ou apagado. A lista de riscos observa
    // este número e se recarrega sozinha.
    val riscosMudaram = MutableStateFlow(0)

    override fun onCreate() {
        super.onCreate()
        sessao = Sessao(this)
        api = criarApi(sessao)
        catalogo = Catalogo(api)
    }
}
