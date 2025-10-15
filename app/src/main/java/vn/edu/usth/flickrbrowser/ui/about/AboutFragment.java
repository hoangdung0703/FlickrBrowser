package vn.edu.usth.flickrbrowser.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.usth.flickrbrowser.BuildConfig;
import vn.edu.usth.flickrbrowser.R;

public class AboutFragment extends Fragment {

    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf,
                             @Nullable ViewGroup parent,
                             @Nullable Bundle b) {
        View v = inf.inflate(R.layout.fragment_about, parent, false);

        // Version
        Chip chip = v.findViewById(R.id.chipVersion);
        chip.setText(getString(R.string.version_fmt, BuildConfig.VERSION_NAME));

        // Welcome + email
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String email = (user != null && user.getEmail() != null) ? user.getEmail()
                : getString(R.string.guest);
        TextView tvWelcome = v.findViewById(R.id.tvWelcome);
        tvWelcome.setText(getString(R.string.welcome_fmt, email));

        // Logout
        v.findViewById(R.id.btnLogout).setOnClickListener(btn -> {
            try {
                mAuth.signOut();
                Toast.makeText(requireContext(), R.string.logged_out, Toast.LENGTH_SHORT).show();
                goToLogin();
            } catch (Exception e) {
                Toast.makeText(requireContext(), R.string.logout_failed, Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }

    private void goToLogin() {
        // Ẩn bottom nav trước khi về login
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setVisibility(View.GONE);

        // Điều hướng về SignIn bằng action đã có popUpTo của nav_graph
        NavController nc = NavHostFragment.findNavController(this);
        nc.navigate(R.id.action_about_to_signIn);
    }
}
