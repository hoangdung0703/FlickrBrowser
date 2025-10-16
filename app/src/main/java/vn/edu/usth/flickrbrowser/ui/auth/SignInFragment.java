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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        // Ẩn BottomNavigation khi đang ở màn Login
        BottomNavigationView bottom = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottom != null) bottom.setVisibility(View.GONE);

        mAuth       = FirebaseAuth.getInstance();
        etEmail     = v.findViewById(R.id.et_email);
        etPassword  = v.findViewById(R.id.et_password);
        btnSignIn   = v.findViewById(R.id.btn_sign_in);
        tvSignUp    = v.findViewById(R.id.tv_sign_up);

        btnSignIn.setOnClickListener(view -> loginUser());

        etPassword.setOnEditorActionListener((tv, actionId, ev) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginUser();
                return true;
            }
            return false;
        });

        tvSignUp.setOnClickListener(view ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_signInFragment_to_signUpFragment)
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // Nếu chạy thẳng SignIn khi user đã có session (trường hợp hiếm),
        // cứ cho vào main để đồng bộ với AuthFragment.
        if (mAuth.getCurrentUser() != null) {
            goToMain();
        }
    }

    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnSignIn.setAlpha(loading ? 0.6f : 1f);
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String pass  = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean ok = true;
        if (TextUtils.isEmpty(email)) { etEmail.setError(getString(R.string.error_email_required)); ok = false; }
        else etEmail.setError(null);

        if (TextUtils.isEmpty(pass))  { etPassword.setError(getString(R.string.error_password_required)); ok = false; }
        else etPassword.setError(null);

        if (!ok) {
            Toast.makeText(requireContext(), R.string.error_fix_inputs, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        mAuth.signInWithEmailAndPassword(email, pass)
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

    /** Điều hướng vào app chính (Home mới) đúng như nav_graph. */
    private void goToMain() {
        try {
            // Hiện lại bottom nav
            BottomNavigationView bottom = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottom != null) bottom.setVisibility(View.VISIBLE);

            // Điều hướng theo action đã có popUpTo trong nav_graph
            NavController nc = NavHostFragment.findNavController(this);
            if (nc.getCurrentDestination() != null &&
                    nc.getCurrentDestination().getId() == R.id.signInFragment) {
                nc.navigate(R.id.action_signInFragment_to_main_nav);
            } else {
                // fallback nếu đang không đứng ở signInFragment
                nc.navigate(R.id.navigation_home_new);
            }
        } catch (Exception e) {
            android.widget.Toast.makeText(requireContext(),
                    "Navigation error: " + e.getMessage(),
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
