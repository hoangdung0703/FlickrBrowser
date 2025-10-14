package vn.edu.usth.flickrbrowser.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import vn.edu.usth.flickrbrowser.R;

public class SignInFragment extends Fragment {

    // Khai báo các thành phần giao diện
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnSignIn;
    private TextView tvSignUp;

    // Khai báo đối tượng Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ View từ layout
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSignIn = view.findViewById(R.id.btn_sign_in);
        tvSignUp = view.findViewById(R.id.tv_sign_up);

        // Xử lý sự kiện click cho nút Sign In
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Xử lý sự kiện click cho chữ Sign Up để chuyển trang
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sử dụng NavController để chuyển sang SignUpFragment
                NavHostFragment.findNavController(SignInFragment.this)
                        .navigate(R.id.action_signInFragment_to_signUpFragment);
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Kiểm tra xem email có trống không
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getContext(), "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem password có trống không
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng nhập người dùng bằng Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            Toast.makeText(getContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                            // Chuyển đến màn hình chính (ExploreFragment)
                            NavHostFragment.findNavController(SignInFragment.this)
                                    .navigate(R.id.action_signInFragment_to_main_nav );
                        } else {
                            // Đăng nhập thất bại
                            Toast.makeText(getContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

