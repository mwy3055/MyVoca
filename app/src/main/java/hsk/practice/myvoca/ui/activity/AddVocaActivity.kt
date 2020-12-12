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
 * Activity where users can add word.
 * inputEng must not be empty because it is the Primary Key of the database.
 * Other fields can be empty.
 */
class AddVocaActivity : AppCompatActivity() {
    private var inputEng: TextInputEditText? = null
    private var inputKor: TextInputEditText? = null
    private var inputMemo: TextInputEditText? = null
    private var buttonOK: Button? = null
    private var buttonCancel: Button? = null
    private var viewModelProvider: ViewModelProvider? = null
    private var vocaViewModel: VocaViewModel? = null
    private var resultCode = Constants.ADD_NEW_VOCA_CANCEL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_voca)
        viewModelProvider = ViewModelProvider(this)
        vocaViewModel = viewModelProvider.get(VocaViewModel::class.java)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar_activity_new_voca)
        setSupportActionBar(toolbar)

        // Show back arrow icon in the Action Bar
        val actionBar = supportActionBar
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE)
        inputEng = findViewById(R.id.add_input_eng)
        inputKor = findViewById(R.id.add_input_kor)
        inputMemo = findViewById(R.id.add_input_memo)
        buttonOK = findViewById(R.id.add_button_ok)
        buttonCancel = findViewById(R.id.add_button_cancel)
        buttonOK.setOnClickListener(View.OnClickListener {
            val eng = inputEng.getText().toString()
            if (eng == null || eng.isEmpty()) {
                Toast.makeText(application, "단어를 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }
            addVocabulary()
            finish()
        })
        buttonCancel.setOnClickListener(View.OnClickListener { finish() })
    }

    private fun addVocabulary() {
        resultCode = Constants.ADD_NEW_VOCA_OK
        val eng = inputEng.getText().toString()
        val kor = inputKor.getText().toString()
        val memo = inputMemo.getText().toString()
        val time = (Calendar.getInstance().timeInMillis / 1000) as Int
        val vocabulary = Vocabulary(eng, kor, time, time, memo)
        vocaViewModel.insertVocabulary(vocabulary)
        Toast.makeText(application, "추가 완료!", Toast.LENGTH_LONG).show()
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
        setResult(resultCode)
        super.finish()
    }
}