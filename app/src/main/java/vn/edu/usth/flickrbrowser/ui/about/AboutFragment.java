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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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

        // Version chip
        Chip chip = v.findViewById(R.id.chipVersion);
        chip.setText(getString(R.string.version_fmt, BuildConfig.VERSION_NAME));

        // Welcome + email
        TextView tvWelcome = v.findViewById(R.id.tvWelcome);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String email = (user != null && user.getEmail() != null)
                ? user.getEmail()
                : getString(R.string.guest);
        tvWelcome.setText(getString(R.string.welcome_fmt, email));

        // Logout
        View btnLogout = v.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(vw -> {
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

    /** Điều hướng về màn hình đăng nhập (ẩn bottom nav + navigate bằng global action). */
    private void goToLogin() {
        // 1) Ẩn bottom nav để màn login sạch
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setVisibility(View.GONE);

        // 2) Dùng NavController của Activity (NavHostFragment) + global action
        try {
            NavController navController =
                    Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_global_go_to_signIn);
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Navigation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
