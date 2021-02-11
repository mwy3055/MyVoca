package hsk.practice.myvoca.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hsk.data.VocaPersistence
import hsk.practice.myvoca.ui.home.HomeViewModel
import hsk.practice.myvoca.ui.quiz.QuizViewModel
import java.lang.reflect.InvocationTargetException

class VocaViewModelFactory(private val vocaPersistence: VocaPersistence) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        try {
            return if (modelClass.isAssignableFrom(HomeViewModel::class.java)
                    || modelClass.isAssignableFrom(QuizViewModel::class.java)) {
                modelClass.getConstructor(VocaPersistence::class.java).newInstance(vocaPersistence)
            } else {
                super.create(modelClass)
            }
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }
}