package com.ud.taller

import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ud.taller.ui.theme.TallerTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background

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

    var board by remember {
        mutableStateOf(List(rows) { MutableList(cols) { 0 } })
    }

    var isPlayerTurn by remember { mutableStateOf(true) }
    var triggerMachineTurn by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf(0) }

    fun checkWin(player: Int): Boolean {
        // Horizontal
        for (r in 0 until rows) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r][c + it] == player }) return true
            }
        }

        // Vertical
        for (c in 0 until cols) {
            for (r in 0..rows - 4) {
                if ((0..3).all { board[r + it][c] == player }) return true
            }
        }

        // Diagonal down-right
        for (r in 0..rows - 4) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r + it][c + it] == player }) return true
            }
        }

        // Diagonal up-right
        for (r in 3 until rows) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r - it][c + it] == player }) return true
            }
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
        val availableColumns = (0 until cols).filter { col -> board[0][col] == 0 }
        if (availableColumns.isNotEmpty()) {
            val randomColumn = availableColumns.random()
            dropPiece(randomColumn, 2)
            isPlayerTurn = true
        }
    }

    LaunchedEffect(triggerMachineTurn) {
        if (triggerMachineTurn && winner == 0) {
            delay(500)
            machineMove()
            triggerMachineTurn = false
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (winner == 0) {
            Text("Turn: ${if (isPlayerTurn) "Player" else "Machine"}")
        } else {
            Text("Winner: ${if (winner == 1) "Player" else "Machine"}")
        }

        for (row in 0 until rows) {
            Row {
                for (col in 0 until cols) {
                    val cell = board[row][col]
                    val color = when (cell) {
                        1 -> Color.Green
                        2 -> Color.Red
                        else -> Color.LightGray
                    }

                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .size(45.dp)
                            .background(color)
                            .clickable(
                                enabled = cell == 0 && isPlayerTurn && winner == 0
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
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnect4() {
    TallerTheme {
        GameScreen()
    }
}
