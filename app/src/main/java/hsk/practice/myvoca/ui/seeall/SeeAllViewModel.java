package hsk.practice.myvoca.ui.seeall;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SeeAllViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SeeAllViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("dtd");
    }

    public LiveData<String> getText() {
        return mText;
    }
}