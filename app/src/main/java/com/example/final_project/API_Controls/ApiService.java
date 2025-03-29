package com.example.final_project.API_Controls;

import com.example.final_project.API_Reponse.ChatResponse;
import com.example.final_project.API_Reponse.OrderDetailResponse;
import com.example.final_project.API_Reponse.OrderListResponse;
import com.example.final_project.API_Reponse.OrderResponse;
import com.example.final_project.API_Reponse.OrderStatusResponse;
import com.example.final_project.API_Reponse.ProductResponse;
import com.example.final_project.API_Reponse.UserResponse;
import com.example.final_project.API_Requests.ChatRequest;
import com.example.final_project.API_Requests.OrderRequest;
import com.example.final_project.API_Requests.UserRequest;
import com.example.final_project.Products.Product;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {

    // Đăng nhập
    @POST("/api/users/login")
    Call<UserResponse> loginUser(@Body UserRequest request);

    // Đăng ký
    @POST("/api/users/register")
    Call<UserResponse> registerUser(@Body UserRequest request);

    // Lấy thông tin người dùng
    @GET("/api/users/user")
    Call<UserResponse> getUserInfo(@Header("Authorization") String token);

    // Cập nhật thông tin người dùng
    @PUT("/api/users/updateProfile")
    Call<UserResponse> updateProfile(@Header("Authorization") String token, @Body UserRequest request);

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

    @GET("products/{id}")
    Call<Product> getProductById(@Header("Authorization") String token, @Path("id") int productId);

    // Lấy danh sách sản phẩm
    @GET("/order/products")
    Call<ProductResponse> getProducts();

    // Tạo đơn hàng
    @POST("/order/create")
    Call<OrderResponse> createOrder(@Header("Authorization") String token, @Body OrderRequest request);

    @GET("/order-status/{orderId}")
    Call<OrderStatusResponse> checkOrderStatus(@Path("orderId") int orderId);

    // Lấy danh sách đơn hàng
    @GET("/order/{userId}")
    Call<OrderListResponse> getUserOrders(@Header("Authorization") String token, @Path("userId") String userId);

    // Lấy chi tiết đơn hàng
    @GET("/order/detail/{orderId}")
    Call<OrderDetailResponse> getOrderDetails(@Header("Authorization") String token, @Path("orderId") String orderId);
}