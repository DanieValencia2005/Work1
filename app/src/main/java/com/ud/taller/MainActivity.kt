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

/**
 * MainActivity sets up the Connect 4 game screen.
 */
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

/**
 * Composable that displays the Connect 4 game board and controls.
 */
@Composable
fun GameScreen(modifier: Modifier = Modifier) {
    val rows = 6
    val cols = 7
    val context = LocalContext.current

    // Game state
    var board by remember { mutableStateOf(List(rows) { MutableList(cols) { 0 } }) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    var triggerMachineTurn by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf(0) }
    var showDrawMessage by remember { mutableStateOf(false) }

    /**
     * Checks if the specified player has won the game.
     * @param player the player number (1 for human, 2 for machine)
     * @return true if the player has a connecting line of four
     */
    fun checkWin(player: Int): Boolean {
        // Horizontal check
        for (r in 0 until rows) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r][c + it] == player }) return true
            }
        }
        // Vertical check
        for (c in 0 until cols) {
            for (r in 0..rows - 4) {
                if ((0..3).all { board[r + it][c] == player }) return true
            }
        }
        // Diagonal (down-right) check
        for (r in 0..rows - 4) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r + it][c + it] == player }) return true
            }
        }
        // Diagonal (up-right) check
        for (r in 3 until rows) {
            for (c in 0..cols - 4) {
                if ((0..3).all { board[r - it][c + it] == player }) return true
            }
        }
        return false
    }

    /**
     * Drops a piece into the given column for a player.
     * @param column the target column index
     * @param player the player number (1 or 2)
     * @return true if the piece was placed, false if column is full
     */
    fun dropPiece(column: Int, player: Int): Boolean {
        for (row in (rows - 1) downTo 0) {
            if (board[row][column] == 0) {
                board = board.toMutableList().apply {
                    this[row] = this[row].toMutableList().apply { this[column] = player }
                }
                if (checkWin(player)) winner = player
                return true
            }
        }
        return false
    }

    /**
     * Makes a random move for the machine player.
     */
    fun machineMove() {
        val availableColumns = (0 until cols).filter { board[0][it] == 0 }
        if (availableColumns.isNotEmpty()) {
            val randomColumn = availableColumns.random()
            dropPiece(randomColumn, 2)
            isPlayerTurn = true
        }
    }

    /**
     * Checks if the game ended in a draw (no empty cells.
     * @return true if board is full and no winner
     */
    fun isDraw(): Boolean = (winner == 0 && board.all { row -> row.none { it == 0 } })

    /**
     * Resets all game state to start a new match.
     */
    fun resetGame() {
        board = List(rows) { MutableList(cols) { 0 } }
        isPlayerTurn = true
        triggerMachineTurn = false
        winner = 0
        showDrawMessage = false
    }

    // Handle machine turn after a short delay
    LaunchedEffect(triggerMachineTurn) {
        if (triggerMachineTurn && winner == 0) {
            delay(600)
            machineMove()
            triggerMachineTurn = false
        }
    }

    // Show draw toast if game is a draw
    LaunchedEffect(board) {
        if (isDraw()) {
            showDrawMessage = true
            Toast.makeText(context, "Draw!", Toast.LENGTH_LONG).show()
        }
    }

    // UI layout
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show game status
        Text(
            text = when {
                winner == 1 -> "Player Wins!"
                winner == 2 -> "Machine Wins!"
                showDrawMessage -> "Draw!"
                else -> "Turn: ${if (isPlayerTurn) "Player" else "Machine"}"
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Rendering the board grid
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .widthIn(max = (40.dp * cols) + (8.dp * cols))
                    .wrapContentWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center
            ) {
                for (col in 0 until cols) {
                    val cell = board[row][col]
                    val color = when (cell) {
                        1 -> Color(0xFF4CAF50) // green for player
                        2 -> Color(0xFFE53935) // red for machine
                        else -> Color(0xFFBDBDBD) // gray for empty
                    }

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(40.dp)
                            .background(color, shape = MaterialTheme.shapes.medium)
                            .clickable(
                                enabled = (cell == 0 && isPlayerTurn && winner == 0 && !showDrawMessage)
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

        // Reset button
        Button(onClick = { resetGame() }) {
            Text("Reset Game")
        }
    }
}

/**
 * Preview composable for Android Studio preview.
 */
@Preview(showBackground = true)
@Composable
fun PreviewConnect4() {
    TallerTheme {
        GameScreen()
    }
}
