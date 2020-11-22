package hsk.practice.myvoca.ui.quiz;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import database.PreferenceManager;
import database.Vocabulary;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;
import hsk.practice.myvoca.ui.customview.VersusView;

/**
 * Shows word quiz to user.
 * There should be at least 5 words in the database to make the quiz shown.
 * When the word is not enough, notification text will be shown instead.
 * User should choose the correct meaning of the quiz word. Four options will be provided.
 * When user selects the option, quiz word and the VersusView at the bottom of the fragment will be updated.
 * <p>
 * Numbers of correct and wrong answers are stored in the SharedPreferences and updated in real time.
 */
public class QuizFragment extends Fragment {
    private ViewModelProvider viewModelProvider;

    private QuizViewModel quizViewModel;
    private VocaViewModel vocaViewModel;

    RelativeLayout noVocaLayout;
    TextView vocaCountText;

    LinearLayout quizLayout;
    TextView quizWordText;
    TextView quizOption1;
    TextView quizOption2;
    TextView quizOption3;
    TextView quizOption4;
    List<TextView> quizOptionsList;

    VersusView versusView;

    Vocabulary answerVoca;
    int answerIndex;

    int answerCount = 0;
    int wrongCount = 0;

    Handler handler;
    Runnable showQuizRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModelProvider = new ViewModelProvider(this);
        quizViewModel = viewModelProvider.get(QuizViewModel.class);
        vocaViewModel = viewModelProvider.get(VocaViewModel.class);
        View root = inflater.inflate(R.layout.fragment_quiz, container, false);

        handler = new Handler();

        noVocaLayout = root.findViewById(R.id.layout_no_voca);
        vocaCountText = root.findViewById(R.id.text_view_cur_voca);

        quizLayout = root.findViewById(R.id.quiz_layout);
        quizWordText = root.findViewById(R.id.quiz_word);
        quizOption1 = root.findViewById(R.id.quiz_option1);
        quizOption2 = root.findViewById(R.id.quiz_option2);
        quizOption3 = root.findViewById(R.id.quiz_option3);
        quizOption4 = root.findViewById(R.id.quiz_option4);

        versusView = root.findViewById(R.id.versus_view);

        quizOptionsList = Arrays.asList(quizOption1, quizOption2, quizOption3, quizOption4);
        for (int i = 0; i < 4; i++) {
            TextView option = quizOptionsList.get(i);
            final int finalI = i;
            option.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quizItemSelected(finalI);
                }
            });
        }

        answerCount = PreferenceManager.getInt(getContext(), PreferenceManager.QUIZ_CORRECT);
        wrongCount = PreferenceManager.getInt(getContext(), PreferenceManager.QUIZ_WRONG);
        versusView.setValues(answerCount, wrongCount);

        showQuizRunnable = new Runnable() {
            @Override
            public void run() {
                vocaViewModel.getVocabularyCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        Log.d("HSK APP", integer.toString());
                        if (integer > 4) {
                            hideEmptyVocaLayout();
                            showQuizLayout();
                            showQuizWord();
                        } else {
                            hideQuizLayout();
                            showEmptyVocaLayout(integer);
                        }
                    }
                });
            }
        };
        tryShowQuizWord();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        tryShowQuizWord();
    }

    public void showEmptyVocaLayout(int vocaCount) {
        noVocaLayout.setVisibility(View.VISIBLE);
        vocaCountText.setText(getString(R.string.current_voca_count, vocaCount));
    }

    public void hideEmptyVocaLayout() {
        noVocaLayout.setVisibility(View.GONE);
        vocaCountText.setVisibility(View.GONE);
    }

    public void showQuizLayout() {
        quizLayout.setVisibility(View.VISIBLE);
        quizWordText.setVisibility(View.VISIBLE);
    }

    public void hideQuizLayout() {
        quizLayout.setVisibility(View.GONE);
        quizWordText.setVisibility(View.GONE);
    }

    public void tryShowQuizWord() {
        handler.postDelayed(showQuizRunnable, 50);
    }

    public void showQuizWord() {
        Vocabulary answer = vocaViewModel.getRandomVocabulary().getValue();
        List<Vocabulary> optionsList = vocaViewModel.getRandomVocabularies(3, answer);
        optionsList.add(answer);

        try {
            quizWordText.setText(answer.eng);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        Collections.shuffle(optionsList);
        for (int i = 0; i < 4; i++) {
            Vocabulary option = optionsList.get(i);
            if (Objects.equals(answer, option)) {
                answerVoca = option;
                answerIndex = i;
            }
            quizOptionsList.get(i).setText(String.format("%d. %s", i + 1, formatString(option.kor)));
        }
    }

    // replace new line character to the space
    public String formatString(String str) {
        return str.replace("\n", " ");
    }

    public void quizItemSelected(int index) {
        boolean isCorrect;
        Context context = getContext();
        if (answerIndex == index) {
            isCorrect = true;
            answerCount++;
            PreferenceManager.setInt(context, PreferenceManager.QUIZ_CORRECT, answerCount);
            versusView.setLeftValue(answerCount);
//            Toast.makeText(context, String.format("정답: %d", answerCount), Toast.LENGTH_LONG).show();
        } else {
            isCorrect = false;
            wrongCount++;
            PreferenceManager.setInt(context, PreferenceManager.QUIZ_WRONG, wrongCount);
            versusView.setRightValue(wrongCount);
//            Toast.makeText(context, String.format("오답: %d", wrongCount), Toast.LENGTH_LONG).show();
        }
        showVocaDialog(answerVoca, isCorrect);
        showQuizWord();
    }

    // shows whether the selection is correct
    public void showVocaDialog(Vocabulary voca, boolean isCorrect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(isCorrect ? "맞았습니다!!" : "틀렸습니다");
        builder.setMessage(String.format("%s: %s", voca.eng, formatString(voca.kor)));
        builder.setPositiveButton(android.R.string.ok, null);

        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}