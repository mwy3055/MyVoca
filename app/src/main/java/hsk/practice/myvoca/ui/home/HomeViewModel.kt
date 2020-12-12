package hsk.practice.myvoca.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel provides data used in the fragment.
 * But here, all data is managed by the VocaViewModel.
 * There is no fragment-dependent data in this application, so each ViewModel has nothing to do.
 * Just left for further use.
 */
class HomeViewModel : ViewModel() {
    private val mText: MutableLiveData<String?>?
    fun getText(): LiveData<String?>? {
        return mText
    }

    init {
        mText = MutableLiveData()
        //mText.setValue("dtd");
    }
}