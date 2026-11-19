package com.example.riskmapping.ui.principal

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.riskmapping.ui.riscos.TelaRiscos

/** As abas da barra inferior. Para criar uma aba nova: acrescente aqui e em um `composable` abaixo. */
private enum class Aba(val rota: String, val titulo: String, val icone: ImageVector) {
    RISCOS("riscos", "Riscos", Icons.Default.Warning),
    PERFIL("perfil", "Perfil", Icons.Default.Person),
}

/** Tela de quem já entrou: a barra de abas embaixo e o conteúdo da aba escolhida em cima. */
@Composable
fun TelaPrincipal(
    aoAbrirRisco: (Long) -> Unit,
    aoCriarRisco: () -> Unit,
    aoSair: () -> Unit,
) {
    val navController = rememberNavController() // navegação só entre as abas
    val rotaAtual = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Aba.entries.forEach { aba ->
                    NavigationBarItem(
                        selected = rotaAtual == aba.rota,
                        onClick = {
                            navController.navigate(aba.rota) {
                                popUpTo(Aba.RISCOS.rota) // trocar de aba não empilha telas
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                        label = { Text(aba.titulo) },
                    )
                }
            }
        },
    ) { espacoDaBarra ->
        NavHost(
            navController = navController,
            startDestination = Aba.RISCOS.rota,
            modifier = Modifier.padding(espacoDaBarra),
        ) {
            composable(Aba.RISCOS.rota) { TelaRiscos(aoAbrirRisco = aoAbrirRisco, aoCriarRisco = aoCriarRisco) }
            composable(Aba.PERFIL.rota) { TelaPerfil(aoSair = aoSair) }
        }
    }
}
