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

import Database.Vocabulary;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;


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
                showVocaNumber(vocabularies.size());
                tryShowRandomVocabulary();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vocaViewModel.isEmpty()) {
                    Snackbar.make(v, "버튼을 눌러 단어를 추가해 주세요.", Snackbar.LENGTH_LONG).show();
                } else {
                    showRandomVocabulary();
                }
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
        if (showVocaWhenFragmentPause && !vocaViewModel.isEmpty()) {
            showRandomVocabulary();
            showVocaWhenFragmentPause = false;
            button.setVisibility(View.VISIBLE);
        }
    }

    private void showRandomVocabulary() {
        int index = random.nextInt(allVocabulary.getValue().size());
        Vocabulary vocabulary = allVocabulary.getValue().get(index);
        homeEng.setText(vocabulary.eng);
        homeKor.setText(vocabulary.kor);
    }

    private void showVocaNumber(int number) {
        vocaNumber.setVisibility(View.VISIBLE);
        vocaNumber.setText(Integer.toString(number) + "단어");
    }
}