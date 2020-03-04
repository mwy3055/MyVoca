package hsk.practice.myvoca.ui.goblog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import hsk.practice.myvoca.R;


public class GoBlogFragment extends Fragment {

    private GoBlogViewModel goBlogViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        goBlogViewModel = new ViewModelProvider(this).get(GoBlogViewModel.class);
        View root = inflater.inflate(R.layout.fragment_go_blog, container, false);
        final TextView textView = root.findViewById(R.id.text_send);
        goBlogViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        String url = "https://thinking-face.tistory.com/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);

        return root;
    }
}