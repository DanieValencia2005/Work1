package com.ud.taller

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.ud.taller.firebase.FirebaseManager
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    var gameIdToJoin by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Autenticación anónima
    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().await()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Conecta 4 Online", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                val gameId = FirebaseManager.createGame()
                navController.navigate("game/$gameId")
            }
        }) {
            Text("Crear Partida")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = gameIdToJoin,
            onValueChange = { gameIdToJoin = it },
            label = { Text("ID de la partida") }
        )

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                val joined = FirebaseManager.joinGame(gameIdToJoin)
                if (joined) {
                    navController.navigate("game/$gameIdToJoin")
                } else {
                    Toast.makeText(context, "No se pudo unir. ID inválido o partida llena.", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text("Unirse a Partida")
        }
    }
}