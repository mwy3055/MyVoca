package Database.source;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import Database.Vocabulary;
import Database.source.local.VocaDao;
import Database.source.local.VocaDatabase;

// Singleton
public class VocaRepository {

    private VocaDao vocaDao;
    private final Executor executor = Executors.newSingleThreadExecutor();

    private static VocaRepository instance;
    private VocaDatabase database;

    private LiveData<List<Vocabulary>> allVocabulary;

    public static VocaRepository getInstance() {
        if (instance == null) {
            loadInstance();
        }
        return instance;
    }

    public static void loadInstance() {
        synchronized (VocaRepository.class) {
            if (instance == null) {
                instance = new VocaRepository();
                instance.vocaDao = VocaDatabase.getInstance().vocaDao();
                instance.database = VocaDatabase.getInstance();
            }
        }
    }

    public LiveData<List<Vocabulary>> getVocabulary(final String eng) {
       /* MutableLiveData<List<Vocabulary>> rtn = new MutableLiveData<>();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                rtn.setValue(vocaDao.loadVocabulary(eng));
            }
        });*/


        return vocaDao.loadVocabulary(eng);
    }

    public LiveData<List<Vocabulary>> getAllVocabulary() {
        if (allVocabulary == null || allVocabulary.getValue() == null) {
            allVocabulary = vocaDao.loadAllVocabulary();
        }
        return allVocabulary;
    }

    public void insertVocabulary(final Vocabulary... vocabularies) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                vocaDao.insertVocabulary(vocabularies);
            }
        });
    }

    public void editVocabulary(final Vocabulary... vocabularies) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                vocaDao.updateVocabulary(vocabularies);
            }
        });
    }

    public void deleteVocabularies(final Vocabulary... vocabularies) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                vocaDao.deleteVocabulary(vocabularies);
            }
        });
    }
}
