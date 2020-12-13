package hsk.practice.myvoca.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * This ViewModel is no use, too.
 */
class QuizViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?
    fun getText(): LiveData<String?>? {
        return mText
    }

    init {
        mText = MutableLiveData()
        mText.value = "This is slideshow fragment"
    }
}