package com.ud.taller.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ud.taller.firebase.FirebaseManager
import com.ud.taller.firebase.GameState
import kotlinx.coroutines.launch

@Composable
fun OnlineGameScreen(gameId: String, onBack: () -> Unit) {
    val uid = FirebaseManager.uid()
    var gs by remember { mutableStateOf<GameState?>(null) }
    var respuesta by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(gameId) {
        FirebaseManager.listenGame(gameId) { gs = it }
    }

    val state = gs ?: return Text("Cargando...")

    val isMyTurn = (state.currentPlayer == uid)
    val canMove = isMyTurn && state.turnAnswered
    val isWordPhase = isMyTurn && !state.turnAnswered

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Players: P1=${state.player1.take(6)} P2=${state.player2?.take(6) ?: "?"}")
        Text("Turno de: ${if (isMyTurn) "TÚ" else "RIVAL"}")

        Spacer(Modifier.height(8.dp))

        state.word?.let { w ->
            if (isWordPhase) {
                Text("Traduce: \"${w.original}\"")
                OutlinedTextField(value = respuesta, onValueChange = { respuesta = it })
                Button(onClick = {
                    val c = respuesta.trim().equals(w.translation, ignoreCase = true)
                    respuesta = ""
                    coroutineScope.launch {
                        FirebaseManager.answerWord(gameId, c)
                    }
                }) { Text("Enviar") }
            } else {
                Text("Palabra: \"${w.original}\"", color = Color.Gray)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tablero
        state.board.forEachIndexed { r, row ->
            Row(horizontalArrangement = Arrangement.Center) {
                row.forEachIndexed { c, cell ->
                    val col = c
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .background(
                                when (cell) {
                                    1 -> Color.Green
                                    2 -> Color.Red
                                    else -> Color.Gray
                                },
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable(enabled = canMove && cell == 0) {
                                coroutineScope.launch {
                                    FirebaseManager.drop(gameId, col)
                                }
                            }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (state.winner != 0) {
            Text(
                text = when {
                    state.winner == 1 && uid == state.player1 -> "¡Ganaste!"
                    state.winner == 2 && uid == state.player2 -> "¡Ganaste!"
                    else -> "Perdiste."
                }
            )
            Button(onClick = onBack) { Text("Salir") }
        }
    }
}