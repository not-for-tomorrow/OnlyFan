package com.example.onlyfanshop.ultils;

import android.content.Context;

import me.leolin.shortcutbadger.ShortcutBadger;

public class BadgeUtils {
    public static void updateCartBadge(Context context, int cartCount) {
        try {
            ShortcutBadger.applyCount(context, cartCount); // Hiển thị số trên icon
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Xóa badge khi người dùng checkout hoặc giỏ hàng rỗng
    public static void clearBadge(Context context) {
        try {
            ShortcutBadger.removeCount(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
