package com.gamerzone.app.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.gamerzone.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    DatabaseReference isAdminReference;
    NavController navController;
    BottomNavigationView bottomNav;
    boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Always switch to Dark mode to use Dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);

        // Register a valueEventListener to get "isAdmin" indication and inflate the bottom navigation menu accordingly
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            registerAdminValueEventListener();
        } else {
            FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() != null) {
                    registerAdminValueEventListener();
                } else {
                    // Reset bottom nav to default to support case of admin logout
                    setBottomNavMenu(false);
                }
            });
        }

        bottomNav = findViewById(R.id.bottom_nav);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        assert navHostFragment != null;
        navController = navHostFragment.getNavController();

        // Set non-admin bottom navigation as default, until the indication from Firebase is received
        setBottomNavMenu(false);

        // Listen to Destination changes and shows the bottom navigation only where it's needed
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if ((destination.getId() == R.id.gamesFragment ||
                    destination.getId() == R.id.favoritesFragment ||
                    destination.getId() == R.id.adminPanelFragment)) {
                bottomNav.setVisibility(View.VISIBLE);
            } else {
                bottomNav.setVisibility(View.GONE);
            }
        });
    }

    private void registerAdminValueEventListener() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            isAdminReference = FirebaseDatabase.getInstance()
                    .getReference("users/" +
                            FirebaseAuth.getInstance().getCurrentUser().getUid() + "/isAdmin");
            isAdminReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    isAdmin = Boolean.TRUE.equals(dataSnapshot.getValue(Boolean.class));

                    // Update the bottom navigation menu according to the indication from Firebase
                    if (isAdmin) {
                        setBottomNavMenu(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setBottomNavMenu(boolean isAdmin) {
        bottomNav.getMenu().clear();
        if (isAdmin) {
            bottomNav.inflateMenu(R.menu.menu_bottom_navigation_admin);
        } else {
            bottomNav.inflateMenu(R.menu.menu_bottom_navigation);
        }

        NavigationUI.setupWithNavController(bottomNav, navController);
    }
}
