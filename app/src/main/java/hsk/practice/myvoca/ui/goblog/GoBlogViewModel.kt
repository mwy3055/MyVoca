package hsk.practice.myvoca.ui.goblog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel provides the data which is used in the fragment
 * That is, fragment doesn't manage the data directly. ViewModel does.
 */
class GoBlogViewModel : ViewModel() {
    private val _goBlogText: MutableLiveData<String> = MutableLiveData()
    val goBlogText: LiveData<String>
        get() = _goBlogText

    init {
        _goBlogText.value = "개발자 블로그로 이동합니다."
    }
}