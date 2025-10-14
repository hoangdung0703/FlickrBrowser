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

public class SignUpFragment extends Fragment {

    // Khai báo các thành phần giao diện
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnSignUp;
    private TextView tvSignIn;

    // Khai báo đối tượng Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ View từ layout
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnSignUp = view.findViewById(R.id.btn_sign_up);
        tvSignIn = view.findViewById(R.id.tv_sign_in);

        // Xử lý sự kiện click cho nút Sign Up
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Xử lý sự kiện click cho chữ Sign In để quay lại trang đăng nhập
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sử dụng NavController để quay lại SignInFragment
                NavHostFragment.findNavController(SignUpFragment.this).popBackStack();
            }
        });
    }

    private void registerUser() {
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

        // Tạo người dùng mới bằng Firebase
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Đăng ký thành công
                            Toast.makeText(getContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                            // Chuyển đến màn hình chính (ExploreFragment)
                            NavHostFragment.findNavController(SignUpFragment.this)
                                    .navigate(R.id.action_signUpFragment_to_main_nav);
                        } else {
                            // Đăng ký thất bại
                            Toast.makeText(getContext(), "Authentication Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

