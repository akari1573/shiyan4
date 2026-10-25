package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMainBinding

/**
 * 主 Activity：显示当前玩家的基本信息，并提供进入信息修改和翻牌游戏的入口。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (PlayerInfo.name.isEmpty()) {
            PlayerInfo.resetDefaults(
                getString(R.string.default_name),
                getString(R.string.gender_male),
                getString(R.string.default_phone)
            )
        }

        binding.btnEditInfo.setOnClickListener {
            startActivity(Intent(this, InfoEditActivity::class.java))
        }
        binding.btnStartGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPlayerInfo()
    }

    private fun refreshPlayerInfo() {
        binding.tvName.text = PlayerInfo.name
        binding.tvGender.text = PlayerInfo.gender
        binding.tvPhone.text = PlayerInfo.phone
        binding.tvHighScore.text = getString(R.string.high_score_value, PlayerInfo.highScore)
    }
}
