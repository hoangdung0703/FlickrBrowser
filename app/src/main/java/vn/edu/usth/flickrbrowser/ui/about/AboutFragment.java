package vn.edu.usth.flickrbrowser.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;

import vn.edu.usth.flickrbrowser.BuildConfig;
import vn.edu.usth.flickrbrowser.R;

public class AboutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_about, parent, false);

        // Gán version app vào Chip
        Chip chip = v.findViewById(R.id.chipVersion);
        chip.setText("Version " + BuildConfig.VERSION_NAME);

        return v;
    }
}
