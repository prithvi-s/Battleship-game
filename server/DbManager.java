package server;
import java.sql.*;

public class DbManager {
    private static Connection connection;
    private static int id=10;
    public static void initialize() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/playas", "root", "password");
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from playas.players");
            while (rs.next()){
                System.out.println(rs.getInt(1) + "  " + rs.getString(2));
                id++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
 /*   public static void insertdb(Player player){
        try {
            String nam = player.getPlayerName();
            Statement stmt = connection.createStatement();
            String sql = "INSERT INTO playas.players VALUES(id,nam)";
            stmt.executeUpdate(sql);
        }catch (SQLException e){
            e.printStackTrace();
        }finally{
            try{
                connection.close();
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }
    } */
}

