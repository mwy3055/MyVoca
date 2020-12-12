package hsk.practice.myvoca.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel provides data used in the fragment.
 * But here, all data is managed by the VocaViewModel.
 * There is no fragment-dependent data in this application, so each ViewModel has nothing to do.
 * Just left for further use.
 */
public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        //mText.setValue("dtd");
    }

    public LiveData<String> getText() {
        return mText;
    }
}