package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityGameBinding

/**
 * 翻牌游戏 Activity：组合游戏模型、自定义视图和控制器，实现完整的 MVC 翻牌游戏�? */
class GameActivity : AppCompatActivity(),
    GameController.GameListener,
    CardGameView.OnCardClickListener {

    private lateinit var binding: ActivityGameBinding
    private val model = GameModel(PAIR_COUNT)
    private var controller: GameController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.cardGameView.bindModel(model)
        binding.cardGameView.onCardClickListener = this
        controller = GameController(model, this)
        binding.btnRestart.setOnClickListener { controller?.restart() }

        refreshStats()
    }

    override fun onCardClick(index: Int) {
        controller?.onCardClick(index)
    }

    override fun onGameStateChanged() {
        refreshStats()
    }

    override fun onGameWon(score: Int) {
        if (score > PlayerInfo.highScore) {
            PlayerInfo.highScore = score
        }
        Toast.makeText(this, R.string.toast_game_win, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        controller?.onDestroy()
        super.onDestroy()
    }

    private fun refreshStats() {
        binding.tvScore.text = getString(R.string.game_score, model.computeScore())
        binding.tvFlips.text = getString(R.string.game_flips, model.flips)
        binding.cardGameView.invalidate()
    }

    companion object {
        private const val PAIR_COUNT = 4
    }
}
