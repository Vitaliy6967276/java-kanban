import com.sun.net.httpserver.HttpServer;
import handlers.*;
import managers.Managers;
import managers.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer server;
    private final TaskManager taskManager;
    private final int port;

    public HttpTaskServer(TaskManager taskManager, int port) {
        this.taskManager = taskManager;
        this.port = port;
        this.server = createHttpServer();
    }

    private HttpServer createHttpServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/tasks", new TaskHandler(taskManager));
            server.createContext("/subtasks", new SubstackHandler(taskManager));
            server.createContext("/epics", new EpicHandler(taskManager));
            server.createContext("/history", new HistoryHandler(taskManager));
            server.createContext("/prioritized", new PrioritizedHandler(taskManager));

            return server;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при создании HTTP-сервера", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порту " + port);
    }

    public void stop(int delay) {
        server.stop(delay);
        System.out.println("Сервер остановлен");
    }

    public int getPort() {
        return port;
    }

    public static void main(String[] args) {
        try {
            TaskManager taskManager = Managers.getDefault();
            HttpTaskServer server = new HttpTaskServer(taskManager, 8080);
            server.start();
        } catch (Exception e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }
}