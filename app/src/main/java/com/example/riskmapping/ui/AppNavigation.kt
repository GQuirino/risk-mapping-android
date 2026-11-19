package com.example.riskmapping.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.riskmapping.RiskMappingApp
import com.example.riskmapping.ui.cadastro.TelaCadastro
import com.example.riskmapping.ui.login.TelaLogin
import com.example.riskmapping.ui.principal.TelaPrincipal
import com.example.riskmapping.ui.riscos.TelaDetalheRisco
import com.example.riskmapping.ui.riscos.TelaFormularioRisco

/** Os nomes das telas do app. */
private object Rotas {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val PRINCIPAL = "principal"
    const val DETALHE_RISCO = "risco/{id}" // {id} é o número do risco
    const val NOVO_RISCO = "novo-risco"
    const val EDITAR_RISCO = "editar-risco/{id}"
}

/**
 * O mapa do app: quais telas existem e como se vai de uma para outra.
 *
 *   login ──(entrou)──▶ principal ──(Sair)──▶ login
 *     └──(Criar conta)──▶ cadastro ──(criou a conta)──▶ principal
 *                          ├──(toca num risco)──▶ detalhe do risco
 *                          ├──(botão +)──▶ formulário (novo risco)
 *                          └──(detalhe: Editar)──▶ formulário (editar risco)
 *
 * Quem já tem token salvo abre direto na tela principal.
 */
@Composable
fun AppNavigation() {
    val app = LocalContext.current.applicationContext as RiskMappingApp
    val navController = rememberNavController()
    val telaInicial = if (app.sessao.estaLogado()) Rotas.PRINCIPAL else Rotas.LOGIN

    NavHost(navController = navController, startDestination = telaInicial) {

        composable(Rotas.LOGIN) {
            TelaLogin(
                aoLogar = { navController.trocarPara(Rotas.PRINCIPAL, apagarDaPilha = Rotas.LOGIN) },
                aoCriarConta = { navController.navigate(Rotas.CADASTRO) },
            )
        }

        composable(Rotas.CADASTRO) {
            TelaCadastro(
                aoVoltar = { navController.popBackStack() },
                // Cadastrou e já está logado: vai para a principal e limpa login e cadastro do histórico
                aoCadastrar = { navController.trocarPara(Rotas.PRINCIPAL, apagarDaPilha = Rotas.LOGIN) },
            )
        }

        composable(Rotas.PRINCIPAL) {
            TelaPrincipal(
                aoAbrirRisco = { id -> navController.navigate("risco/$id") },
                aoCriarRisco = { navController.navigate(Rotas.NOVO_RISCO) },
                aoSair = {
                    app.sessao.sair() // esquece o token
                    navController.trocarPara(Rotas.LOGIN, apagarDaPilha = Rotas.PRINCIPAL)
                },
            )
        }

        composable(Rotas.NOVO_RISCO) {
            TelaFormularioRisco(
                aoVoltar = { navController.popBackStack() },
                aoSalvar = { navController.popBackStack() }, // a lista de riscos se atualiza sozinha
            )
        }

        composable(
            route = Rotas.DETALHE_RISCO,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entrada ->
            val id = entrada.arguments!!.getLong("id")
            TelaDetalheRisco(
                aoVoltar = { navController.popBackStack() },
                aoEditar = { navController.navigate("editar-risco/$id") },
            )
        }

        composable(
            route = Rotas.EDITAR_RISCO,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) {
            TelaFormularioRisco(
                aoVoltar = { navController.popBackStack() },
                aoSalvar = { navController.popBackStack() }, // volta ao detalhe, que se atualiza sozinho
            )
        }
    }
}

/** Vai para [destino] e tira [apagarDaPilha] do histórico, para o botão "voltar" não retornar a ela. */
private fun NavController.trocarPara(destino: String, apagarDaPilha: String) {
    navigate(destino) {
        popUpTo(apagarDaPilha) { inclusive = true }
    }
}
