package handlers;

import adapters.DurationAdapter;
import adapters.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.Endpoint;
import exceptions.TaskNotFoundException;
import managers.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class SubstackHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .serializeNulls()
            .create();

    public SubstackHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            Endpoint endpoint = getEndpoint(requestPath, requestMethod);

            switch (endpoint) {
                case GET_SUBTASKS:
                    handleGetSubtasks(exchange);
                    break;
                case GET_SUBTASK_BY_ID:
                    handleGetSubtaskById(exchange);
                    break;
                case POST_SUBTASK:
                    handlePostSubtask(exchange);
                    break;
                case DELETE_SUBTASK:
                    handleDeleteSubtask(exchange);
                    break;
                default:
                    sendNotFound(exchange);
                    break;
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        String response = gson.toJson(subtasks);
        sendText(exchange, response);
    }

    private void handleGetSubtaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getSubtaskId(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор подзадачи");
            return;
        }
        int id = idOpt.get();
        try {
            Subtask subtask = taskManager.getSubtaskById(id);
            String response = gson.toJson(subtask);
            sendText(exchange, response);
        } catch (TaskNotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handlePostSubtask(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (requestBody.isEmpty()) {
            writeResponse(exchange, "Тело запроса пусто");
            return;
        }

        Subtask subtask;
        try {
            subtask = gson.fromJson(requestBody, Subtask.class);
        } catch (Exception e) {
            writeResponse(exchange, "Некорректный JSON");
            return;
        }

        try {
            if (subtask.getId() == null || subtask.getId() == 0) {
                taskManager.generateSubtask(subtask);
                sendNoContent(exchange);
            } else {
                taskManager.updateSubtask(subtask);
                String response = gson.toJson(subtask);
                sendText(exchange, response);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        } catch (TaskNotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getSubtaskId(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор подзадачи");
            return;
        }
        int id = idOpt.get();
        taskManager.deleteSubtask(id);
        sendNoContent(exchange);
    }

    private Optional<Integer> getSubtaskId(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length != 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_SUBTASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_SUBTASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            try {
                Integer.parseInt(pathParts[2]);
                if (requestMethod.equals("GET")) {
                    return Endpoint.GET_SUBTASK_BY_ID;
                }
                if (requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_SUBTASK;
                }
            } catch (NumberFormatException ignored) {

            }
        }

        return Endpoint.UNKNOWN;
    }

}
