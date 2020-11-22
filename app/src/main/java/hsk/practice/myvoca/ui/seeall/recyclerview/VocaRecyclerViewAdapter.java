package hsk.practice.myvoca.ui.seeall.recyclerview;

import android.os.Handler;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import database.VocaComparator;
import database.Vocabulary;
import hsk.practice.myvoca.Constants;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;
import hsk.practice.myvoca.ui.seeall.OnDeleteModeListener;
import hsk.practice.myvoca.ui.seeall.OnEditVocabularyListener;

public class VocaRecyclerViewAdapter extends RecyclerView.Adapter<VocaRecyclerViewAdapter.VocaViewHolder>
        implements OnDeleteModeListener {

    public interface OnVocaClickListener {
        void onVocaClick(VocaViewHolder holder, View view, int position);

        boolean onVocaLongClick(VocaViewHolder holder, View view, int position);
    }

    public interface showVocaOnNotification {
        void showVocabularyOnNotification(Vocabulary vocabulary);
    }

    public interface OnSelectModeListener {
        void onDeleteModeEnabled();

        void onDeleteModeDisabled();
    }

    private static VocaRecyclerViewAdapter instance;
    private static int sortState = 0;

    private ViewModelProvider viewModelProvider;

    private OnVocaClickListener vocaClickListener;
    private showVocaOnNotification showVocaOnNotification;

    private AppCompatActivity activity;
    private LiveData<List<Vocabulary>> currentVocabulary;
    private VocaViewModel vocaViewModel;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean deleteMode = false;
    private OnSelectModeListener onDeleteModeListener;

    private OnEditVocabularyListener onEditVocabularyListener;

    private boolean searchMode = false;

    private Handler handler;
    private int observeDelay = 400;

    public static VocaRecyclerViewAdapter getInstance(AppCompatActivity activity) {
        if (instance == null) {
            synchronized (VocaRecyclerViewAdapter.class) {
                instance = new VocaRecyclerViewAdapter(activity);
            }
        }
        return instance;
    }

    private VocaRecyclerViewAdapter(final AppCompatActivity activity) {
        this.activity = activity;
        viewModelProvider = new ViewModelProvider(activity);
        vocaViewModel = viewModelProvider.get(VocaViewModel.class);

        currentVocabulary = vocaViewModel.getAllVocabulary();
    }

    /* Notify adapter to show added item on screen immediately */
    public void observe() {
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        }, observeDelay);
    }

    @NonNull
    @Override
    public VocaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VocaView view = new VocaView(activity);
        VocaViewHolder holder = new VocaViewHolder(view, this, onEditVocabularyListener);

        holder.setVocaClickListener(vocaClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VocaViewHolder holder, int position) {
        if (currentVocabulary.getValue() == null) {
            holder.setVocabulary(new Vocabulary("null", "널입니다.", (int) (System.currentTimeMillis() / 1000), (int) (System.currentTimeMillis() / 1000), ""));
            holder.setVocaClickListener(vocaClickListener);
            return;
        }

        Vocabulary vocabulary = currentVocabulary.getValue().get(position);
        holder.setVocabulary(vocabulary);
        holder.setVocaClickListener(vocaClickListener);

        CheckBox checkBox = holder.vocaView.deleteCheckBox;
        if (deleteMode) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(selectedItems.get(position));
            holder.vocaView.invalidate();
        } else {
            checkBox.setVisibility(View.GONE);
            checkBox.setChecked(false);
        }
    }

    /* this should be used only when setting observer to LiveData object
     */
    public LiveData<List<Vocabulary>> getCurrentVocabulary() {
        return currentVocabulary;
    }

    @Override
    public int getItemCount() {
        if (currentVocabulary.getValue() == null) {
            return -1;
        }
        return currentVocabulary.getValue().size();
    }

    public Object getItem(int position) {
        return currentVocabulary.getValue().get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    public int getItemPosition(Vocabulary vocabulary) {
        return currentVocabulary.getValue().indexOf(vocabulary);
    }

    /* Getter/Setters of Listeners */
    public OnVocaClickListener getVocaClickListener() {
        return vocaClickListener;
    }

    public void setOnDeleteModeListener(OnSelectModeListener listener) {
        this.onDeleteModeListener = listener;
    }

    public void setVocaClickListener(OnVocaClickListener vocaClickListener) {
        this.vocaClickListener = vocaClickListener;
    }

    public void setOnEditVocabularyListener(OnEditVocabularyListener listener) {
        this.onEditVocabularyListener = listener;
    }

    public void setShowVocaOnNotificationListener(showVocaOnNotification notificationListener) {
        this.showVocaOnNotification = notificationListener;
    }


    /* Methods for selected state */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    public void switchSelectedState(int position) {
        if (selectedItems.get(position)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void clearSelectedState() {
        selectedItems.clear();
        notifyItemsChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
    }

    public boolean isDeleteMode() {
        return this.deleteMode;
    }

    public void notifyItemsChanged() {
        for (int i = 0; i < currentVocabulary.getValue().size(); i++) {
            notifyItemChanged(i);
        }
    }

    /* for search mode */
    public void enableSearchMode() {
        searchMode = true;
    }

    public void disableSearchMode() {
        if (searchMode) {
            searchMode = false;
            currentVocabulary = vocaViewModel.getAllVocabulary();
            sortItems(sortState);
            notifyDataSetChanged();
        }
    }

    public void searchVocabulary(final String query) {
        currentVocabulary = vocaViewModel.getVocabulary("%" + query + "%");
        currentVocabulary.observe(activity, new Observer<List<Vocabulary>>() {
            @Override
            public void onChanged(List<Vocabulary> vocabularies) {
                Log.d("HSK APP", "Searched " + query + ": " + (currentVocabulary.getValue() == null ? -1 : currentVocabulary.getValue().size()));
                sortItems(sortState);
                notifyDataSetChanged();
            }
        });
    }

    /* for remove and restore item */
    public void removeItem(int position) {
        Vocabulary deletedVocabulary = currentVocabulary.getValue().get(position);
        currentVocabulary.getValue().remove(position);
        notifyItemRemoved(position);

        vocaViewModel.deleteVocabulary(deletedVocabulary);
    }

    public void deleteVocabulary() {
        List<Integer> selected = getSelectedItems();
        // reverse iteration
        for (ListIterator iterator = selected.listIterator(selected.size()); iterator.hasPrevious(); ) {
            Vocabulary vocabulary = (Vocabulary) getItem((int) iterator.previous());
            vocaViewModel.deleteVocabulary(vocabulary);
        }
        if (isDeleteMode()) {
            disableDeleteMode();
        }
    }

    public void restoreItem(Vocabulary vocabulary, int position) {
        currentVocabulary.getValue().add(position, vocabulary);
        notifyItemInserted(position);

        vocaViewModel.insertVocabulary(vocabulary);
    }

    /* for sorting items */
    public void sortItems(int method) {
        if (currentVocabulary == null || currentVocabulary.getValue() == null) {
            return;
        }
        sortState = method;
        if (sortState == 0) {
            Collections.sort(currentVocabulary.getValue(), VocaComparator.getEngComparator());
        } else if (sortState == 1) {
            Collections.sort(currentVocabulary.getValue(), VocaComparator.getAddedTimeComparator());
        } else {
            sortState = 0;
            Toast.makeText(activity.getApplicationContext(), "정렬할 수 없습니다: " + method, Toast.LENGTH_LONG).show();
        }
        notifyDataSetChanged();
    }

    /* See SeeAllFragment.onDeleteModeEnabled() Method */
    @Override
    public void enableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE ENABLED");
        setDeleteMode(true);
        notifyItemsChanged();
        if (onDeleteModeListener != null) {
            onDeleteModeListener.onDeleteModeEnabled();
        }
    }

    /* See SeeAllFragment.onDeleteModeDisabled() Method */
    @Override
    public void disableDeleteMode() {
        Log.d("HSK APP", "DELETE MODE DISABLED");
        setDeleteMode(false);
        if (onDeleteModeListener != null) {
            onDeleteModeListener.onDeleteModeDisabled();
        }
    }


    public class VocaViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {

        OnVocaClickListener vocaClickListener;
        OnDeleteModeListener onDeleteModeListener;
        OnEditVocabularyListener onEditVocabularyListener;

        public RelativeLayout viewForeground, viewBackground;

        private MenuItem.OnMenuItemClickListener onMenuItemClickListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int position = getAdapterPosition();
                Vocabulary vocabulary = currentVocabulary.getValue().get(position);
                Log.d("HSK APP", Integer.toString(position));
                switch (item.getItemId()) {
                    case Constants.EDIT_CODE:
                        Log.d("HSK APP", "edit: " + vocabulary.eng);
                        onEditVocabularyListener.editVocabulary(position, vocabulary);
                        break;
                    case Constants.DELETE_CODE:
                        VocaRecyclerViewAdapter adapter = VocaRecyclerViewAdapter.this;
                        adapter.enableDeleteMode();
                        adapter.switchSelectedState(position);
                        break;
                    case Constants.SHOW_ON_NOTIFICATION_CODE:
                        // TODO: show selected vocabulary on notification
                        showVocaOnNotification.showVocabularyOnNotification(vocabulary);
                        break;
                }
                return true;
            }
        };

        public VocaView vocaView;

        public VocaViewHolder(@NonNull View vocaView,
                              OnDeleteModeListener onDeleteModeListener,
                              OnEditVocabularyListener onEditVocabularyListener) {
            super(vocaView);

            viewBackground = vocaView.findViewById(R.id.view_background);
            viewForeground = vocaView.findViewById(R.id.view_foreground);

            this.vocaView = (VocaView) vocaView;
            this.vocaView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (vocaClickListener != null) {
                        int position = getAdapterPosition();
                        vocaClickListener.onVocaClick(VocaViewHolder.this, v, position);
                    }
                }
            });
            this.vocaView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (vocaClickListener != null) {
                        int position = getAdapterPosition();
                        return vocaClickListener.onVocaLongClick(VocaViewHolder.this, v, position);
                    }
                    return false;
                }
            });

            this.onDeleteModeListener = onDeleteModeListener;
            this.onEditVocabularyListener = onEditVocabularyListener;

            vocaView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (VocaRecyclerViewAdapter.getInstance(activity).deleteMode) {
                return;
            }
            MenuItem edit = menu.add(Menu.NONE, Constants.EDIT_CODE, 1, "수정");
            MenuItem delete = menu.add(Menu.NONE, Constants.DELETE_CODE, 2, "삭제");
//            MenuItem showOnNotification = menu.add(Menu.NONE, Constants.SHOW_ON_NOTIFICATION_CODE, 3, "알림에 보이기");

            edit.setOnMenuItemClickListener(onMenuItemClickListener);
            delete.setOnMenuItemClickListener(onMenuItemClickListener);
//            showOnNotification.setOnMenuItemClickListener(onMenuItemClickListener);
        }

        public void setVocabulary(Vocabulary vocabulary) {
            vocaView.setVocabulary(vocabulary);
        }

        public void setVocaClickListener(OnVocaClickListener vocaClickListener) {
            this.vocaClickListener = vocaClickListener;
        }
    }
}
