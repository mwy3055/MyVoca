package hsk.practice.myvoca.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hsk.data.VocaPersistence
import java.lang.reflect.InvocationTargetException

class NewVocaViewModelFactory(private val vocaPersistence: VocaPersistence) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewVocaViewModel::class.java)) {
            try {
                return modelClass.getConstructor(VocaPersistence::class.java)
                        .newInstance(vocaPersistence)
            } catch (e: NoSuchMethodException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: IllegalAccessException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InstantiationException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            } catch (e: InvocationTargetException) {
                throw RuntimeException("Cannot create an instance of $modelClass", e)
            }
        } else {
            return super.create(modelClass)
        }
    }
}