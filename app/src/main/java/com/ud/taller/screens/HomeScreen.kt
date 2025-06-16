package com.ud.taller.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.ud.taller.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    var joinId by remember { mutableStateOf("") }

    // Autenticación anónima
    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            FirebaseAuth.getInstance().signInAnonymously().await()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Bienvenido", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val gameId = FirebaseManager.createGame()
                    navController.navigate("game/$gameId")
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al crear la partida: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text("Crear partida")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = joinId,
            onValueChange = { joinId = it },
            label = { Text("Código de partida") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val joined = FirebaseManager.joinGame(joinId)
                    if (joined) {
                        navController.navigate("game/$joinId")
                    } else {
                        Toast.makeText(context, "ID inválido o partida llena.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al unirse: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }) {
            Text("Unirse a partida")
        }
    }
}

