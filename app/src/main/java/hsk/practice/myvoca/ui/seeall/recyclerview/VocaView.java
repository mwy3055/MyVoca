package hsk.practice.myvoca.ui.seeall.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.Calendar;

import Database.Vocabulary;
import hsk.practice.myvoca.R;

public class VocaView extends LinearLayout {

    private LinearLayout vocaLayout;

    private TextView vocaKor;
    private TextView vocaEng;
    private TextView lastEditTime;

    public CheckBox deleteCheckBox;

    public VocaView(Context context) {
        super(context);
        init(context);
    }

    public VocaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.voca_layout, this, true);

        vocaLayout = findViewById(R.id.voca_layout);
        vocaKor = findViewById(R.id.voca_kor);
        vocaEng = findViewById(R.id.voca_eng);
        lastEditTime = findViewById(R.id.last_edit_time);
        deleteCheckBox = findViewById(R.id.delete_check_box);
    }

    public void setVocabulary(Vocabulary vocabulary) {
        setVocaKor(vocabulary.kor);
        setVocaEng(vocabulary.eng);
        setLastEditTime(vocabulary.lastEditedTime);
    }

    public void setVocaKor(String kor) {
        vocaKor.setText(kor);
    }

    public void setVocaEng(String eng) {
        vocaEng.setText(eng);
    }

    public void setLastEditTime(int time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((long) time * 1000);

        int year = cal.get(Calendar.YEAR);
        int mon = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        lastEditTime.setText(String.format("%d.%02d.%02d. %02d:%02d:%02d", year, mon + 1, day, hour, min, sec));
    }

}
