package com.example.final_project.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.final_project.API_Reponse.MentionedProduct;
import com.example.final_project.API_Requests.ChatRequest;
import com.example.final_project.Products.Product;
import com.example.final_project.Products.ProductDetailFragment;
import com.example.final_project.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private ImageButton sendButton;
    private boolean isFragmentAlive = true;
    private String userId;
    private static final String PROCESSING_MESSAGE = "⏳ Đang xử lý...";

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
        view.setBackgroundResource(R.drawable.rounded_background);

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
                    displayMessage("🚫 KHÔNG CÓ KẾT NỐI INTERNET", false, null);
                    return;
                }
                new CheckNetworkStabilityTask(message).execute();
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

    private class CheckNetworkStabilityTask extends AsyncTask<Void, Void, Boolean> {
        private final String userMessage;

        CheckNetworkStabilityTask(String userMessage) {
            this.userMessage = userMessage;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return isNetworkStable();
        }

        @Override
        protected void onPostExecute(Boolean isStable) {
            if (!isFragmentAlive) return;

            displayMessage(userMessage, true, null);
            messageInput.setText("");
            // Xóa bất kỳ tin nhắn "Đang xử lý..." cũ trước khi thêm mới
            removeProcessingMessage();
            displayMessage(PROCESSING_MESSAGE, false, null);
            callChatApi(userMessage);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            params.width = (int) (displayMetrics.widthPixels * 0.98);
            params.height = (int) (displayMetrics.heightPixels * 0.85);
            getDialog().getWindow().setAttributes(params);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void callChatApi(String userMessage) {
        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is null or empty");
            removeProcessingMessage();
            displayMessage("🚫 Bạn cần đăng nhập trước.", false, null);
            return;
        }

        ChatRequest request = new ChatRequest(userId, userMessage);
        Call<ChatResponse> call = apiService.sendChatMessage("Bearer " + token, request);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (!isFragmentAlive) return;

                // Xóa "Đang xử lý..." trước khi xử lý phản hồi
                removeProcessingMessage();

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        String botReply = response.body().getMessage();
                        List<MentionedProduct> mentionedProducts = response.body().getMentionedProducts();
                        if (botReply != null && !botReply.isEmpty()) {
                            displayMessage(botReply, false, mentionedProducts);
                        } else {
                            displayMessage("⚠ AI không phản hồi. Vui lòng thử lại.", false, null);
                        }
                    } else {
                        String errorMessage = response.body().getMessage();
                        if (response.code() == 429) {
                            int retryAfter = response.body().getRetryAfter() != null ? response.body().getRetryAfter() : 10;
                            errorMessage = "⚠ Hệ thống đang bận. Đang thử lại sau " + retryAfter + " giây...";
                            displayMessage(errorMessage, false, null);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (isFragmentAlive) {
                                    // Xóa bất kỳ tin nhắn "Đang xử lý..." cũ trước khi thêm mới
                                    removeProcessingMessage();
                                    displayMessage(PROCESSING_MESSAGE, false, null);
                                    callChatApi(userMessage);
                                }
                            }, retryAfter * 1000L);
                        } else {
                            displayMessage(errorMessage, false, null);
                        }
                    }
                } else {
                    String errorMessage = "❌ Không nằm trong danh mục được hỗ trợ, vui lòng hỏi các vấn đề liên quan đến sản phẩm, dinh dưỡng,... ";
                    displayMessage(errorMessage, false, null);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                if (!isFragmentAlive) return;
                // Xóa "Đang xử lý..." trước khi hiển thị lỗi
                removeProcessingMessage();
                displayMessage("❌ Lỗi kết nối: " + t.getMessage(), false, null);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isNetworkStable() {
        try {
            URL url = new URL("http://172.16.74.100:3000/api/ping");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            boolean isStable = responseCode >= 200 && responseCode < 300;
            Log.d(TAG, "Network stability check (HTTP): " + (isStable ? "Stable" : "Unstable") + ", Response code: " + responseCode);
            return isStable;
        } catch (IOException e) {
            Log.e(TAG, "Network stability check failed: " + e.getMessage());
            return false;
        }
    }

    private void displayMessage(String message, boolean isUser, List<MentionedProduct> mentionedProducts) {
        LinearLayout messageLayout = new LinearLayout(getContext());
        messageLayout.setOrientation(LinearLayout.VERTICAL);
        messageLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView messageTextView = new TextView(getContext());
        // Chỉ định dạng HTML cho tin nhắn bot, trừ "Đang xử lý..."
        if (!isUser && !PROCESSING_MESSAGE.equals(message)) {
            message = formatBotResponse(message);
            Log.d(TAG, "Formatted bot response: " + message);
        }
        messageTextView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));
        messageTextView.setTextSize(16);
        messageTextView.setTextColor(getResources().getColor(android.R.color.black));
        messageTextView.setPadding(16, 8, 16, 8); // Đồng bộ padding
        // Giới hạn chiều rộng tối đa để tin nhắn bot và user đều nhau
        messageTextView.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.75)); // 75% chiều rộng màn hình

        LinearLayout.LayoutParams messageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // Đồng bộ margin để căn chỉnh đều
        messageParams.setMargins(8, 8, 8, 8);

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

        // Phần hiển thị sản phẩm (giữ nguyên)
        if (!isUser && mentionedProducts != null && !mentionedProducts.isEmpty()) {
            String normalizedMessage = message.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
            for (MentionedProduct product : mentionedProducts) {
                String normalizedProductName = product.getName().toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
                if (normalizedMessage.contains(normalizedProductName)) {
                    LinearLayout productLayout = new LinearLayout(getContext());
                    productLayout.setOrientation(LinearLayout.HORIZONTAL);
                    productLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    productLayout.setPadding(16, 8, 16, 8);
                    productLayout.setBackgroundResource(R.drawable.product_item_background);
                    productLayout.setElevation(4);

                    LinearLayout.LayoutParams productLayoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    productLayoutParams.setMargins(0, 0, 0, 16);
                    productLayout.setLayoutParams(productLayoutParams);

                    ImageView productImage = new ImageView(getContext());
                    LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(240, 240);
                    imageParams.setMargins(16, 16, 16, 0);
                    productImage.setLayoutParams(imageParams);
                    productImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    try {
                        Picasso.get().load(product.getImages()).placeholder(R.drawable.img).error(R.drawable.img).into(productImage);
                    } catch (Exception e) {
                        productImage.setImageResource(R.drawable.img);
                        Log.e(TAG, "Error loading image for product " + product.getName() + ": " + e.getMessage());
                    }

                    LinearLayout productInfoLayout = new LinearLayout(getContext());
                    productInfoLayout.setOrientation(LinearLayout.VERTICAL);
                    productInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1.0f
                    ));

                    TextView productNameTextView = new TextView(getContext());
                    productNameTextView.setText(product.getName());
                    productNameTextView.setTextSize(16);
                    productNameTextView.setTextColor(getResources().getColor(android.R.color.black));
                    productNameTextView.setTypeface(null, android.graphics.Typeface.BOLD);
                    productInfoLayout.addView(productNameTextView);

                    TextView productPriceTextView = new TextView(getContext());
                    productPriceTextView.setText(formatPrice(product.getPrice()));
                    productPriceTextView.setTextSize(14);
                    productPriceTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    productInfoLayout.addView(productPriceTextView);

                    Button detailButton = new Button(getContext());
                    detailButton.setText("Xem chi tiết");
                    detailButton.setTextSize(12);
                    detailButton.setBackgroundTintList(getResources().getColorStateList(R.color.green_smoke));
                    detailButton.setTextColor(getResources().getColor(android.R.color.white));
                    LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    buttonParams.setMargins(0, 8, 0, 0);
                    detailButton.setLayoutParams(buttonParams);
                    detailButton.setOnClickListener(v -> fetchProductDetails(product.getProductId()));
                    productInfoLayout.addView(detailButton);

                    productLayout.addView(productImage);
                    productLayout.addView(productInfoLayout);
                    messageLayout.addView(productLayout);
                }
            }
        }

        messageContainer.addView(messageLayout);
        // Không lưu tin nhắn "Đang xử lý..." vào messageList
        if (!PROCESSING_MESSAGE.equals(message)) {
            messageList.add(new Message(message, isUser, mentionedProducts));
            saveMessageHistory();
        }
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void fetchProductDetails(int productId) {
        String token = getUserToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "🚫 Bạn cần đăng nhập trước.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Fetching product details for productId: " + productId);
        Call<Product> call = apiService.getProductById("Bearer " + token, productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (!isFragmentAlive) return;

                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    Log.d(TAG, "Product fetched successfully: " + product.getName());
                    ProductDetailFragment fragment = new ProductDetailFragment();
                    Bundle args = new Bundle();
                    args.putSerializable("product", product);
                    fragment.setArguments(args);

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();

                    dismiss();
                } else {
                    String errorMessage = "❌ Lỗi khi lấy thông tin sản phẩm: " + response.code();
                    if (response.code() == 404) {
                        errorMessage = "❌ Sản phẩm không tồn tại (ID: " + productId + "). Vui lòng kiểm tra lại.";
                    } else if (response.code() == 401) {
                        errorMessage = "🚫 Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.";
                    }
                    Log.e(TAG, "Error fetching product: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                if (!isFragmentAlive) return;
                String errorMessage = "❌ Lỗi kết nối: " + t.getMessage();
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "⏰ Hết thời gian chờ. Vui lòng thử lại sau.";
                } else if (t instanceof IOException) {
                    errorMessage = "⚠ Lỗi mạng. Vui lòng kiểm tra kết nối internet.";
                }
                Log.e(TAG, "Failed to fetch product (ID: " + productId + "): " + t.getMessage());
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeProcessingMessage() {
        Log.d(TAG, "Attempting to remove processing message. messageList size: " + messageList.size() + ", messageContainer children: " + messageContainer.getChildCount());
        // Duyệt qua messageContainer để tìm và xóa tin nhắn "Đang xử lý..."
        for (int i = messageContainer.getChildCount() - 1; i >= 0; i--) {
            View view = messageContainer.getChildAt(i);
            if (view instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) view;
                if (layout.getChildCount() > 0 && layout.getChildAt(0) instanceof TextView) {
                    TextView textView = (TextView) layout.getChildAt(0);
                    String text = textView.getText().toString();
                    // So sánh với chuỗi gốc, bỏ qua định dạng HTML
                    if (text.contains(PROCESSING_MESSAGE)) {
                        messageContainer.removeViewAt(i);
                        Log.d(TAG, "Removed processing message at index: " + i);
                        break;
                    }
                }
            }
        }
        // Đảm bảo không có tin nhắn "Đang xử lý..." trong messageList
        for (int i = messageList.size() - 1; i >= 0; i--) {
            if (PROCESSING_MESSAGE.equals(messageList.get(i).getText())) {
                messageList.remove(i);
                Log.d(TAG, "Removed processing message from messageList at index: " + i);
            }
        }
        Log.d(TAG, "After removal, messageList size: " + messageList.size() + ", messageContainer children: " + messageContainer.getChildCount());
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
                displayMessage(message.getText(), message.isUser(), message.getMentionedProducts());
            }
        }
    }

    private String formatBotResponse(String message) {
        // Loại bỏ ký tự không mong muốn
        message = message.replaceAll("\\*+", "");

        // Tách các đoạn văn bằng nhiều xuống dòng
        String[] paragraphs = message.split("\n\\s*\n");
        StringBuilder formattedMessage = new StringBuilder();

        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (paragraph.isEmpty()) continue;

            // Xử lý danh sách (gạch đầu dòng)
            String[] lines = paragraph.split("\n");
            boolean isList = false;
            StringBuilder paragraphHtml = new StringBuilder();

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Kiểm tra nếu dòng bắt đầu bằng ký hiệu danh sách
                if (line.matches("^[\\-•+*]\\s+.*")) {
                    isList = true;
                    String listItem = line.replaceFirst("^[\\-•+*]\\s+", "");
                    paragraphHtml.append("<li>").append(listItem).append("</li>");
                } else {
                    // Nếu không phải danh sách, thêm dòng bình thường
                    paragraphHtml.append(line).append("<br>");
                }
            }

            // Bao danh sách trong <ul> nếu có
            if (isList) {
                formattedMessage.append("<ul>")
                        .append(paragraphHtml)
                        .append("</ul>");
            } else {
                // Định dạng đoạn văn với thụt đầu dòng
                formattedMessage.append("<p style=\"margin-left: 10px;\">")
                        .append(paragraphHtml)
                        .append("</p>");
            }

            // Thêm khoảng cách giữa các đoạn
            if (i < paragraphs.length - 1) {
                formattedMessage.append("<br>");
            }
        }

        return formattedMessage.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentAlive = false;
    }

    private static class Message {
        private final String text;
        private final boolean isUser;
        private final List<MentionedProduct> mentionedProducts;

        public Message(String text, boolean isUser, List<MentionedProduct> mentionedProducts) {
            this.text = text;
            this.isUser = isUser;
            this.mentionedProducts = mentionedProducts != null ? new ArrayList<>(mentionedProducts) : null;
        }

        public String getText() {
            return text;
        }

        public boolean isUser() {
            return isUser;
        }

        public List<MentionedProduct> getMentionedProducts() {
            return mentionedProducts != null ? new ArrayList<>(mentionedProducts) : null;
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        formatter.setMinimumFractionDigits(0);
        return formatter.format(price);
    }
}