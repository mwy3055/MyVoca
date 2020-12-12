package hsk.practice.myvoca.ui.activity

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import database.Vocabulary
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocaViewModel
import hsk.practice.myvoca.databinding.ActivityAddVocaBinding
import java.util.*

/**
 * Activity where users can add word.
 * inputEng must not be empty because it is the Primary Key of the database.
 * Other fields can be empty.
 */
class AddVocaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVocaBinding

    private var viewModelProvider: ViewModelProvider? = null
    private var vocaViewModel: VocaViewModel? = null
    private var resultCode = Constants.ADD_NEW_VOCA_CANCEL
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddVocaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModelProvider = ViewModelProvider(this)
        vocaViewModel = viewModelProvider!!.get(VocaViewModel::class.java)
        val toolbar = findViewById<Toolbar?>(R.id.toolbar_activity_new_voca)
        setSupportActionBar(toolbar)

        // Show back arrow icon in the Action Bar
        val actionBar = supportActionBar
        actionBar?.displayOptions = ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_SHOW_TITLE
        binding.addButtonOk.setOnClickListener {
            val eng = binding.addInputEng.text.toString()
            if (eng.isEmpty()) {
                Toast.makeText(application, "단어를 입력해 주세요.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            addVocabulary()
            finish()
        }
        binding.addButtonCancel.setOnClickListener { finish() }
    }

    private fun addVocabulary() {
        resultCode = Constants.ADD_NEW_VOCA_OK
        val eng = binding.addInputEng.text.toString()
        val kor = binding.addInputKor.text.toString()
        val memo = binding.addInputMemo.text.toString()
        val time = (Calendar.getInstance().timeInMillis / 1000).toInt()
        val vocabulary = Vocabulary(eng, kor, time, time, memo)
        vocaViewModel?.insertVocabulary(vocabulary)
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