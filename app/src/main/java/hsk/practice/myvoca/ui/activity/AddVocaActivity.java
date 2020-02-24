package hsk.practice.myvoca.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import Database.Vocabulary;
import hsk.practice.myvoca.Constants;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;

public class AddVocaActivity extends AppCompatActivity {

    private TextInputEditText inputEng;
    private TextInputEditText inputKor;
    private TextInputEditText inputMemo;

    private Button buttonOK;
    private Button buttonCancel;

    private ViewModelProvider viewModelProvider;
    private VocaViewModel vocaViewModel;

    private int resultCode = Constants.ADD_NEW_VOCA_CANCEL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voca);

        viewModelProvider = new ViewModelProvider(this);
        vocaViewModel = viewModelProvider.get(VocaViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar_activity_new_voca);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);


        inputEng = findViewById(R.id.add_input_eng);
        inputKor = findViewById(R.id.add_input_kor);
        inputMemo = findViewById(R.id.add_input_memo);

        buttonOK = findViewById(R.id.add_button_ok);
        buttonCancel = findViewById(R.id.add_button_cancel);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addVocabulary();
                finish();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void addVocabulary() {
        resultCode = Constants.ADD_NEW_VOCA_OK;

        String eng = inputEng.getText().toString();
        String kor = inputKor.getText().toString();
        String memo = inputMemo.getText().toString();
        int time = (int) (Calendar.getInstance().getTimeInMillis() / 1000);

        Vocabulary vocabulary = new Vocabulary(eng, kor, time, time, memo);
        vocaViewModel.insertVocabulary(vocabulary);
        Toast.makeText(getApplication(), "추가 완료!", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        setResult(resultCode);
        super.finish();
    }
}
