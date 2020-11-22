package hsk.practice.myvoca.ui.quiz;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * This ViewModel is no use, too.
 */
public class QuizViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QuizViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}