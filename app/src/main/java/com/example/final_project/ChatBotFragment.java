package com.example.final_project;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
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
    private Button sendButton, cleanButton;
    private ApiService apiService;
    private List<Message> messageList = new ArrayList<>();
    private boolean isFragmentAlive = true;

    public ChatBotFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_bot, container, false);

        messageContainer = view.findViewById(R.id.message_container);
        scrollView = view.findViewById(R.id.scroll_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        cleanButton = view.findViewById(R.id.clean_button);

        Retrofit retrofit = RetrofitClient.getClient("");
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

                if (!isValidTopic(message)) {
                    displayMessage("🚫 <b>NGOÀI PHẠM VI PHỤC VỤ!</b> Hãy hỏi về <i>món ăn, công thức nấu ăn, sức khỏe</i>!", false);
                    return;
                }

                displayMessage("⏳ Đang xử lý...", false);
                callGeminiApi(message);
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

    private void callGeminiApi(String userMessage) {
        GeminiRequest request = new GeminiRequest(userMessage);
        Call<GeminiResponse> call = apiService.getGeminiResponse(request, BuildConfig.GOOGLE_API_KEY);

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (!isFragmentAlive) return;

                removeProcessingMessage(); // XÓA "⏳ Đang xử lý..." ngay khi API phản hồi

                if (response.isSuccessful() && response.body() != null) {
                    String botReply = response.body().getResponse();
                    if (botReply != null && !botReply.isEmpty()) {
                        displayMessage(botReply, false);
                    } else {
                        displayMessage("⚠ AI không phản hồi. Vui lòng thử lại.", false);
                    }
                } else {
                    displayMessage("❌ Lỗi API: " + response.code(), false);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
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

        // LayoutParams cho tin nhắn
        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        messageParams.setMargins(0, 0, 0, 24); // Khoảng cách giữa các tin nhắn

        // Layout chứa tin nhắn để căn chỉnh
        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        if (isUser) {
            messageTextView.setBackgroundResource(R.drawable.user_message_background);
            messageParams.gravity = Gravity.END; // Đẩy user về bên phải
            messageLayout.setGravity(Gravity.END);
        } else {
            messageTextView.setBackgroundResource(R.drawable.bot_message_background);
            messageParams.gravity = Gravity.START; // Đẩy bot về bên trái
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

    private void saveMessageHistory() {
        if (getActivity() == null) return;
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        List<Message> lastMessages = messageList.size() > 50 ? messageList.subList(messageList.size() - 50, messageList.size()) : messageList;
        editor.putString("message_history", gson.toJson(lastMessages));
        editor.apply();
    }

    private void loadMessageHistory() {
        if (getActivity() == null) return;
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
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

    private boolean isValidTopic(String message) {
        // Danh sách từ khóa tiếng Việt có dấu
        String[] vietnameseKeywords = {"ăn", "uống", "món", "công thức", "nấu", "dinh dưỡng", "calo", "sức khỏe", "đồ ăn", "ẩm thực", "thực đơn"};

        // Danh sách từ khóa tiếng Việt không dấu (chuẩn hóa)
        String[] vietnameseNoAccentKeywords = {"an", "uong", "mon", "cong thuc", "nau", "dinh duong", "calo", "suc khoe", "do an", "am thuc", "thuc don"};

        // Danh sách từ khóa tiếng Anh
        String[] englishKeywords = {"eat", "drink", "food", "recipe", "cook", "nutrition", "calories", "health", "menu", "cuisine"};

        // Chuẩn hóa văn bản nhập vào (chuyển về chữ thường, loại bỏ dấu tiếng Việt)
        String normalizedMessage = removeVietnameseAccents(message.toLowerCase());

        // Kiểm tra từ khóa tiếng Việt (có dấu)
        for (String keyword : vietnameseKeywords) {
            if (message.toLowerCase().contains(keyword)) return true;
        }

        // Kiểm tra từ khóa tiếng Việt không dấu
        for (String keyword : vietnameseNoAccentKeywords) {
            if (normalizedMessage.contains(keyword)) return true;
        }

        // Kiểm tra từ khóa tiếng Anh
        for (String keyword : englishKeywords) {
            if (normalizedMessage.contains(keyword)) return true;
        }

        return false;
    }

    // Hàm loại bỏ dấu tiếng Việt
    private String removeVietnameseAccents(String input) {
        input = input.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        input = input.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        input = input.replaceAll("[ìíịỉĩ]", "i");
        input = input.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        input = input.replaceAll("[ùúụủũưừứựửữ]", "u");
        input = input.replaceAll("[ỳýỵỷỹ]", "y");
        input = input.replaceAll("[đ]", "d");

        input = input.replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A");
        input = input.replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E");
        input = input.replaceAll("[ÌÍỊỈĨ]", "I");
        input = input.replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O");
        input = input.replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U");
        input = input.replaceAll("[ỲÝỴỶỸ]", "Y");
        input = input.replaceAll("[Đ]", "D");

        return input;
    }


    private String formatBotResponse(String message) {
        // Xóa dấu sao `*`
        message = message.replaceAll("\\*", "");

        // Chuyển xuống dòng thành HTML `<br>`
        message = message.replace("\n", "<br>");

        // Chuyển danh sách gạch đầu dòng `-`, `•`, `+` thành danh sách HTML
        message = message.replaceAll("(?m)^[\\-•+]\\s*(.*)", "<li>$1</li>");
        message = message.replaceAll("(?s)(<li>.*?</li>)", "<ul>$1</ul>"); // Bao danh sách vào `<ul>`

        return message;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentAlive = false;
    }
}
