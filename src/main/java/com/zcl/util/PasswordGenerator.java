package com.zcl.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordGenerator {
    public static void main(String[] args) {
        String password = "admin123";

        // 使用固定盐值生成可预测的哈希
        String salt = BCrypt.gensalt(10);
        String hashed = BCrypt.hashpw(password, salt);
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hashed);

        // 验证
        boolean matches = BCrypt.checkpw(password, hashed);
        System.out.println("验证结果: " + matches);
    }

    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }

    public static boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
