package com.example.final_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 8;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_SECURITY_QUESTION = "security_question";
    private static final String COLUMN_SECURITY_ANSWER = "security_answer";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FULL_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PHONE + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_SECURITY_QUESTION + " TEXT, " +
                COLUMN_SECURITY_ANSWER + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public boolean addUser(String fullName, String email, String phone, String password, String securityQuestion, String securityAnswer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String hashedPassword = SecurityUtils.hashSHA256(password);
        String hashedSecurityQuestion = SecurityUtils.hashSHA256(securityQuestion);
        String hashedSecurityAnswer = SecurityUtils.hashSHA256(securityAnswer);

        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_PASSWORD, hashedPassword); // Mã hóa mật khẩu
        values.put(COLUMN_SECURITY_QUESTION, hashedSecurityQuestion); // Mã hóa câu hỏi bảo mật
        values.put(COLUMN_SECURITY_ANSWER, hashedSecurityAnswer); // Mã hóa câu trả lời bảo mật


        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;  // Trả về true nếu thành công
    }



    // Kiểm tra email đã tồn tại chưa
    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Kiểm tra số điện thoại đã tồn tại chưa
    public boolean isPhoneExists(String phone) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_PHONE + "=?", new String[]{phone});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Xác thực đăng nhập bằng email hoặc số điện thoại & mật khẩu đã mã hóa
    public boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String hashedPassword = SecurityUtils.hashSHA256(password); // Mã hóa mật khẩu nhập vào

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS +
                        " WHERE (" + COLUMN_EMAIL + "=? OR " + COLUMN_PHONE + "=?) AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, username, hashedPassword}
        );

        boolean isAuthenticated = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isAuthenticated;
    }

    public boolean checkSecurityQuestionAnswer(String username, String securityQuestion, String securityAnswer) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Mã hóa câu hỏi và câu trả lời bảo mật
        String hashedQuestion = SecurityUtils.hashSHA256(securityQuestion);
        String hashedAnswer = SecurityUtils.hashSHA256(securityAnswer);

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE (" + COLUMN_EMAIL + "=? OR " + COLUMN_PHONE + "=?) " +
                        "AND " + COLUMN_SECURITY_QUESTION + "=? AND " + COLUMN_SECURITY_ANSWER + "=?",
                new String[]{username, username, hashedQuestion, hashedAnswer}
        );

        boolean isValid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isValid;
    }



    // Cập nhật mật khẩu mới cho người dùng
    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword); // Cập nhật mật khẩu đã mã hóa

        // Cập nhật mật khẩu cho người dùng dựa trên email hoặc số điện thoại
        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_EMAIL + "=? OR " + COLUMN_PHONE + "=?",
                new String[]{username, username});
        db.close();

        return rowsAffected > 0;
    }

    // Lấy tên đầy đủ của người dùng
    public String getUserFullName(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_FULL_NAME + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_EMAIL + "=? OR " + COLUMN_PHONE + "=?",
                new String[]{username, username}
        );

        String fullName = null;
        if (cursor.moveToFirst()) {
            fullName = cursor.getString(cursor.getColumnIndex(COLUMN_FULL_NAME));
        }
        cursor.close();
        db.close();
        return fullName;
    }


}
