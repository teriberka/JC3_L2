package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //Если в течении 5 секунд не будет сообщений по сокету то вызовится исключение
                    socket.setSoTimeout(3000);

                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/reg ")) {
                            String[] token = str.split(" ");


                            if (token.length < 4) {
                                continue;
                            }

                            boolean succeed = server
                                    .getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (succeed) {

//                                // задание 3 | сохраняем данные пользователя при регистрации
//                                try {
//                                    server.saveUserToDB(token[1],token[2],token[3]);
//                                } catch (SQLException e) {
//                                    e.printStackTrace();
//                                }

                                sendMsg("Регистрация прошла успешно");

                            } else {
                                sendMsg("Регистрация  не удалась. \n" +
                                        "Возможно логин уже занят, или данные содержат пробел");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");

                            if (token.length < 3) {
                                continue;
                            }

                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);

                            login = token[1];

//                            // задание 1 | я понимаю что лучше быеще переделать механизм контроля кто уже авторизовался,
//                            // но сделал метод только для проверки логина и пароля.
//                            if (server.checkLoginPass(token[1], token[2])) {
////                            if (newNick != null) {
//                                if (!server.isLoginAuthorized(login)) {
//                                    sendMsg("/authok " + newNick);
//                                    nick = newNick;
//                                    server.subscribe(this);
//                                    System.out.println("Клиент: " + nick + " подключился"+ socket.getRemoteSocketAddress());
//                                    socket.setSoTimeout(0);
//                                    break;
//                                } else {
//                                    sendMsg("С этим логином уже прошли аутентификацию");
//                                }
//                            } else {
//                                sendMsg("Неверный логин / пароль");
//                            }
//                        }
//                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {

                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }

//                            // задание 2 | реализовать возможность смены ника
//                            if (str.startsWith("/change_nick ")) {
//                                System.out.println("debug | case:/change_nick | start");
//
//                                String[] token = str.split(" ", 3);
//
//                                if (token.length < 2) {
//                                    continue;
//                                }
//
//                                System.out.println("debug | case:/change_nick | old nick="+ nick + ", new nick="+token[1]);
//
//                                try {
//                                    server.changeNick(nick, token[1]);
//                                    server.broadcastMsg(nick, "сменил ник на " + token[1]);
//
//                                } catch (SQLException e) {
//                                    e.printStackTrace();
//                                }
//
//                            }


                            if (str.startsWith("/w ")) {
                                String[] token = str.split(" ", 3);

                                if (token.length < 3) {
                                    continue;
                                }

                                // задание 4 | сохранение приватного сообщения в бд
                                try {
                                    server.saveMsgToDB(nick, token[1], token[2]);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                server.privateMsg(this, token[1], token[2]);
                            }
                        } else {

//                            // задание 4 | сохранение группового сообщения в бд
//                            try {
//                                server.saveMsgToDB(nick, "all", str);
//                            } catch (SQLException e) {
//                                e.printStackTrace();
//                            }

                            server.broadcastMsg(nick, str);
                        }
                    }
                }catch (SocketTimeoutException e){
                    sendMsg("/end");
                }
                ///////
                catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент отключился");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }
}
