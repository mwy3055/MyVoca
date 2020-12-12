package hsk.practice.myvoca.ui.goblog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel provides the data which is used in the fragment
 * That is, fragment doesn't manage the data directly. ViewModel does.
 */
class GoBlogViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?
    fun getText(): LiveData<String?>? {
        return mText
    }

    init {
        mText = MutableLiveData()
        mText.value = "개발자 블로그로 이동합니다."
    }
}