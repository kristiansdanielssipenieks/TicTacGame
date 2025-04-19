// A lot of inspiration came from :https://www.youtube.com/watch?v=Zqnq-kSMMc0&ab_channel=EasyTuto, there was a plan to make the game online as well but ran into technical issues with firebase
package com.example.tictactoe
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.tictactoe.databinding.ActivityGameBinding
import kotlinx.coroutines.*
// Use Kotlin coroutines for asynchronous operations, specifically for the AI delay.
import kotlin.random.Random
// Use the Random class for the AI's difficulty logic and initial player selection.


class GameActivity : AppCompatActivity(), View.OnClickListener {
// Manages the game screen, board, player interactions, and game logic.
// Implements OnClickListener to handle clicks on the game board buttons.

    lateinit var binding: ActivityGameBinding
    // Holds references to UI elements from activity_game.xml via View Binding.

    private var gameModel : GameModel? = null
    // Holds the current state of the game, observed from GameData.

    // Coroutine scope for managing asynchronous tasks like AI moves.
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    // Binds coroutines to the main thread dispatcher, necessary for UI updates.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Called when the activity is first created.
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        // Inflate the layout and initialize view binding.
        setContentView(binding.root)
        // Set the activity's UI content to the root view of the inflated layout.

        // Set click listeners for all 9 game board buttons, routing clicks to the onClick method.
        binding.btn0.setOnClickListener(this)
        binding.btn1.setOnClickListener(this)
        binding.btn2.setOnClickListener(this)
        binding.btn3.setOnClickListener(this)
        binding.btn4.setOnClickListener(this)
        binding.btn5.setOnClickListener(this)
        binding.btn6.setOnClickListener(this)
        binding.btn7.setOnClickListener(this)
        binding.btn8.setOnClickListener(this)

        binding.startGameBtn.setOnClickListener {
            // Set click listener for the start game button.
            startGame()
            // Call startGame function when the button is clicked.
        }

        GameData.gameModel.observe(this){
            // Observe changes to the shared GameModel LiveData. This block runs whenever the game state is updated.
            gameModel = it
            // Update the local gameModel variable with the latest state.
            setUI()
            // Refresh the UI to reflect the new game state.

            // Trigger AI move if it's PvC mode, game is in progress, and it's the AI's turn ("O").
            if (gameModel?.gameMode == GameMode.PVC && gameModel?.gameStatus == GameStatus.INPROGRESS && gameModel?.currentPlayer == "O") {
                makeAIMove()
            }
        }
    }

    override fun onDestroy() {
        // Called when the activity is about to be destroyed.
        super.onDestroy()
        coroutineScope.cancel() // Cancel any running coroutines managed by this scope to prevent leaks.
    }

    // Updates the UI elements (buttons, status text) based on the current gameModel state.
    fun setUI(){
        gameModel?.apply {
            // Safely access gameModel properties if not null.

            // Collect all game board buttons into a list for easy iteration.
            val buttons = listOf(
                binding.btn0, binding.btn1, binding.btn2,
                binding.btn3, binding.btn4, binding.btn5,
                binding.btn6, binding.btn7, binding.btn8
            )

            // Iterate through each button to set its text and color based on the board state.
            for (i in buttons.indices) {
                buttons[i].text = filledPos[i]
                // Set button text from the game model's filled positions.
                when (filledPos[i]) {
                    // Set text color based on whether the position is "X", "O", or empty.
                    "X" -> buttons[i].setTextColor(resources.getColor(R.color.player_x_color, theme))
                    "O" -> buttons[i].setTextColor(resources.getColor(R.color.player_o_color, theme))
                    else -> buttons[i].setTextColor(resources.getColor(R.color.black, theme)) // Default color for empty cells.
                }
            }

            // Control the visibility of the "Start Game" button based on game status.
            binding.startGameBtn.visibility = if (gameStatus == GameStatus.JOINED) View.VISIBLE else View.INVISIBLE

            // Update the game status text view with relevant information including player names and greeting.
            binding.gameStatusText.text =
                when (gameStatus) {
                    GameStatus.CREATED -> {
                        "Game ID :$gameId" // Display game ID (if applicable).
                    }
                    GameStatus.JOINED -> {
                        "$player1Name vs $player2Name\nPress Start Game" // Show player names and prompt to start.
                    }
                    GameStatus.INPROGRESS -> {
                        // Indicate whose turn it is using player names.
                        if (currentPlayer == "X") "$player1Name's turn" else "$player2Name's turn"
                    }
                    GameStatus.FINISHED -> {
                        if (winner.isNotEmpty()) {
                            // Announce the winner by name and display the custom greeting.
                            val winnerName = if (winner == "X") player1Name else player2Name
                            "$winnerName Wins!\n$customGreeting"
                        } else {
                            // Announce a draw and display the custom greeting.
                            "DRAW!\n$customGreeting"
                        }
                    }
                }
        }
    }

    // Resets the game board and status to start a new game.
    fun startGame(){
        gameModel?.apply {
            // Safely access gameModel.
            // Only allow starting if the game is in a ready state (JOINED or CREATED).
            if (gameStatus == GameStatus.JOINED || gameStatus == GameStatus.CREATED) {
                updateGameData(
                    copy( // Create a new GameModel instance with updated fields, preserving others.
                        gameStatus = GameStatus.INPROGRESS, // Set status to actively playing.
                        filledPos = mutableListOf("","","","","","","","",""), // Clear the board.
                        winner = "", // Clear the previous winner.
                        currentPlayer = (arrayOf("X","O"))[Random.nextInt(2)] // Randomly choose the starting player.
                    )
                )
            }
        }
    }

    // Saves the current GameModel state using the GameData singleton.
    fun updateGameData(model : GameModel){
        GameData.saveGameModel(model)
    }

    // Checks the current board state for a win or a draw.
    fun checkForWinner(){
        gameModel?.apply {
            // Safely access gameModel.
            // Define all possible winning combinations of indices.
            val winningPos = arrayOf(
                intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), // Rows
                intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), // Columns
                intArrayOf(0, 4, 8), intArrayOf(2, 4, 6) // Diagonals
            )

            // Iterate through each winning combination.
            for ( i in winningPos){
                // Check if the three positions in the current combination have the same, non-empty marker.
                if(
                    filledPos[i[0]] == filledPos[i[1]] &&
                    filledPos[i[1]] == filledPos[i[2]] &&
                    filledPos[i[0]].isNotEmpty()
                ){
                    gameStatus = GameStatus.FINISHED // Set status to finished if a winner is found.
                    winner = filledPos[i[0]] // Record the marker of the winning player.
                    updateGameData(this) // Save the updated state (win).
                    return // Exit the function immediately after finding a winner.
                }
            }

            // Check for a draw: if no winning line is found AND all positions are filled.
            if (filledPos.none(){ it.isEmpty() }){
                gameStatus = GameStatus.FINISHED // Set status to finished.
                winner = "" // Explicitly set winner to empty for a draw.
                updateGameData(this) // Save the updated state (draw).
            }
        }
    }

    // Handles click events for the game board buttons.
    override fun onClick(v: View?) {
        gameModel?.apply {
            // Safely access gameModel.

            // Ignore clicks if the game is not in progress or if it's the AI's turn in PvC mode.
            if (gameStatus!= GameStatus.INPROGRESS || (gameMode == GameMode.PVC && currentPlayer == "O")) {
                Toast.makeText(applicationContext,"Game not started or waiting for Computer",Toast.LENGTH_SHORT).show()
                return // Exit if click is invalid.
            }

            // Get the index of the clicked button from its tag.
            val clickedPos =(v?.tag as String).toInt()

            // If the clicked position is empty, proceed with placing the marker.
            if(filledPos[clickedPos].isEmpty()){
                filledPos[clickedPos] = currentPlayer // Place the current player's marker on the board.
                currentPlayer = if(currentPlayer=="X") "O" else "X" // Switch to the other player's turn.
                checkForWinner() // Check if the move resulted in a win or draw.
                updateGameData(this) // Save the updated game state.
            }
        }
    }

    // --- PvC Mode (AI) Logic ---

    // Initiates the AI's move selection and execution.
    private fun makeAIMove() {
        gameModel?.apply {
            // Safely access gameModel.
            // Ensure it's PvC mode, game is in progress, and it's the AI's turn (O).
            if (gameMode == GameMode.PVC && gameStatus == GameStatus.INPROGRESS && currentPlayer == "O") {
                coroutineScope.launch {
                    // Launch a coroutine to perform the AI move asynchronously, preventing UI freezes.
                    delay(1000) // Add a small delay to make the AI's turn visually understandable.

                    val aiMove = getAIMove(filledPos, difficulty = 0.65) // Get the AI's chosen move based on difficulty (65%).

                    // Place the AI's marker ("O") if the chosen position is empty.
                    if (filledPos[aiMove].isEmpty()) {
                        filledPos[aiMove] = "O" // AI is always player O.
                        currentPlayer = "X" // Switch turn back to the human player (X).
                        checkForWinner() // Check for win/draw after AI move.
                        updateGameData(this@apply) // Save the updated state.
                    } else {
                        // Fallback: if the chosen AI move is somehow not empty (shouldn't happen with correct logic), find a random empty spot.
                        val emptyPositions = filledPos.indices.filter { filledPos[it].isEmpty() }
                        if (emptyPositions.isNotEmpty()) {
                            val randomMove = emptyPositions.random()
                            filledPos[randomMove] = "O"
                            currentPlayer = "X"
                            checkForWinner()
                            updateGameData(this@apply)
                        }
                    }
                }
            }
        }
    }

    // Determines the AI's next move based on the current board state and difficulty.
    private fun getAIMove(board: List<String>, difficulty: Double): Int {
        val emptyPositions = board.indices.filter { board[it].isEmpty() }
        // Get a list of all currently empty positions on the board.

        // Decide whether to make a smart move or a random move based on the difficulty probability.
        return if (Random.nextDouble() < difficulty) {
            // Smart move logic: Try to win, then try to block, then find a strategic spot.
            findWinningOrBlockingMove(board, "O") // Check if AI can win.
                ?: findWinningOrBlockingMove(board, "X") // If not, check if AI needs to block the opponent.
                ?: findBestStrategicMove(board) // If no immediate win or block, find a strategic position (center, corners).
                ?: emptyPositions.randomOrNull() ?: 0 // Fallback to a random empty spot, or index 0 if none are left.
        } else {
            // Random move logic (for lower difficulty or when smart move fails).
            emptyPositions.randomOrNull() ?: 0 // Choose a random empty spot, or index 0 if none are left.
        }
    }

    // Helper function to find a move that immediately wins the game for 'player' or blocks the opponent.
    private fun findWinningOrBlockingMove(board: List<String>, player: String): Int? {
        // Define all possible winning combinations.
        val winningPos = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), // Rows
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), // Columns
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6) // Diagonals
        )

        // Iterate through each winning line.
        for (line in winningPos) {
            val (p1, p2, p3) = line.map { board[it] }

            // Check if two positions are occupied by the 'player' and the third is empty.
            if (p1 == player && p2 == player && p3.isEmpty()) return line[2] // Return the index of the empty spot if it completes the line.
            if (p1 == player && p3 == player && p2.isEmpty()) return line[1]
            if (p2 == player && p3 == player && p1.isEmpty()) return line[0]
        }
        return null // Return null if no winning or blocking move is found for the given 'player'.
    }

    // Helper function to find a strategically good move
    private fun findBestStrategicMove(board: List<String>): Int? {
        // Prioritize the center position
        if (board[4].isEmpty()) return 4

        // If center is taken, prioritize corners
        val corners = listOf(0, 2, 6, 8)
        corners.shuffled().forEach { corner ->
            if (board[corner].isEmpty()) return corner // Return the first empty corner found
        }

        // If center and corners are taken, prioritize edges. Shuffle to add "randomness"
        val edges = listOf(1, 3, 5, 7)
        edges.shuffled().forEach { edge ->
            if (board[edge].isEmpty()) return edge // Return the first empty edge found
        }

        return null // Return null if no strategic positions are available
    }

}