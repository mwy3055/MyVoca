package hsk.practice.myvoca.ui.activity;

import android.content.Intent;
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

public class EditVocaActivity extends AppCompatActivity {

    private int position;
    private Vocabulary vocabulary;
    private int exitCode;

    private TextInputEditText inputEng;
    private TextInputEditText inputKor;
    private TextInputEditText inputMemo;

    private Button buttonOK;
    private Button buttonCancel;

    VocaViewModel vocaViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_voca);

        Intent intent = getIntent();
        position = intent.getIntExtra(Constants.POSITION, 0);
        vocabulary = (Vocabulary) intent.getSerializableExtra(Constants.EDIT_VOCA);

        vocaViewModel = new ViewModelProvider(this).get(VocaViewModel.class);
        Toolbar toolbar = findViewById(R.id.toolbar_activity_edit_voca);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);

        inputEng = findViewById(R.id.edit_input_eng);
        inputKor = findViewById(R.id.edit_input_kor);
        inputMemo = findViewById(R.id.edit_input_memo);
        inputEng.setText(vocabulary.eng);
        inputKor.setText(vocabulary.kor);
        inputMemo.setText(vocabulary.memo);

        buttonOK = findViewById(R.id.edit_button_ok);
        buttonCancel = findViewById(R.id.edit_button_cancel);

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editVocabulary();
                exitCode = Constants.EDIT_NEW_VOCA_OK;
                finish();
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitCode = Constants.EDIT_NEW_VOCA_CANCEL;
                finish();
            }
        });
    }

    private void editVocabulary() {
        String eng = inputEng.getText().toString();
        String kor = inputKor.getText().toString();
        String memo = inputMemo.getText().toString();
        int time = (int) (Calendar.getInstance().getTimeInMillis() / 1000);

        Vocabulary newVocabulary = new Vocabulary(eng, kor, vocabulary.addedTime, time, memo);
        if (vocabulary.eng.equals(newVocabulary.eng)) {
            vocaViewModel.editVocabulary(newVocabulary);
        } else {
            vocaViewModel.deleteVocabulary(vocabulary);
            vocaViewModel.insertVocabulary(newVocabulary);
        }
        Toast.makeText(getApplication(), "수정 완료!", Toast.LENGTH_LONG).show();
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
        setResult(exitCode);
        super.finish();
    }
}
