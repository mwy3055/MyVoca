package database.source;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import database.Vocabulary;
import database.source.local.VocaDao;
import database.source.local.VocaDatabase;
import hsk.practice.myvoca.AppHelper;

/**
 * VocaRepository mediates between VocaViewModel and VocaRepository.
 * Executes operations at the separate thread.
 *
 * Implemented as Singleton: To unify the management process
 */
public class VocaRepository {

    private VocaDao vocaDao;
    private final ExecutorService executor = Executors.newCachedThreadPool();

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
                instance.database = VocaDatabase.getInstance();
                instance.vocaDao = VocaDatabase.getInstance().vocaDao();
            }
        }
    }

    private void loadVocabulary() {
        try {
            executor.execute(LoadTask);
            allVocabulary = LoadTask.get(10, TimeUnit.SECONDS);
            allVocabulary.observeForever(new Observer<List<Vocabulary>>() {
                @Override
                public void onChanged(List<Vocabulary> vocabularies) {
                    Log.d("HSK APP", "load complete, " + vocabularies.size());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LiveData<List<Vocabulary>> getVocabulary(String query) {
        if (AppHelper.isStringOnlyAlphabet(query)) {
            Log.d("HSK APP", "search by eng: " + query);
            return vocaDao.loadVocabularyByEng(query);
        } else {
            Log.d("HSK APP", "search by kor: " + query);
            return vocaDao.loadVocabularyByKor(query);
        }
    }

    public LiveData<List<Vocabulary>> getAllVocabulary() {
        if (allVocabulary == null || allVocabulary.getValue() == null) {
            loadVocabulary();
        }
        return allVocabulary;
    }

    public LiveData<Vocabulary> getRandomVocabulary() {
        final MutableLiveData<Vocabulary> result = new MutableLiveData<>();
        final Random random = new Random();

        if (allVocabulary == null || allVocabulary.getValue() == null) {
            loadVocabulary();
            allVocabulary.observeForever(new Observer<List<Vocabulary>>() {
                @Override
                public void onChanged(List<Vocabulary> vocabularies) {
                    int index = random.nextInt(vocabularies.size());
                    result.setValue(vocabularies.get(index));
                    allVocabulary.removeObserver(this);
                }
            });
        } else {
            int index = random.nextInt(allVocabulary.getValue().size());
            result.setValue(allVocabulary.getValue().get(index));
        }
        return result;
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

    private FutureTask<LiveData<List<Vocabulary>>> LoadTask = new FutureTask<LiveData<List<Vocabulary>>>(
            new Callable<LiveData<List<Vocabulary>>>() {
                @Override
                public LiveData<List<Vocabulary>> call() throws Exception {
                    return vocaDao.loadAllVocabulary();
                }
            }
    );
}
