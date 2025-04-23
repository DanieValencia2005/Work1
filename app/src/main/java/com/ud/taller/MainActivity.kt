package com.ud.taller

import kotlinx.coroutines.delay
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ud.taller.ui.theme.TallerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TallerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val rows = 6
    val cols = 7
    val context = LocalContext.current

    var board by remember { mutableStateOf(List(rows) { MutableList(cols) { 0 } }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var triggerMachineTurn by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf(0) }
    var showDrawMessage by remember { mutableStateOf(false) }

    fun checkWin(player: Int): Boolean {
        for (r in 0 until rows) {
            for (c in 0..cols - 4) if ((0..3).all { board[r][c + it] == player }) return true
        }
        for (c in 0 until cols) {
            for (r in 0..rows - 4) if ((0..3).all { board[r + it][c] == player }) return true
        }
        for (r in 0..rows - 4) {
            for (c in 0..cols - 4) if ((0..3).all { board[r + it][c + it] == player }) return true
        }
        for (r in 3 until rows) {
            for (c in 0..cols - 4) if ((0..3).all { board[r - it][c + it] == player }) return true
        }
        return false
    }

    fun dropPiece(column: Int, player: Int): Boolean {
        for (row in (rows - 1) downTo 0) {
            if (board[row][column] == 0) {
                board = board.toMutableList().apply {
                    this[row] = this[row].toMutableList().apply { this[column] = player }
                }
                if (checkWin(player)) {
                    winner = player
                }
                return true
            }
        }
        return false
    }

    fun machineMove() {
        val availableColumns = (0 until cols).filter { board[0][it] == 0 }
        if (availableColumns.isNotEmpty()) {
            val randomColumn = availableColumns.random()
            dropPiece(randomColumn, 2)
            isPlayerTurn = true
        }
    }

    fun isDraw(): Boolean {
        return winner == 0 && board.all { row -> row.all { it != 0 } }
    }

    fun resetGame() {
        board = List(rows) { MutableList(cols) { 0 } }
        isPlayerTurn = true
        triggerMachineTurn = false
        winner = 0
        showDrawMessage = false
    }

    LaunchedEffect(triggerMachineTurn) {
        if (triggerMachineTurn && winner == 0) {
            delay(600)
            machineMove()
            triggerMachineTurn = false
        }
    }

    LaunchedEffect(board) {
        if (isDraw()) {
            showDrawMessage = true
            Toast.makeText(context, "¡Empate!", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                winner == 1 -> "¡Ganó el Jugador!"
                winner == 2 -> "¡Ganó la Máquina!"
                showDrawMessage -> "¡Empate!"
                else -> "Turno: ${if (isPlayerTurn) "Jugador" else "Máquina"}"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .widthIn(max = (50.dp * cols) + (8.dp * cols)) // limitar el ancho
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                for (col in 0 until cols) {
                    val cell = board[row][col]
                    val color = when (cell) {
                        1 -> Color(0xFF4CAF50) // verde jugador
                        2 -> Color(0xFFE53935) // rojo máquina
                        else -> Color(0xFFBDBDBD) // gris vacío
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(50.dp)
                            .background(color, shape = MaterialTheme.shapes.medium)
                            .clickable(
                                enabled = cell == 0 && isPlayerTurn && winner == 0 && !showDrawMessage
                            ) {
                                if (dropPiece(col, 1)) {
                                    isPlayerTurn = false
                                    triggerMachineTurn = true
                                }
                            }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { resetGame() }) {
            Text("Reiniciar Partida")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnect4() {
    TallerTheme {
        GameScreen()
    }
}