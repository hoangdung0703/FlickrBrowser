package vn.edu.usth.flickrbrowser;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.usth.flickrbrowser.core.util.ThemeUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme before setting content view
        ThemeUtil.initTheme(this);
        
        super.onCreate(savedInstanceState);
        // Sử dụng layout activity_main có chứa NavHostFragment
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavView = findViewById(R.id.bottom_navigation);

        // Tìm NavController từ "bản đồ" nav_graph
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Kết nối thanh điều hướng dưới cùng với NavController
            // Tự động xử lý việc chuyển fragment khi bấm nút
            NavigationUI.setupWithNavController(bottomNavView, navController);

            // Thêm một listener để theo dõi màn hình nào đang hiển thị
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                // Kiểm tra ID của màn hình đích
                if (destination.getId() == R.id.signInFragment || destination.getId() == R.id.signUpFragment) {
                    // Nếu là màn hình đăng nhập hoặc đăng ký, hãy ẩn thanh điều hướng đi
                    bottomNavView.setVisibility(View.GONE);
                } else {
                    // Ngược lại, hiện nó ra ở các màn hình khác
                    bottomNavView.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
