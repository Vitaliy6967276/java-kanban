import com.sun.net.httpserver.HttpServer;
import managers.Managers;
import managers.TaskManager;

import java.net.InetSocketAddress;

public class HttpTaskServer {
    public static void main(String[] args) throws Exception {
        TaskManager taskManager = Managers.getDefault();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", new TaskHandler(taskManager));
        server.createContext("/subtasks", new SubstackHandler(taskManager));
        server.createContext("/epics", new EpicHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
        server.start();
        System.out.println("Server started on port 8080");
    }
}