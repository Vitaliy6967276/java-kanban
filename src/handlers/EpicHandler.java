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
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .serializeNulls()
            .create();

    public EpicHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            Endpoint endpoint = getEndpoint(requestPath, requestMethod);

            switch (endpoint) {
                case GET_EPICS:
                    handleGetEpics(exchange);
                    break;
                case GET_EPIC_BY_ID:
                    handleGetEpicById(exchange);
                    break;
                case POST_EPIC:
                    handlePostEpic(exchange);
                    break;
                case DELETE_EPIC:
                    handleDeleteEpic(exchange);
                    break;
                case GET_EPIC_SUBTASKS:
                    handleGetEpicSubtasks(exchange);
                    break;
                default:
                    sendNotFound(exchange);
                    break;
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        List<Epic> epics = taskManager.getAllEpics();
        String response = gson.toJson(epics);
        sendText(exchange, response);
    }

    private void handleGetEpicById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getEpicId(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор эпика");
            return;
        }
        int id = idOpt.get();
        try {
            Epic epic = taskManager.getEpicById(id);
            String response = gson.toJson(epic);
            sendText(exchange, response);
        } catch (TaskNotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handlePostEpic(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (requestBody.isEmpty()) {
            writeResponse(exchange, "Тело запроса пусто");
            return;
        }

        Epic epic;
        try {
            epic = gson.fromJson(requestBody, Epic.class);
        } catch (Exception e) {
            writeResponse(exchange, "Некорректный JSON");
            return;
        }

        try {
            if (epic.getId() == null || epic.getId() == 0) {
                taskManager.generateEpic(epic);
                String response = gson.toJson(epic);
                sendText(exchange, response);
            } else {
                taskManager.updateEpic(epic);
                String response = gson.toJson(epic);
                sendText(exchange, response);
            }
        } catch (IllegalArgumentException e) {
            writeResponse(exchange, e.getMessage());
        }
    }

    private void handleDeleteEpic(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getEpicId(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор эпика");
            return;
        }
        int id = idOpt.get();
        taskManager.deleteEpic(id);
        sendNoContent(exchange);
    }

    private void handleGetEpicSubtasks(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getEpicIdFromSubtasksPath(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор эпика в пути /epics/{id}/subtasks");
            return;
        }
        int epicId = idOpt.get();
        List<Subtask> subtasks = taskManager.getSubtasksByEpic(epicId);
        String response = gson.toJson(subtasks);
        sendText(exchange, response);
    }

    private Optional<Integer> getEpicId(HttpExchange exchange) {
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

    private Optional<Integer> getEpicIdFromSubtasksPath(HttpExchange exchange) {
        String[] pathParts = exchange.getRequestURI().getPath().split("/");
        if (pathParts.length != 4 || !pathParts[3].equals("subtasks")) {
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

        if (pathParts.length == 2 && pathParts[1].equals("epics")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_EPICS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_EPIC;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("epics")) {
            try {
                Integer.parseInt(pathParts[2]);
                if (requestMethod.equals("GET")) {
                    return Endpoint.GET_EPIC_BY_ID;
                }
                if (requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_EPIC;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (pathParts.length == 4 && pathParts[1].equals("epics") && pathParts[3].equals("subtasks")) {
            try {
                Integer.parseInt(pathParts[2]);
                if (requestMethod.equals("GET")) {
                    return Endpoint.GET_EPIC_SUBTASKS;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return Endpoint.UNKNOWN;
    }


}
