// A lot of PvP code and basis of the game came from :[https://www.youtube.com/watch?v=Zqnq-kSMMc0&ab_channel=EasyTuto], there was a plan to make the game online as well but ran into technical issues with firebase
// Assistance provided by Google Gemini (LLM).
// Key prompts used for development/modification:
// - Initial request: "Do you see errors in my code? If so could you fix them and explain them to me so I dont make these mistakes in the future?"
// - Providing existing files: "MainActivity.kt", "GameActivity.kt", "GameModel.kt"
// - Request for help in creating the vs Computer feature: "Can you take a look at my code and help me understand how to add a "play against computer" game mode "
// - Request for UI modification: can you show me how to correctly modify the ui file to change the colors?"
// - Request for general purpose comments: "Can you please generate simple comments for my code?"
package com.example.tictactoe
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tictactoe.databinding.ActivityMainBinding
// Enable View Binding for activity_main.xml.

// The main entry screen for configuring and starting the game
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // Holds references to UI elements from activity_main.xml.

    override fun onCreate(savedInstanceState: Bundle?) {
        // Called when the activity is first created.
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Inflate layout and initialize view binding.
        setContentView(binding.root)
        // Set the activity's UI content.

        binding.playOfflineBtn.setOnClickListener {
            // Set click listener for the "Start Game" button.
            createLocalGame()
            // Call function to set up game data and start the game.
        }

        // Listen for changes in the game mode selection (PvP/PvC).
        binding.gameModeRg.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.mode_pvc_radio) {
                // If PvC is selected, set Player 2 name to "Computer" and disable input.
                binding.player2NameEt.setText("Computer")
                binding.player2NameEt.isEnabled = false
            } else {
                // If PvP is selected, clear Player 2 name and enable input.
                binding.player2NameEt.setText("")
                binding.player2NameEt.isEnabled = true
            }
        }

        // Set PvP as the default mode when the activity starts.
        binding.modePvpRadio.isChecked = true
        binding.player2NameEt.isEnabled = true // Ensure Player 2 name input is enabled by default for PvP.
    }

    // Gathers user input for names, mode and greeting and creates/saves the initial GameModel.
    fun createLocalGame() {
        // Get player names, using defaults if inputs are empty.
        val player1Name = binding.player1NameEt.text.toString().trim().ifEmpty { "Player X" }
        var player2Name = binding.player2NameEt.text.toString().trim().ifEmpty { "Player O" } // Use var as it might be changed for PVC.
        val customGreeting = binding.customGreetingEt.text.toString().trim().ifEmpty { "Good Luck!" }

        // Determine the selected game mode.
        val selectedGameMode = if (binding.modePvpRadio.isChecked) {
            GameMode.PVP
        } else {
            player2Name = "Computer" // name is Computer if PvC is selected.
            GameMode.PVC
        }

        // Create and save the initial GameModel with user configurations.
        GameData.saveGameModel(
            GameModel(
                gameStatus = GameStatus.JOINED, // Set initial status ready
                player1Name = player1Name,
                player2Name = player2Name,
                gameMode = selectedGameMode,
                customGreeting = customGreeting
            )
        )
        startGame() // Navigate to the game activity.
    }

    // Starts the GameActivity to begin playing.
    fun startGame(){
        startActivity(Intent(this,GameActivity::class.java))
    }
}