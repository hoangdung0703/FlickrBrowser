package vn.edu.usth.flickrbrowser.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import vn.edu.usth.flickrbrowser.R;

public class SignInFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private Button btnSignIn;
    private TextView tvSignUp;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ẩn BottomNavigation khi ở màn đăng nhập
        BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
        if (nav != null) nav.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        etEmail    = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSignIn  = view.findViewById(R.id.btn_sign_in);
        tvSignUp   = view.findViewById(R.id.tv_sign_up);

        btnSignIn.setOnClickListener(v -> loginUser());

        // Nhấn Done trên password cũng login
        etPassword.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginUser();
                return true;
            }
            return false;
        });

        // Đi tới màn Sign Up
        tvSignUp.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_signInFragment_to_signUpFragment)
        );
    }


    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnSignIn.setAlpha(loading ? 0.6f : 1f);
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean ok = true;
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_email_required));
            ok = false;
        } else etEmail.setError(null);

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_password_required));
            ok = false;
        } else etPassword.setError(null);

        if (!ok) {
            Toast.makeText(requireContext(), R.string.error_fix_inputs, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show();
                        goToMain();
                    } else {
                        Toast.makeText(requireContext(), R.string.login_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Điều hướng vào app chính (Home) và hiện BottomNav. */
    private void goToMain() {
        try {
            // Hiện BottomNavigation + chọn tab Home
            BottomNavigationView nav = requireActivity().findViewById(R.id.bottom_navigation);
            if (nav != null) {
                nav.setVisibility(View.VISIBLE);
                nav.setSelectedItemId(R.id.navigation_home);
            }

            // Điều hướng sang Home bằng action trong nav-graph (đã popUpTo=nav_graph)
            NavController nc = NavHostFragment.findNavController(this);
            if (nc.getCurrentDestination() != null
                    && nc.getCurrentDestination().getId() == R.id.signInFragment) {
                nc.navigate(R.id.action_signInFragment_to_main_nav); // tới navigation_home
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
