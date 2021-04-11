package hsk.practice.myvoca.ui.gogithub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GoGitHubViewModel : ViewModel() {
    private val _goBlogText: MutableLiveData<String> = MutableLiveData()
    val goBlogText: LiveData<String>
        get() = _goBlogText

    init {
        _goBlogText.value = "추가할 기능 및 버그 리포트 등은 GitHub Issue에 올려 주세요."
    }
}