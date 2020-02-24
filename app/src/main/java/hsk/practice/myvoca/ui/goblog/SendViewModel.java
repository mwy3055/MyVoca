package hsk.practice.myvoca.ui.goblog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SendViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SendViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("개발자 블로그로 이동합니다.");
    }

    public LiveData<String> getText() {
        return mText;
    }
}