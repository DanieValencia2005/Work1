package com.ud.taller.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Palabra(val original: String = "", val translation: String = "")
data class GameState(
    val board: List<List<Int>> = List(6) { List(7) { 0 } },
    val currentPlayer: String = "",
    val player1: String = "",
    val player2: String? = null,
    val winner: Int = 0,
    val word: Palabra? = null,
    val turnAnswered: Boolean = false
)

object FirebaseManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val vocabCollection = db.collection("vocabulario")
    private val gamesCollection = db.collection("games")

    fun uid() = auth.currentUser?.uid ?: ""

    suspend fun createGame(): String = withContext(Dispatchers.IO) {
        val uid = uid()
        val newGame = GameState(player1 = uid, currentPlayer = uid)
        val ref = gamesCollection.add(newGame).await()
        ref.id
    }

    suspend fun joinGame(gameId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = uid()
        val snap = gamesCollection.document(gameId).get().await()
        if (snap.exists() && snap.getString("player2") == null && snap.getString("player1") != uid) {
            gamesCollection.document(gameId).update("player2", uid).await()
            true
        } else false
    }

    fun listenGame(gameId: String, onChange: (GameState) -> Unit) {
        gamesCollection.document(gameId)
            .addSnapshotListener { snap, _ ->
                snap?.toObject(GameState::class.java)?.let(onChange)
            }
    }

    suspend fun obtenerPalabraAleatoria(): Palabra? = withContext(Dispatchers.IO) {
        val snap = vocabCollection.get().await()
        snap.documents.mapNotNull { it.toObject(Palabra::class.java) }.randomOrNull()
    }

    suspend fun askNewWord(gameId: String) = withContext(Dispatchers.IO) {
        val palabra = obtenerPalabraAleatoria()
        gamesCollection.document(gameId).update(
            mapOf("word" to palabra, "turnAnswered" to false)
        ).await()
    }

    suspend fun answerWord(gameId: String, correct: Boolean) = withContext(Dispatchers.IO) {
        gamesCollection.document(gameId).update("turnAnswered", true).await()
        if (!correct) switchTurn(gameId)
    }

    private suspend fun switchTurn(gameId: String) = withContext(Dispatchers.IO) {
        val snap = gamesCollection.document(gameId).get().await()
        val gs = snap.toObject(GameState::class.java) ?: return@withContext
        val next = if (gs.currentPlayer == gs.player1) gs.player2!! else gs.player1
        gamesCollection.document(gameId).update("currentPlayer", next).await()
        askNewWord(gameId)
    }

    suspend fun drop(gameId: String, column: Int) = withContext(Dispatchers.IO) {
        val snap = gamesCollection.document(gameId).get().await()
        val gs = snap.toObject(GameState::class.java) ?: return@withContext
        val board = gs.board.map { it.toMutableList() }.toMutableList()
        val me = uid()
        val isP1 = me == gs.player1
        for (r in 5 downTo 0) {
            if (board[r][column] == 0) {
                board[r][column] = if (isP1) 1 else 2
                break
            }
        }
        val winner = if (checkWin(board, if (isP1) 1 else 2)) (if (isP1) 1 else 2) else 0
        gamesCollection.document(gameId).update(
            mapOf(
                "board" to board,
                "winner" to winner
            )
        ).await()
        if (winner == 0) switchTurn(gameId)
    }

    private fun checkWin(b: List<List<Int>>, p: Int): Boolean {
        repeat(6) { r ->
            repeat(4) { c ->
                if ((0..3).all { b[r][c + it] == p }) return true
            }
        }
        repeat(7) { c ->
            repeat(3) { r ->
                if ((0..3).all { b[r + it][c] == p }) return true
            }
        }
        repeat(3) { r ->
            repeat(4) { c ->
                if ((0..3).all { b[r + it][c + it] == p }) return true
            }
        }
        repeat(3) { r ->
            repeat(4) { c ->
                if ((0..3).all { b[5 - r - it][c + it] == p }) return true
            }
        }
        return false
    }
}
