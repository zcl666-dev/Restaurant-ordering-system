package com.zcl;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库连接测试类
 * 应用启动时自动执行数据库连接测试
 */
//@Component      //数据库连接测试
public class DBConnectionTest implements CommandLineRunner {

    // 数据库配置信息
    private static final String URL = "jdbc:mysql://localhost:3306/restaurant_order_system?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("开始测试数据库连接...");
        System.out.println("========================================");
        
        testConnection();
        
        System.out.println("========================================");
        System.out.println("数据库连接测试完成！");
        System.out.println("========================================");
    }

    /**
     * 测试数据库连接
     */
    private void testConnection() {
        Connection connection = null;
        try {
            // 加载驱动
            Class.forName(DRIVER);
            
            // 获取数据库连接
            System.out.println("正在尝试连接数据库...");
            System.out.println("URL: " + URL);
            System.out.println("用户名: " + USER);
            
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            
            // 检查连接是否成功
            if (connection != null && !connection.isClosed()) {
                System.out.println("\n✅ 数据库连接成功！\n");
                
                // 获取数据库元数据
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("========== 数据库信息 ==========");
                System.out.println("数据库产品名称: " + metaData.getDatabaseProductName());
                System.out.println("数据库产品版本: " + metaData.getDatabaseProductVersion());
                System.out.println("数据库URL: " + metaData.getURL());
                System.out.println("用户名: " + metaData.getUserName());
                System.out.println("驱动名称: " + metaData.getDriverName());
                System.out.println("驱动版本: " + metaData.getDriverVersion());
                
                // 显示当前连接的数据库
                String catalog = connection.getCatalog();
                System.out.println("当前连接的数据库: " + catalog);
                System.out.println("================================");
                
            } else {
                System.out.println("\n❌ 数据库连接失败，无法获取连接对象。\n");
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("\n❌ MySQL驱动程序未找到！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("请确保pom.xml中已添加mysql-connector-j依赖\n");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("\n❌ 数据库连接异常！");
            System.err.println("错误信息: " + e.getMessage());
            System.err.println("错误代码: " + e.getErrorCode());
            System.err.println("SQL状态: " + e.getSQLState());
            System.err.println("\n可能的原因：");
            System.err.println("1. MySQL服务未启动");
            System.err.println("2. 数据库'restaurant_order_system'不存在");
            System.err.println("3. 用户名或密码错误");
            System.err.println("4. 数据库地址或端口不正确\n");
            e.printStackTrace();
        } finally {
            // 关闭连接
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("数据库连接已关闭。");
                } catch (SQLException e) {
                    System.err.println("关闭数据库连接时出错: " + e.getMessage());
                }
            }
        }
    }
}
