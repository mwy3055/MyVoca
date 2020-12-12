package hsk.practice.myvoca.ui.seeall.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import database.Vocabulary
import hsk.practice.myvoca.AppHelper
import hsk.practice.myvoca.R

/**
 * Custom view which shows a vocabulary object in the database.
 * Shows english word, korean meaning and the last-edited time.
 */
class VocaView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private var vocaLayout: LinearLayout? = null
    private var vocaKor: TextView? = null
    private var vocaEng: TextView? = null
    private var lastEditTime: TextView? = null
    var deleteCheckBox: CheckBox? = null


    private fun init(context: Context?) {
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.voca_layout, this, true)
        vocaLayout = findViewById(R.id.voca_layout)
        vocaKor = findViewById(R.id.voca_kor)
        vocaEng = findViewById(R.id.voca_eng)
        lastEditTime = findViewById(R.id.last_edit_time)
        deleteCheckBox = findViewById(R.id.delete_check_box)
    }

    fun setVocabulary(vocabulary: Vocabulary?) {
        if (vocabulary != null) {
            setVocaKor(vocabulary.kor)
        }
        if (vocabulary != null) {
            setVocaEng(vocabulary.eng)
        }
        if (vocabulary != null) {
            setLastEditTime(vocabulary.lastEditedTime)
        }
    }

    fun setVocaKor(kor: String?) {
        vocaKor?.text = kor
    }

    fun setVocaEng(eng: String?) {
        vocaEng?.text = eng
    }

    fun setLastEditTime(time: Int) {
        lastEditTime?.text = AppHelper.getTimeString(time as Long * 1000)
    }
}