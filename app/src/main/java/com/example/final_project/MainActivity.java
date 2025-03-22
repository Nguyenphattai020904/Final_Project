package com.example.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.final_project.Fragments.CartFragment;
import com.example.final_project.Fragments.ChatBotFragment;
import com.example.final_project.Fragments.DiscountFragment;
import com.example.final_project.Fragments.HomeFragment;
import com.example.final_project.Fragments.ProfileFragment;
import com.example.final_project.Fragments.QRFragment;
import com.example.final_project.Log.LogInActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private BottomNavigationView bottomNavigationView;
    private float dX, dY;
    private int lastAction;
    private BadgeDrawable cartBadge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isLoggedIn) {
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
            finish();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_discount) {
                selectedFragment = new DiscountFragment();
            } else if (item.getItemId() == R.id.nav_qr) {
                selectedFragment = new QRFragment();
            } else if (item.getItemId() == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment, selectedFragment.getClass().getSimpleName())
                        .commit();
            }
            return true;
        });

        // Khởi tạo badge cho mục Cart
        cartBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_cart);
        cartBadge.setVisible(false);
        cartBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        cartBadge.setBadgeTextColor(getResources().getColor(android.R.color.white));

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        ImageView chatBotIcon = findViewById(R.id.chat_bot_icon);
        chatBotIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        view.setY(event.getRawY() + dY);
                        view.setX(event.getRawX() + dX);
                        lastAction = MotionEvent.ACTION_MOVE;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (lastAction == MotionEvent.ACTION_DOWN) {
                            showChatBot();
                        }
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    public void showChatBot() {
        ChatBotFragment chatBotFragment = new ChatBotFragment();
        chatBotFragment.show(getSupportFragmentManager(), "chat_bot");
    }

    public void updateCartBadge(int itemCount) {
        if (itemCount > 0) {
            cartBadge.setVisible(true);
            cartBadge.setNumber(itemCount);
        } else {
            cartBadge.setVisible(false);
        }
    }
}