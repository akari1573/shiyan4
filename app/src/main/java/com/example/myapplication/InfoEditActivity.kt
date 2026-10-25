package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityInfoEditBinding

/**
 * 信息修改 Activity：修改玩家姓名、性别和电话，修改结果保存在内存中（无需写入文件）。
 */
class InfoEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInfoEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.etName.setText(PlayerInfo.name)
        binding.etPhone.setText(PlayerInfo.phone)
        if (PlayerInfo.gender == getString(R.string.gender_female)) {
            binding.rbFemale.isChecked = true
        } else {
            binding.rbMale.isChecked = true
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun save() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.toast_name_empty, Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.length != 11 || !phone.all { it.isDigit() }) {
            Toast.makeText(this, R.string.toast_phone_invalid, Toast.LENGTH_SHORT).show()
            return
        }

        PlayerInfo.name = name
        PlayerInfo.phone = phone
        PlayerInfo.gender = if (binding.rbFemale.isChecked) {
            getString(R.string.gender_female)
        } else {
            getString(R.string.gender_male)
        }
        Toast.makeText(this, R.string.toast_save_success, Toast.LENGTH_SHORT).show()
        finish()
    }
}
