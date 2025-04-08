package com.example.final_project.API_Controls;

import com.example.final_project.API_Reponse.ChatResponse;
import com.example.final_project.API_Reponse.NotificationResponse;
import com.example.final_project.API_Reponse.OrderDetailResponse;
import com.example.final_project.API_Reponse.OrderListResponse;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Reponse.ProductResponse;
import com.example.final_project.API_Reponse.UnreadCountResponse;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.API_Reponse.VoucherResponse;
import com.example.final_project.API_Requests.ChatRequest;
import com.example.final_project.API_Requests.FeedbackRequest;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.API_Requests.VoucherRequest;
import com.example.final_project.Address.Address;
import com.example.final_project.Address.District;
import com.example.final_project.Products.Product;
import com.example.final_project.Address.Province;
import com.example.final_project.Voucher;
import com.example.final_project.Address.Ward;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    // Đăng nhập
    @POST("/api/users/login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    // Đăng ký
    @POST("/api/users/register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    @POST("api/users/registerWithGoogle")
    Call<UserResponse> registerWithGoogle(@Body UserRequest userRequest);

    // Lấy thông tin người dùng
    @GET("/api/users/user")
    Call<UserResponse> getUserInfo(@Header("Authorization") String token);

    @Multipart
    @PUT("api/users/updateProfile")
    Call<UserResponse> updateProfile(
            @Header("Authorization") String token,
            @Part("name") String name,
            @Part("phone") String phone,
            @Part("gender") String gender,
            @Part("dateOfBirth") String dateOfBirth,
            @Part MultipartBody.Part profileImg
    );

    @POST("/api/users/sendOTP")
    Call<UserResponse> sendOTP(@Body UserRequest request);

    @POST("/api/users/verifyOTP")
    Call<UserResponse> verifyOTP(@Body UserRequest request);

    @POST("/api/users/forgotPassword")
    Call<UserResponse> forgotPassword(@Body UserRequest request);

    @POST("/api/users/updatePassword")
    Call<UserResponse> updatePassword(@Body UserRequest request);

    // Lấy danh sách chat
    @GET("/api/chat/{userId}")
    Call<ChatResponse> getUserChat(@Path("userId") String userId);

    // Gửi tin nhắn chat
    @POST("api/chat/ask-ai")
    Call<ChatResponse> sendChatMessage(@Header("Authorization") String token, @Body ChatRequest request);

    @GET("/api/products/{id}")
    Call<Product> getProductById(@Header("Authorization") String token, @Path("id") int productId);

    @GET("api/products/bestsellers")
    Call<ProductResponse> getBestSellers();

    // Lấy danh sách sản phẩm
    @GET("/order/products")
    Call<ProductResponse> getProducts();

    // Tạo đơn hàng
    @POST("/order/create")
    Call<OrderResponse> createOrder(@Header("Authorization") String token, @Body OrderRequest request);

    @GET("/order/order-status/{orderId}")
    Call<OrderStatusResponse> checkOrderStatus(@Path("orderId") int orderId);

    // Lấy danh sách đơn hàng
    @GET("/order/{userId}")
    Call<OrderListResponse> getUserOrders(@Header("Authorization") String token, @Path("userId") String userId);

    // Lấy chi tiết đơn hàng
    @GET("/order/detail/{orderId}")
    Call<OrderDetailResponse> getOrderDetails(@Header("Authorization") String token, @Path("orderId") String orderId);

    @GET("api/address/provinces")
    Call<List<Province>> getProvinces();

    @GET("api/address/districts/{provinceCode}")
    Call<List<District>> getDistricts(@Path("provinceCode") int provinceCode);

    @GET("api/address/wards/{districtCode}")
    Call<List<Ward>> getWards(@Path("districtCode") int districtCode);

    @GET("api/address/user/{userId}")
    Call<List<Address>> getUserAddresses(@Path("userId") int userId);

    @POST("api/address/save")
    Call<Void> saveAddress(@Body Address address);

    @DELETE("api/address/{addressId}")
    Call<Void> deleteAddress(@Path("addressId") int addressId);

    @POST("api/feedback/save")
    Call<Void> sendFeedback(@Body FeedbackRequest request);

    @GET("api/vouchers/{user_id}")
    Call<List<Voucher>> getVouchers(@Path("user_id") int userId);

    @POST("api/vouchers/apply")
    Call<VoucherResponse> applyVoucher(@Body VoucherRequest voucherRequest);

    @POST("/order/calculate-total")
    Call<Map<String, Double>> calculateTotal(@Header("Authorization") String token, @Body Map<String, Object> request);

    @GET("notifications/{userId}")
    Call<NotificationResponse> getNotifications(@Path("userId") int userId, @Header("Authorization") String token);

    @GET("notifications/unread-count/{userId}")
    Call<UnreadCountResponse> getUnreadCount(@Path("userId") int userId, @Header("Authorization") String token);

    @DELETE("notifications/{id}")
    Call<Void> deleteNotification(@Path("id") int id, @Header("Authorization") String token);

    // Sửa endpoint để khớp với route trên server
    @GET("/api/spin/count/{userId}")
    Call<Map<String, Integer>> getSpinCount(
            @Path("userId") int userId,
            @Header("Authorization") String token
    );

    // Thêm /spin vào endpoint để khớp với route /api/spin/spin/:userId
    @POST("/api/spin/spin/{userId}")
    Call<Map<String, String>> spinWheel(
            @Path("userId") int userId,
            @Header("Authorization") String token
    );
}