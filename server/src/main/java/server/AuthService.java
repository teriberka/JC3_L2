package server;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password);

    // добавляем метод для получении ника из бд по нику и паролю в интерфейс
    String getNicknameByLoginAndPasswordFromDB(String login, String password);

    boolean registration(String login, String password, String nickname);
}
