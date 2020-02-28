package hsk.practice.myvoca;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Random;

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
        if (allVocabularies == null) {
            loadVocabularies();
        }
        return allVocabularies;
    }

    public int getVocabularyCount() {
        if (allVocabularies == null) {
            loadVocabularies();
        }
        return allVocabularies.getValue().size();
    }

    public void deleteVocabulary(Vocabulary... vocabularies) {
        vocaRepo.deleteVocabularies(vocabularies);
    }

    public LiveData<List<Vocabulary>> getVocabulary(String eng) {
        return vocaRepo.getVocabulary(eng);
    }

    public void insertVocabulary(Vocabulary... vocabularies) {
        vocaRepo.insertVocabulary(vocabularies);
    }

    private void loadVocabularies() {
        // do what?
        allVocabularies = vocaRepo.getAllVocabulary();
    }

    public void editVocabulary(Vocabulary vocabulary) {
        vocaRepo.editVocabulary(vocabulary);
    }

    public LiveData<Vocabulary> getRandomVocabulary() {
        if (allVocabularies == null || allVocabularies.getValue() == null) {
            loadVocabularies();
        }

        Random random = new Random();
        int index = random.nextInt(allVocabularies.getValue().size());
        return new MutableLiveData<>(allVocabularies.getValue().get(index));
    }

    public boolean isEmpty() {
        return allVocabularies.getValue().isEmpty();
    }
}
