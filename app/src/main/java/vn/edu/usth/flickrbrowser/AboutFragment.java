package vn.edu.usth.flickrbrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hiển thị version app (dùng BuildConfig)
        TextView tvVersion = view.findViewById(R.id.tvAppVersion);
        tvVersion.setText("App version: " + BuildConfig.VERSION_NAME);

        // Click mở README link
        TextView tvReadme = view.findViewById(R.id.tvReadmeLink);
        tvReadme.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/yourname/yourrepo#readme"));
            startActivity(intent);
        });
    }
}
