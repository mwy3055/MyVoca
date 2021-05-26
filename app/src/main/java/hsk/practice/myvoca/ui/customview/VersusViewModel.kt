package hsk.practice.myvoca.ui.customview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VersusViewModel @Inject constructor() : ViewModel() {

    private val _leftValue = MutableLiveData(0)
    val leftValue: LiveData<Int>
        get() = _leftValue

    private val _rightValue = MutableLiveData(0)
    val rightValue: LiveData<Int>
        get() = _rightValue

    fun setLeftValue(value: Int) {
        if (value >= 0) {
            _leftValue.postValue(value)
        }
    }

    fun setRightValue(value: Int) {
        if (value >= 0) {
            _rightValue.postValue(value)
        }
    }

    fun increaseLeftValue() {
        _leftValue.value = _leftValue.value!! + 1
    }

    fun increaseRightValue() {
        _rightValue.value = _rightValue.value!! + 1
    }

}