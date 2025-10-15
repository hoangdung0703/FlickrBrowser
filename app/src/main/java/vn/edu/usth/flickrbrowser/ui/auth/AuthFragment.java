package vn.edu.usth.flickrbrowser.ui.auth;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vn.edu.usth.flickrbrowser.R;

public class AuthFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Trì hoãn 1 giây để kiểm tra, tạo cảm giác app đang load
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Kiểm tra fragment còn tồn tại không trước khi điều hướng
            if (getContext() == null) return;

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Đã đăng nhập -> vào màn hình chính
                NavHostFragment.findNavController(AuthFragment.this)
                        .navigate(R.id.action_authFragment_to_main_nav);
            } else {
                // Chưa đăng nhập -> vào màn hình đăng nhập
                NavHostFragment.findNavController(AuthFragment.this)
                        .navigate(R.id.action_authFragment_to_signInFragment);
            }
        }, 1000);
    }
}

