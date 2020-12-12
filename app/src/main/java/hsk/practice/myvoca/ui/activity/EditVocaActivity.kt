package hsk.practice.myvoca.ui.activity

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import database.Vocabulary
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import java.util.*

/**
 * Activity where users can modify the word.
 * inputEng must not be null, with the same reason in the AddVocaActivity.
 *
 * If the field 'eng' is edited, activity erases the previous word and add the new word.
 * Otherwise, activity just edits the database. See editVocabulary().
 */
class EditVocaActivity : AppCompatActivity() {
    private var position = 0
    private var vocabulary: Vocabulary? = null
    private var exitCode = 0
    private var inputEng: TextInputEditText? = null
    private var inputKor: TextInputEditText? = null
    private var inputMemo: TextInputEditText? = null
    private var buttonOK: Button? = null
    private var buttonCancel: Button? = null
    var vocaViewModel: VocaViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_voca)
        val intent = intent
        position = intent.getIntExtra(Constants.POSITION, 0)
        vocabulary = intent.getSerializableExtra(Constants.EDIT_VOCA) as Vocabulary
        vocaViewModel = ViewModelProvider(this).get(VocaViewModel::class.java)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar_activity_edit_voca)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE)
        inputEng = findViewById(R.id.edit_input_eng)
        inputKor = findViewById(R.id.edit_input_kor)
        inputMemo = findViewById(R.id.edit_input_memo)
        inputEng.setText(vocabulary.eng)
        inputKor.setText(vocabulary.kor)
        inputMemo.setText(vocabulary.memo)
        buttonOK = findViewById(R.id.edit_button_ok)
        buttonCancel = findViewById(R.id.edit_button_cancel)
        buttonOK.setOnClickListener(View.OnClickListener {
            if (!editVocabulary()) {
                Toast.makeText(applicationContext, "단어를 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            exitCode = Constants.EDIT_NEW_VOCA_OK
            finish()
        })
        buttonCancel.setOnClickListener(View.OnClickListener {
            exitCode = Constants.EDIT_NEW_VOCA_CANCEL
            finish()
        })
    }

    // returns true if vocabulary is edited (or added) successfully, false otherwise
    private fun editVocabulary(): Boolean {
        val eng = inputEng.getText().toString()
        val kor = inputKor.getText().toString()
        val memo = inputMemo.getText().toString()
        val time = (Calendar.getInstance().timeInMillis / 1000) as Int
        if (eng == null || eng == "") {
            return false
        }
        val newVocabulary = Vocabulary(eng, kor, vocabulary.addedTime, time, memo)
        if (vocabulary.eng == newVocabulary.eng) {
            vocaViewModel.editVocabulary(newVocabulary)
        } else {
            vocaViewModel.deleteVocabulary(vocabulary)
            vocaViewModel.insertVocabulary(newVocabulary)
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