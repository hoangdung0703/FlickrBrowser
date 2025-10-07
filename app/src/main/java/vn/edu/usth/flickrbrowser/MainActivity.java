package vn.edu.usth.flickrbrowser;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import vn.edu.usth.flickrbrowser.ui.explore.ExploreFragment;
import vn.edu.usth.flickrbrowser.ui.favorites.FavoritesFragment;
import vn.edu.usth.flickrbrowser.ui.search.SearchFragment;
import vn.edu.usth.flickrbrowser.ui.about.AboutFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    // Cache fragments để tránh tạo lại
    private SearchFragment searchFragment;
    private ExploreFragment exploreFragment;
    private FavoritesFragment favoritesFragment;
    private AboutFragment aboutFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Khởi tạo fragments
        searchFragment = new SearchFragment();
        exploreFragment = new ExploreFragment();
        favoritesFragment = new FavoritesFragment();
        aboutFragment = new AboutFragment();

        // Lắng nghe chọn tab bottom nav
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = searchFragment;
            } else if (item.getItemId() == R.id.navigation_explore) {
                selectedFragment = exploreFragment;
            } else if (item.getItemId() == R.id.navigation_favorites) {
                selectedFragment = favoritesFragment;
            } else if (item.getItemId() == R.id.navigation_about) {
                selectedFragment = aboutFragment;
            }

            if (selectedFragment != null && selectedFragment != currentFragment) {
                showFragment(selectedFragment);
                currentFragment = selectedFragment;
                return true;
            }
            return false;
        });

        // Tab mặc định khi mở app
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    private void showFragment(Fragment fragment) {
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Hide tất cả fragment trước
        if (searchFragment.isAdded()) transaction.hide(searchFragment);
        if (exploreFragment.isAdded()) transaction.hide(exploreFragment);
        if (favoritesFragment.isAdded()) transaction.hide(favoritesFragment);
        if (aboutFragment.isAdded()) transaction.hide(aboutFragment);

        // Show hoặc add fragment mới
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        }

        transaction.commit();
    }
}
