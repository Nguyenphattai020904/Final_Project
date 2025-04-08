package com.example.final_project;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import com.example.final_project.Fragments.CartFragment;
import com.example.final_project.Fragments.ChatBotFragment;
import com.example.final_project.Fragments.DiscountFragment;
import com.example.final_project.Fragments.HomeFragment;
import com.example.final_project.Fragments.PaymentFragment;
import com.example.final_project.Fragments.ProfileFragment;
import com.example.final_project.Log.LogInActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.badge.BadgeDrawable;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private BottomNavigationView bottomNavigationView;
    private float dX, dY, initialX, initialY; // Thêm initialX, initialY để xác định chạm hay di chuyển
    private BadgeDrawable cartBadge;
    private Dialog adDialog;
    private static final String CHANNEL_ID = "spin_channel";
    private static final int NOTIFICATION_ID = 1001;
    private BroadcastReceiver spinReceiver;
    private boolean isReceiverRegistered = false;
    private static final float TOUCH_THRESHOLD = 10.0f; // Ngưỡng khoảng cách để phân biệt chạm và di chuyển

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

        // Khởi tạo spinReceiver
        spinReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(context, "Bạn đã nhận được 1 lượt quay", Toast.LENGTH_SHORT).show();

                Intent notificationIntent = new Intent(context, LuckyWheel.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.notifications)
                        .setContentTitle("Lượt quay mới!")
                        .setContentText("Bạn đã nhận được 1 lượt quay. Hãy quay ngay!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID, builder.build());
            }
        };

        // Đăng ký spinReceiver với cờ phù hợp
        IntentFilter filter = new IntentFilter("com.example.final_project.SPIN_NOTIFICATION");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(spinReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(spinReceiver, filter);
        }
        isReceiverRegistered = true;

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.nav_discount) {
                selectedFragment = new DiscountFragment();
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

        cartBadge = bottomNavigationView.getOrCreateBadge(R.id.nav_cart);
        cartBadge.setVisible(false);
        cartBadge.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        cartBadge.setBadgeTextColor(getResources().getColor(android.R.color.white));

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            showAdDialog();
        }

        ImageView chatBotIcon = findViewById(R.id.chat_bot_icon);
        chatBotIcon.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Lưu vị trí ban đầu của ngón tay
                    initialX = event.getRawX();
                    initialY = event.getRawY();
                    dX = view.getX() - initialX;
                    dY = view.getY() - initialY;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Di chuyển biểu tượng
                    view.setX(event.getRawX() + dX);
                    view.setY(event.getRawY() + dY);
                    break;

                case MotionEvent.ACTION_UP:
                    // Tính khoảng cách di chuyển
                    float deltaX = Math.abs(event.getRawX() - initialX);
                    float deltaY = Math.abs(event.getRawY() - initialY);

                    // Nếu khoảng cách nhỏ hơn ngưỡng, coi như chạm để mở chatbot
                    if (deltaX < TOUCH_THRESHOLD && deltaY < TOUCH_THRESHOLD) {
                        showChatBot();
                    }
                    break;

                default:
                    return false;
            }
            return true;
        });

        createNotificationChannel();
        setupDailySpinCheck();
    }

    private void showAdDialog() {
        adDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        adDialog.setContentView(R.layout.ad_dialog_layout);
        adDialog.setCancelable(false);

        ImageView adImage = adDialog.findViewById(R.id.ad_image);
        ImageView closeButton = adDialog.findViewById(R.id.ad_close_button);

        adImage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LuckyWheel.class);
            startActivity(intent);
            adDialog.dismiss();
        });

        closeButton.setOnClickListener(v -> adDialog.dismiss());

        adDialog.show();
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null && "myapp".equals(intent.getData().getScheme())) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (fragment instanceof PaymentFragment) {
                ((PaymentFragment) fragment).checkPaymentStatus();
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Spin Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void setupDailySpinCheck() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent("com.example.final_project.SPIN_NOTIFICATION");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 3);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Chỉ hủy đăng ký nếu spinReceiver đã được đăng ký
        if (isReceiverRegistered) {
            unregisterReceiver(spinReceiver);
            isReceiverRegistered = false;
        }
        // Đảm bảo dialog được đóng khi Activity bị hủy
        if (adDialog != null && adDialog.isShowing()) {
            adDialog.dismiss();
        }
    }
}