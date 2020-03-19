package hsk.practice.myvoca.ui.seeall;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import Database.Vocabulary;
import hsk.practice.myvoca.Constants;
import hsk.practice.myvoca.R;
import hsk.practice.myvoca.VocaViewModel;
import hsk.practice.myvoca.services.notification.ShowNotificationService;
import hsk.practice.myvoca.ui.activity.EditVocaActivity;
import hsk.practice.myvoca.ui.seeall.recyclerview.VocaRecyclerViewAdapter;


public class SeeAllFragment extends Fragment implements VocaRecyclerViewAdapter.OnSelectModeListener,
        VocaRecyclerViewAdapter.showVocaOnNotification,
        VocabularyTouchHelper.VocabularyTouchHelperListener,
        OnEditVocabularyListener {

    private AppCompatActivity parentActivity;
    private DrawerLayout drawer;
    private ViewModelProvider viewModelProvider;

    private LinearLayout seeAllLayout;

    private Toolbar toolbar;
    private MenuItem searchMenuItem;
    private Window window;

    private SeeAllViewModel seeAllViewModel;

    private VocaViewModel vocaViewModel;

    private Spinner sortSpinner;
    private static int sortState = 0;

    private TextView vocaSizeText;
    private RecyclerView vocaRecyclerView;

    private SearchView searchView;
    private boolean isSearchMode = false;

    private LinearLayout deleteLayout;
    private Button deleteVocabularyButton;
    private Button deleteCancelButton;

    private VocaRecyclerViewAdapter vocaRecyclerViewAdapter;

    private Handler handler = new Handler();
    private final int loadAdapterDelay = 10;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        parentActivity = (AppCompatActivity) getActivity();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModelProvider = new ViewModelProvider(this);
        seeAllViewModel = viewModelProvider.get(SeeAllViewModel.class);
        vocaViewModel = viewModelProvider.get(VocaViewModel.class);

        View root = inflater.inflate(R.layout.fragment_see_all, container, false);
        seeAllLayout = root.findViewById(R.id.layout_see_all);

        toolbar = parentActivity.findViewById(R.id.toolbar);
        drawer = parentActivity.findViewById(R.id.drawer_layout);
        setHasOptionsMenu(true);

        vocaSizeText = root.findViewById(R.id.text_voca_number);
        vocaRecyclerView = root.findViewById(R.id.recycler_view_voca);

        deleteLayout = root.findViewById(R.id.layout_delete);
        deleteVocabularyButton = root.findViewById(R.id.button_delete_vocabulary);
        deleteVocabularyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Integer> selectedItems = vocaRecyclerViewAdapter.getSelectedItems();

                AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
                builder.setTitle("삭제");
                builder.setMessage(selectedItems.size() + "개의 단어를 삭제합니다.");
                builder.setIcon(android.R.drawable.ic_dialog_alert);

                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vocaRecyclerViewAdapter.deleteVocabulary();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        deleteCancelButton = root.findViewById(R.id.add_button_cancel);
        deleteCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vocaRecyclerViewAdapter.disableDeleteMode();
            }
        });

        showVocaSize();

        sortSpinner = root.findViewById(R.id.spinner_sort);
        showSpinner();


        final LoadAdapterTask task = new LoadAdapterTask();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                task.execute();
            }
        }, loadAdapterDelay);

        vocaRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        vocaRecyclerView.addItemDecoration(new DividerItemDecoration(vocaRecyclerView.getContext(), new LinearLayoutManager(parentActivity).getOrientation()));

        ItemTouchHelper.SimpleCallback callback = new VocabularyTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(callback).attachToRecyclerView(vocaRecyclerView);
        return root;
    }

    private void showVocaSize() {
        final LiveData<Integer> vocaSize = vocaViewModel.getVocabularyCount();
        vocaSize.observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                vocaSizeText.setText(Integer.toString(integer));
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        Log.d("HSK APP", "onCreateOptionsMenu() in SeeAllFragment");
        searchMenuItem = menu.findItem(R.id.action_search);
        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Called when SearchView is expanding
                animateSearchToolbar(1, true, true);
                vocaRecyclerViewAdapter.enableSearchMode();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Called when SearchView is collapsing
                if (searchMenuItem.isActionViewExpanded()) {
                    animateSearchToolbar(1, false, false);
                    vocaRecyclerViewAdapter.disableSearchMode();
                    vocaSizeText.setText(Integer.toString(vocaRecyclerViewAdapter.getItemCount()));
                }
                return true;
            }
        });

        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchVocabulary(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchVocabulary(String query) {
        vocaRecyclerViewAdapter.searchVocabulary(query);
        vocaRecyclerViewAdapter.getCurrentVocabulary().observe(getViewLifecycleOwner(), new Observer<List<Vocabulary>>() {
            @Override
            public void onChanged(List<Vocabulary> vocabularies) {
                if (isSearchMode) {
                    vocaSizeText.setText(Integer.toString(vocabularies.size()));
                }
            }
        });
    }

    /* for drawing search animations on toolbar */
    public void animateSearchToolbar(int numberOfMenuIcon, boolean containsOverflow, boolean show) {
        toolbar.setBackgroundColor(ContextCompat.getColor(parentActivity, R.color.design_default_color_primary));
        if (window == null) {
            window = parentActivity.getWindow();
        }
        // set status bar color
        // window.setStatusBarColor(ContextCompat.getColor(parentActivity, android.R.color.white));

        int animationDuration = 500;
        if (show) {
            isSearchMode = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        isRtl(getResources()) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, 0.0f, (float) width);
                createCircularReveal.setDuration(animationDuration);
                createCircularReveal.start();
            } else {
                TranslateAnimation translateAnimation = new TranslateAnimation(0.0f, 0.0f, (float) (-toolbar.getHeight()), 0.0f);
                translateAnimation.setDuration(animationDuration);
                toolbar.clearAnimation();
                toolbar.startAnimation(translateAnimation);
            }
        } else {
            isSearchMode = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int width = toolbar.getWidth() -
                        (containsOverflow ? getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material) : 0) -
                        ((getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) * numberOfMenuIcon) / 2);
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(toolbar,
                        isRtl(getResources()) ? toolbar.getWidth() - width : width, toolbar.getHeight() / 2, (float) width, 0.0f);
                createCircularReveal.setDuration(animationDuration);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        toolbar.setBackgroundColor(getThemeColor(parentActivity, R.attr.colorPrimary));
                        //window.setStatusBarColor(getThemeColor(parentActivity, R.attr.colorPrimaryDark));
                    }
                });
                createCircularReveal.start();
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                Animation translateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-toolbar.getHeight()));
                AnimationSet animationSet = new AnimationSet(true);
                animationSet.addAnimation(alphaAnimation);
                animationSet.addAnimation(translateAnimation);
                animationSet.setDuration(animationDuration);
                animationSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        toolbar.setBackgroundColor(getThemeColor(parentActivity, R.attr.colorPrimary));
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                toolbar.startAnimation(animationSet);
            }
            //window.setStatusBarColor(getThemeColor(parentActivity, R.attr.colorPrimaryDark));
        }
    }

    private boolean isRtl(Resources resources) {
        return resources.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    private static int getThemeColor(Context context, int id) {
        Resources.Theme theme = context.getTheme();
        TypedArray a = theme.obtainStyledAttributes(new int[]{id});
        int result = a.getColor(0, 0);
        a.recycle();
        return result;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && vocaRecyclerViewAdapter.isDeleteMode() &&
                        !drawer.isDrawerOpen(GravityCompat.START)) {
                    vocaRecyclerViewAdapter.disableDeleteMode();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, final int position) {
        if (viewHolder instanceof VocaRecyclerViewAdapter.VocaViewHolder) {
            final Vocabulary deletedVocabulary = (Vocabulary) vocaRecyclerViewAdapter.getItem(position);
            String eng = deletedVocabulary.eng;

            Log.d("HSK APP", "pos: " + position);

            vocaRecyclerViewAdapter.removeItem(position);
            vocaSizeText.setText(Integer.toString(vocaRecyclerViewAdapter.getItemCount()));

            Snackbar snackbar = Snackbar.make(seeAllLayout, eng + "이(가) 삭제되었습니다.", Snackbar.LENGTH_LONG);
            snackbar.setAction("실행 취소", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vocaRecyclerViewAdapter.restoreItem(deletedVocabulary, position);
                    vocaSizeText.setText(Integer.toString(vocaRecyclerViewAdapter.getItemCount()));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private void showSpinner() {
        String[] items = getResources().getStringArray(R.array.sort_method);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(parentActivity.getApplicationContext(), R.layout.spinner_item, items);
        sortSpinner.setAdapter(sortAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (sortState == position) {
                    return;
                }
                sortState = position;
                vocaRecyclerViewAdapter.sortItems(sortState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sortState = 0;
                vocaRecyclerViewAdapter.sortItems(sortState);
            }
        });
        sortSpinner.setPrompt("정렬 방법");
        sortSpinner.setSelection(sortState);
        sortSpinner.setGravity(Gravity.CENTER);
    }


    /* Implementations of interfaces */
    @Override
    public void onDeleteModeEnabled() {
        deleteLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeleteModeDisabled() {
        vocaRecyclerViewAdapter.clearSelectedState();
        deleteLayout.setVisibility(View.GONE);
    }

    @Override
    public void editVocabulary(int position, Vocabulary vocabulary) {
        Intent intent = new Intent(parentActivity.getApplicationContext(), EditVocaActivity.class);
        intent.putExtra(Constants.POSITION, position);
        intent.putExtra(Constants.EDIT_VOCA, vocabulary);
        startActivityForResult(intent, Constants.CALL_EDIT_VOCA_ACTIVITY);
    }

    @Override
    public void showVocabularyOnNotification(Vocabulary vocabulary) {
        Intent intent = new Intent(getContext(), ShowNotificationService.class);
        intent.putExtra(ShowNotificationService.SHOW_VOCA, vocabulary);
        parentActivity.startService(intent);

        Snackbar.make(seeAllLayout, "알림에 보임: " + vocabulary.eng, Snackbar.LENGTH_LONG).show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("HSK APP", "requestCode = " + requestCode + ", resultCode = " + resultCode);
        if (requestCode == Constants.CALL_EDIT_VOCA_ACTIVITY && resultCode == Constants.EDIT_NEW_VOCA_OK) {
            vocaRecyclerViewAdapter.notifyItemsChanged();
        } else if (requestCode == Constants.CALL_ADD_VOCA_ACTIVITY && resultCode == Constants.ADD_NEW_VOCA_OK) {
            return;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void setAdapter() {
        vocaRecyclerViewAdapter.setOnEditVocabularyListener(this);
        vocaRecyclerViewAdapter.setOnDeleteModeListener(this);
        vocaRecyclerViewAdapter.setShowVocaOnNotificationListener(this);
        vocaRecyclerViewAdapter.setVocaClickListener(new VocaRecyclerViewAdapter.OnVocaClickListener() {
            @Override
            public void onVocaClick(VocaRecyclerViewAdapter.VocaViewHolder holder, View view, int position) {
                Log.d("HSK APP", position + " clicked.");
                if (vocaRecyclerViewAdapter.isDeleteMode()) {
                    vocaRecyclerViewAdapter.switchSelectedState(position);
                } else {
                    // do nothing
                }
            }

            @Override
            public boolean onVocaLongClick(VocaRecyclerViewAdapter.VocaViewHolder holder, View view, int position) {
                return false;
            }
        });
        vocaRecyclerViewAdapter.getCurrentVocabulary().observeForever(new Observer<List<Vocabulary>>() {
            @Override
            public void onChanged(List<Vocabulary> vocabularies) {
                Log.d("HSK APP", "setAdapter() -> onChanged()");
                showVocaSize();
                vocaRecyclerViewAdapter.observe();
            }
        });
        vocaRecyclerView.setAdapter(vocaRecyclerViewAdapter);
    }

    private class LoadAdapterTask extends AsyncTask<Void, Void, Void> {

        private SeeAllFragment thisFragment;

        @Override
        protected Void doInBackground(Void... voids) {
            // TODO: 로딩화면 표시?
            thisFragment = SeeAllFragment.this;
            vocaRecyclerViewAdapter = VocaRecyclerViewAdapter.getInstance(parentActivity);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            setAdapter();
        }
    }
}