package com.example.final_project.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.final_project.API_Controls.ApiService;
import com.example.final_project.API_Controls.RetrofitClient;
import com.example.final_project.API_Reponse.ChatResponse;
import com.example.final_project.API_Requests.ChatRequest;
import com.example.final_project.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatBotFragment extends DialogFragment {
    private static final String TAG = "ChatBot";
    private LinearLayout messageContainer;
    private ScrollView scrollView;
    private EditText messageInput;
    private Button cleanButton;
    private ApiService apiService;
    private List<Message> messageList = new ArrayList<>();
    private ImageView sendButton;
    private boolean isFragmentAlive = true;
    private String userId;

    public ChatBotFragment() {}

    public static ChatBotFragment newInstance(String userId) {
        ChatBotFragment fragment = new ChatBotFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_bot, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString("userId", "");
        }

        messageContainer = view.findViewById(R.id.message_container);
        scrollView = view.findViewById(R.id.scroll_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        cleanButton = view.findViewById(R.id.clean_button);

        Retrofit retrofit = RetrofitClient.getClient();
        apiService = retrofit.create(ApiService.class);

        loadMessageHistory();

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                if (!isNetworkAvailable()) {
                    displayMessage("🚫 KHÔNG CÓ KẾT NỐI INTERNET", false);
                    return;
                }
                displayMessage(message, true);
                messageInput.setText("");

                displayMessage("⏳ Đang xử lý...", false);
                callChatApi(message);
            }
        });

        cleanButton.setOnClickListener(v -> {
            messageList.clear();
            messageContainer.removeAllViews();
            saveMessageHistory();
            Toast.makeText(getContext(), "✅ Đã xóa tất cả tin nhắn", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            params.width = (int) (displayMetrics.widthPixels * 0.9);
            params.height = (int) (displayMetrics.heightPixels * 0.7);
            getDialog().getWindow().setAttributes(params);
        }
    }

    private void callChatApi(String userMessage) {
        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty");
            removeProcessingMessage();
            displayMessage("🚫 Bạn cần đăng nhập trước.", false);
            return;
        }

        ChatRequest request = new ChatRequest(userId, userMessage);
        Call<ChatResponse> call = apiService.sendChatMessage("Bearer " + token, request);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (!isFragmentAlive) return;

                removeProcessingMessage();

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        String botReply = response.body().getMessage();
                        if (botReply != null && !botReply.isEmpty()) {
                            displayMessage(botReply, false);
                        } else {
                            displayMessage("⚠ AI không phản hồi. Vui lòng thử lại.", false);
                        }
                    } else {
                        displayMessage("⚠ " + response.body().getMessage(), false);
                    }
                } else {
                    if (response.code() == 400) {
                        displayMessage("🚫 <b>NGOÀI PHẠM VI PHỤC VỤ!</b> Hãy hỏi về <i>món ăn, công thức nấu ăn, sức khỏe</i>!", false);
                    } else {
                        displayMessage("❌ Lỗi API: " + response.code(), false);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                if (!isFragmentAlive) return;
                removeProcessingMessage();
                displayMessage("❌ Lỗi kết nối: " + t.getMessage(), false);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void displayMessage(String message, boolean isUser) {
        TextView messageTextView = new TextView(getContext());

        if (!isUser) {
            message = formatBotResponse(message);
        }

        Spanned formattedMessage = Html.fromHtml(message);
        messageTextView.setText(formattedMessage);
        messageTextView.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        messageParams.setMargins(0, 0, 0, 24);

        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        if (isUser) {
            messageTextView.setBackgroundResource(R.drawable.user_message_background);
            messageParams.gravity = Gravity.END;
            messageLayout.setGravity(Gravity.END);
        } else {
            messageTextView.setBackgroundResource(R.drawable.bot_message_background);
            messageParams.gravity = Gravity.START;
            messageLayout.setGravity(Gravity.START);
        }

        messageTextView.setLayoutParams(messageParams);
        messageLayout.addView(messageTextView);
        messageContainer.addView(messageLayout);

        messageList.add(new Message(message, isUser));
        saveMessageHistory();

        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void removeProcessingMessage() {
        if (!messageList.isEmpty() && "⏳ Đang xử lý...".equals(messageList.get(messageList.size() - 1).getText())) {
            messageList.remove(messageList.size() - 1);
            messageContainer.removeViewAt(messageContainer.getChildCount() - 1);
        }
    }

    private String getUserToken() {
        SharedPreferences sharedPreferences = getActivity().getApplicationContext()
                .getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("access_token", null);
        Log.d(TAG, "Retrieved token: " + token);
        return token;
    }

    private void saveMessageHistory() {
        if (getActivity() == null) return;
        SharedPreferences sharedPreferences = getActivity().getApplicationContext()
                .getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        List<Message> lastMessages = messageList.size() > 50 ? messageList.subList(messageList.size() - 50, messageList.size()) : messageList;
        editor.putString("message_history", gson.toJson(lastMessages));
        editor.apply();
    }

    private void loadMessageHistory() {
        if (getActivity() == null) return;
        SharedPreferences sharedPreferences = getActivity().getApplicationContext()
                .getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("message_history", null);
        Type type = new TypeToken<ArrayList<Message>>() {}.getType();
        List<Message> loadedMessages = gson.fromJson(json, type);
        if (loadedMessages != null) {
            messageList = new ArrayList<>(loadedMessages);
            messageContainer.removeAllViews();
            for (Message message : new ArrayList<>(messageList)) {
                displayMessage(message.getText(), message.isUser());
            }
        }
    }

    private String formatBotResponse(String message) {
        message = message.replaceAll("\\*", "");
        message = message.replace("\n", "<br>");
        message = message.replaceAll("(?m)^[\\-•+]\\s*(.*)", "<li>$1</li>");
        message = message.replaceAll("(?s)(<li>.*?</li>)", "<ul>$1</ul>");
        return message;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentAlive = false;
    }

    private static class Message {
        private final String text;
        private final boolean isUser;

        public Message(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }

        public String getText() {
            return text;
        }

        public boolean isUser() {
            return isUser;
        }
    }
}