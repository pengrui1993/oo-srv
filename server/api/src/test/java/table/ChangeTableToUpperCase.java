package table;


import java.sql.DriverManager;
import java.sql.SQLException;

public class ChangeTableToUpperCase {
    public static void thatIsWorkingWithMysql() throws ClassNotFoundException, SQLException {
        var url =  "jdbc:mysql://127.0.0.1:3306/jeecg-boot?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        var uname = "root";
        var pwd = "123456";
        var driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        var conn = DriverManager.getConnection(url,uname,pwd);
        var sm = conn.createStatement();
        sm.execute("""
                select t.table_name
                from information_schema.tables as t
                where t.table_schema = 'jeecg-boot'
                    and t.table_name like 'qrtz_%'
                """);
        var res = sm.getResultSet();
        conn.setAutoCommit(false);
        try{
            while(res.next()){
                var tableName = res.getString(1);
                var updater = conn.createStatement();
                updater.execute(String.format("rename table %s to %s",tableName,tableName.toUpperCase()));
                updater.close();
            }
            conn.commit();
        }catch (Throwable t){
            conn.rollback();
        }
        sm.close();
        conn.close();
    }
}
