package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

// import java sql lib
import java.sql.*;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;

//    // param for SQLite db connection
//    private static Connection connection;
//    private static Statement stmt;
//    private static PreparedStatement psInsert;
//
//
//    public static void dbConnect() throws ClassNotFoundException, SQLException {
//        Class.forName("org.sqlite.JDBC");
//        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
//        stmt = connection.createStatement();
//    }
//
//    public static void dbDisconnect() {
//        try {
//            stmt.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        try {
//            connection.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // метод для проверки подключения к бд
//    public static void selectEx() throws SQLException {
//        ResultSet rs = stmt.executeQuery("SELECT login,pass,nick FROM users;");
//
//        while (rs.next()) {
//            System.out.println(rs.getString("login") + " " + rs.getString("pass") + " " + rs.getString("nick"));
//        }
//        rs.close();
//    }
//
//    // задание 1 , метод проверяющий существует такой логин и пароль, возвращает true если логин и пароль существует
//    // и false если логин или пароль задан не верно.
//    public static boolean checkLoginPass(String login, String pass) {
//        try {
//            ResultSet rs = stmt.executeQuery("SELECT nick FROM users where login='" + login + "' and pass='" + pass + "';");
//            return true;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
////        return rs.getString("nick");
//    }


    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            try {
                dbConnect();
                System.out.println("База данных SQLite подключена!");

//                selectEx();

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился ");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {

                dbDisconnect();
                System.out.println("Отключились от базы данных SQLite!");

                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String nick, String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(nick + ": " + msg);
        }
    }

//    // задание 1 | метод сохраняющий новый ник
//    public void changeNick(String oldNick, String newNick) throws SQLException {
//        psInsert = connection.prepareStatement("update users set nick=? where nick=? ;");
//
//        connection.setAutoCommit(false);
//
//        psInsert.setString(2, oldNick);
//        psInsert.setString(1, newNick);
//        psInsert.executeUpdate();
//
//        connection.setAutoCommit(true);
//    }
//
//
//    // задание 3 | метод сохраняющий логин пароль ниск при регистрации
//    public void saveUserToDB(String login, String pass, String nick) throws SQLException {
//        psInsert = connection.prepareStatement("INSERT INTO users (login, pass, nick) VALUES (?, ?, ?))");
//
//        connection.setAutoCommit(false);
//
//        psInsert.setString(1, login);
//        psInsert.setString(2, pass);
//        psInsert.setString(3, nick);
//        psInsert.executeUpdate();
//
//        connection.setAutoCommit(true);
//    }
//
//    // задание 4 | метод сохраняющий сообщение в бд
//    public void saveMsgToDB(String fromNick, String toNick, String msg) throws SQLException {
//        psInsert = connection.prepareStatement("INSERT INTO msg_history (from_nick, to_nick, msg, time) VALUES (?, ?, ?,(SELECT datetime('now')))");
//
//        connection.setAutoCommit(false);
//
//        psInsert.setString(1, fromNick);
//        psInsert.setString(2, toNick);
//        psInsert.setString(3, msg);
//        psInsert.executeUpdate();
//
//        connection.setAutoCommit(true);
//    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] private [ %s ] : %s",
                sender.getNick(), receiver, msg);

        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver)) {
                c.sendMsg(message);
                if (!sender.getNick().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }

        sender.sendMsg("not found user: " + receiver);
    }


    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthorized(String login){
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");

        for (ClientHandler c : clients) {
            sb.append(c.getNick()).append(" ");
        }
        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}
