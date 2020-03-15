package hsk.practice.myvoca;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.util.List;

import Database.Vocabulary;
import Database.source.VocaRepository;


public class VocaViewModel extends ViewModel {

    private LiveData<List<Vocabulary>> allVocabularies;
    private Vocabulary vocabulary;
    private VocaRepository vocaRepo;

    public VocaViewModel() {
        this.vocaRepo = VocaRepository.getInstance();
        loadVocabularies();
    }

    public LiveData<List<Vocabulary>> getAllVocabulary() {
        if (allVocabularies == null || allVocabularies.getValue() == null) {
            loadVocabularies();
        }
        return allVocabularies;
    }

    public LiveData<Integer> getVocabularyCount() {
        final MutableLiveData<Integer> result = new MutableLiveData<>();
        if (allVocabularies == null || allVocabularies.getValue() == null) {
            loadVocabularies();
            allVocabularies.observeForever(new Observer<List<Vocabulary>>() {
                @Override
                public void onChanged(List<Vocabulary> vocabularies) {
                    result.setValue(vocabularies.size());
                    allVocabularies.removeObserver(this);
                }
            });
        } else {
            result.setValue(allVocabularies.getValue().size());
        }
        return result;
    }

    public void deleteVocabulary(Vocabulary... vocabularies) {
        vocaRepo.deleteVocabularies(vocabularies);
    }

    public LiveData<List<Vocabulary>> getVocabulary(String query) {
        return vocaRepo.getVocabulary(query);
    }

    public void insertVocabulary(Vocabulary... vocabularies) {
        vocaRepo.insertVocabulary(vocabularies);
    }

    private synchronized void loadVocabularies() {
        // do what?
        allVocabularies = vocaRepo.getAllVocabulary();
    }

    public void editVocabulary(Vocabulary vocabulary) {
        vocaRepo.editVocabulary(vocabulary);
    }

    public LiveData<Vocabulary> getRandomVocabulary() {
        return vocaRepo.getRandomVocabulary();
    }

    public LiveData<Boolean> isEmpty() {
        final MutableLiveData<Boolean> result = new MutableLiveData<>(true);
        if (allVocabularies == null || allVocabularies.getValue() == null) {
            loadVocabularies();
            allVocabularies.observeForever(new Observer<List<Vocabulary>>() {
                @Override
                public void onChanged(List<Vocabulary> vocabularies) {
                    result.setValue(vocabularies.size() == 0);
                    allVocabularies.removeObserver(this);
                }
            });
        } else {
            result.setValue(allVocabularies.getValue().size() == 0);
        }
        return result;
    }
}
