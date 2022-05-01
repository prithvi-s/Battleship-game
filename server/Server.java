package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.*;

public class Server {
        private static Connection connection;
        private static int id=10;
        public Server(int port) {
            try {
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

                ServerSocket serverSocket = new ServerSocket(port);
                MatchRoom matchRoom = new MatchRoom();
                do {
                    new Player(serverSocket.accept(), matchRoom).start();
                 } while (true);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8900;
        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        new Server(port);
    }

}
