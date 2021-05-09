package hsk.practice.myvoca.ui.activity

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.databinding.ActivityEditVocaBinding
import hsk.practice.myvoca.framework.RoomVocabulary
import hsk.practice.myvoca.ui.NewVocaViewModel
import java.util.*

/**
 * Activity where users can modify the word.
 * inputEng must not be null, with the same reason in the AddVocaActivity.
 *
 * If the field 'eng' is edited, activity erases the previous word and add the new word.
 * Otherwise, activity just edits the database. See editVocabulary().
 */
@AndroidEntryPoint
class EditVocaActivity : AppCompatActivity() {
    private lateinit var vocabulary: RoomVocabulary
    private var exitCode = 0

    private lateinit var binding: ActivityEditVocaBinding

    private val newVocaViewModel: NewVocaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_voca)
        binding.lifecycleOwner = this

        vocabulary = intent.getSerializableExtra(Constants.EDIT_VOCA) as RoomVocabulary
        binding.target = vocabulary

        val toolbar = findViewById<Toolbar?>(R.id.toolbar_activity_edit_voca)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE

        binding.editButtonOk.setOnClickListener {
            if (!updateVocabulary()) {
                Toast.makeText(applicationContext, "단어를 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            exitCode = Constants.EDIT_NEW_VOCA_OK
            finish()
        }

        binding.editButtonCancel.setOnClickListener {
            exitCode = Constants.EDIT_NEW_VOCA_CANCEL
            finish()
        }
    }

    // returns true if vocabulary is edited (or added) successfully, false otherwise
    private fun updateVocabulary(): Boolean {
        val eng = binding.editInputEng.text.toString()
        val kor = binding.editInputKor.text.toString()
        val memo = binding.editInputMemo.text.toString()
        val time = Calendar.getInstance().timeInMillis / 1000
        if (eng == "") {
            return false
        }
        val newVocabulary = RoomVocabulary(eng, kor, vocabulary.addedTime, time, memo)
        if (vocabulary.eng == newVocabulary.eng) {
            newVocaViewModel.updateVocabulary(newVocabulary)
        } else {
            newVocaViewModel.deleteVocabulary(vocabulary)
            newVocaViewModel.insertVocabulary(newVocabulary)
        }
        Toast.makeText(application, "수정 완료!", Toast.LENGTH_LONG).show()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun finish() {
        setResult(exitCode)
        super.finish()
    }
}