package hsk.practice.myvoca.ui.activity

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.hsk.data.VocaRepository
import dagger.hilt.android.AndroidEntryPoint
import hsk.practice.myvoca.Constants
import hsk.practice.myvoca.R
import hsk.practice.myvoca.VocabularyImpl
import hsk.practice.myvoca.databinding.ActivityAddVocaBinding
import hsk.practice.myvoca.framework.toVocabulary
import hsk.practice.myvoca.module.RoomVocaRepository
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * Activity where users can add word.
 * inputEng must not be empty because it is the Primary Key of the database.
 * Other fields can be empty.
 */
@AndroidEntryPoint
class AddVocaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVocaBinding

    @RoomVocaRepository
    @Inject
    lateinit var vocaRepository: VocaRepository

    private var resultCode = Constants.ADD_NEW_VOCA_CANCEL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_voca)

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
        val time = Calendar.getInstance().timeInMillis / 1000
        val vocabulary = VocabularyImpl(
            eng = eng,
            kor = kor,
            addedTime = time,
            lastEditedTime = time,
            memo = memo
        )
        lifecycleScope.launch {
            vocaRepository.insertVocabulary(vocabulary.toVocabulary())
        }
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