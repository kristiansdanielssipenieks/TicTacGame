// A lot of inspiration came from :https://www.youtube.com/watch?v=Zqnq-kSMMc0&ab_channel=EasyTuto, there was a plan to make the game online as well but ran into technical issues with firebase
package com.example.tictactoe
// Organize code within the project's package.

import kotlin.random.Random
// Allow random selection (e.g., for starting player).

// Represents the complete state of a Tic-Tac-Toe game.
data class GameModel (
    var gameId: String = "—1", // Unique identifier for the game (less relevant for offline).
    var filledPos : MutableList<String> = mutableListOf("","","","","","","","",""),
    // Stores the markers ("X", "O", or "") for each of the 9 board positions.
    var winner : String = "", // Stores the marker of the winning player ("X" or "O") or is empty for a draw/ongoing game.
    var gameStatus : GameStatus = GameStatus.CREATED, // Tracks the current phase of the game (CREATED, JOINED, INPROGRESS, FINISHED).
    var currentPlayer : String = (arrayOf("X","O"))[Random.nextInt(2)], // Indicates whose turn it is ("X" or "O") randomly

    // Fields added for player names, game mode, and custom greeting.
    var player1Name: String = "Player X", // Custom name for Player X.
    var player2Name: String = "Player O", // Custom name for Player O.
    var gameMode: GameMode = GameMode.PVP, // Determines if it's Player vs Player or Player vs Computer.
    var customGreeting: String = "Good Luck!" // A custom message to display at the end of the game.
)

// Defines the possible states a game can be in.
enum class GameStatus{
    CREATED, // Initial state (potentially for online game creation which i wanted to implement but had technical issues with using firebase).
    JOINED, // Players/settings are ready, waiting to start.
    INPROGRESS, // Game is actively being played.
    FINISHED // Game has ended (win or draw).
}

// Defines the possible game modes.
enum class GameMode {
    PVP,
    PVC
}