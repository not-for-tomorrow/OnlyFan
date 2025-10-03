# 🚪 Logout Feature - Chức năng đăng xuất

## ✅ **Đã thêm thành công nút Logout!**

### 🎯 **Tính năng mới:**

#### 1. **Nút Logout trong MainActivity**
- **Vị trí:** Dưới nút "Test API Connection"
- **Màu sắc:** Đỏ (#F44336) với chữ trắng
- **Chức năng:** Đăng xuất khỏi tất cả tài khoản

#### 2. **Chức năng Logout hoàn chỉnh:**
```java
private void logout() {
    // Sign out from Firebase
    mAuth.signOut();
    
    // Sign out from Google
    mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
        // Show logout success message
        Toast.makeText(MainActivity.this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
        
        // Navigate back to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    });
}
```

## 🎯 **Cách hoạt động:**

### ✅ **Khi nhấn nút Logout:**
1. **Đăng xuất khỏi Firebase** - Xóa session Firebase
2. **Đăng xuất khỏi Google** - Xóa session Google Sign-In
3. **Hiển thị thông báo** - "Đã đăng xuất thành công!"
4. **Chuyển về LoginActivity** - Quay lại màn hình đăng nhập
5. **Xóa stack activity** - Không thể quay lại MainActivity bằng nút Back

## 🚀 **Kết quả:**

### ✅ **Sau khi logout:**
- ✅ Đã đăng xuất khỏi Firebase
- ✅ Đã đăng xuất khỏi Google
- ✅ Quay lại màn hình Login
- ✅ Có thể đăng nhập lại với tài khoản khác
- ✅ Google Sign-In sẽ hiển thị account picker

## 📱 **UI Layout:**

```
┌─────────────────────────┐
│    Welcome [Username]  │
│                         │
│  Enter Product ID:      │
│  [Input Field]         │
│                         │
│  [View Product Detail] │
│  [Test API Connection] │
│  [    LOGOUT    ]      │ ← Nút mới (màu đỏ)
└─────────────────────────┘
```

## 🔧 **Technical Details:**

### **Imports đã thêm:**
```java
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
```

### **Variables đã thêm:**
```java
private Button btnLogout;
private FirebaseAuth mAuth;
private GoogleSignInClient mGoogleSignInClient;
```

## 🎉 **Test Logout:**

1. **Đăng nhập bằng Google Sign-In**
2. **Chuyển sang MainActivity**
3. **Nhấn nút "Logout" (màu đỏ)**
4. **Kiểm tra thông báo "Đã đăng xuất thành công!"**
5. **Kiểm tra quay lại LoginActivity**
6. **Test đăng nhập lại với tài khoản khác**

## ✅ **Hoàn thành!**

**Nút Logout đã được thêm thành công vào MainActivity!**

- ✅ Giao diện đẹp với nút màu đỏ
- ✅ Chức năng logout hoàn chỉnh
- ✅ Đăng xuất khỏi Firebase và Google
- ✅ Chuyển về LoginActivity
- ✅ Có thể đăng nhập lại

**Ứng dụng giờ đây có đầy đủ chức năng đăng nhập và đăng xuất!** 🚀
