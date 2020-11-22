package hsk.practice.myvoca.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Random;

import database.Vocabulary;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;

/**
 * First-shown fragment
 * Shows a random word. Press the button to change the word.
 * If there is no word in the database, notification text will be shown instead.
 */
public class HomeFragment extends Fragment {

    private ViewModelProvider viewModelProvider;

    private HomeViewModel homeViewModel;
    private VocaViewModel vocaViewModel;

    private LiveData<List<Vocabulary>> allVocabulary;
    private boolean showVocaWhenFragmentPause = true;

    private Random random = new Random();

    private TextView vocaNumber;
    private TextView homeEng;
    private TextView homeKor;
    private Button button;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("HSK APP", "HomeFragment onCreateView(), " + Boolean.toString(showVocaWhenFragmentPause));
        viewModelProvider = new ViewModelProvider(this);
        homeViewModel = viewModelProvider.get(HomeViewModel.class);
        vocaViewModel = viewModelProvider.get(VocaViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        vocaNumber = root.findViewById(R.id.home_voca_number);
        homeEng = root.findViewById(R.id.home_eng);
        homeKor = root.findViewById(R.id.home_kor);
        button = root.findViewById(R.id.home_load_new_vocabulary_button);

        allVocabulary = vocaViewModel.getAllVocabulary();
        allVocabulary.observe(getViewLifecycleOwner(), new Observer<List<Vocabulary>>() {
            @Override
            public void onChanged(List<Vocabulary> vocabularies) {
                if (vocabularies.size() > 0) {
                    showVocaNumber(vocabularies.size());
                    tryShowRandomVocabulary();
                }
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                LiveData<Boolean> isEmpty = vocaViewModel.isEmpty();
                isEmpty.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            Snackbar.make(v, "버튼을 눌러 단어를 추가해 주세요.", Snackbar.LENGTH_LONG).show();
                        } else {
                            showRandomVocabulary();
                        }
                    }
                });
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        Log.d("HSK APP", "HomeFragment onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("HSK APP", "HomeFragment onPause()");
        super.onPause();
        showVocaWhenFragmentPause = true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void tryShowRandomVocabulary() {
        final LiveData<Boolean> isEmpty = vocaViewModel.isEmpty();
        isEmpty.observeForever(new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (showVocaWhenFragmentPause && !aBoolean) {
                    showRandomVocabulary();
                    showVocaWhenFragmentPause = false;
                    button.setVisibility(View.VISIBLE);
                }
                isEmpty.removeObserver(this);
            }
        });
    }

    private void showRandomVocabulary() {
        final LiveData<Vocabulary> randomVocabulary = vocaViewModel.getRandomVocabulary();
        randomVocabulary.observeForever(new Observer<Vocabulary>() {
            @Override
            public void onChanged(Vocabulary vocabulary) {
                homeEng.setText(vocabulary.eng);
                homeKor.setText(vocabulary.kor);
                randomVocabulary.removeObserver(this);
            }
        });
    }

    private void showVocaNumber(int number) {
        vocaNumber.setVisibility(View.VISIBLE);
        vocaNumber.setText(Integer.toString(number) + "단어");
    }
}