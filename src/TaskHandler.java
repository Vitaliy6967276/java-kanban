import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private Gson gson = new Gson();

    public TaskHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .serializeNulls()
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String requestMethod = exchange.getRequestMethod();
            String requestPath = exchange.getRequestURI().getPath();
            Endpoint endpoint = getEndpoint(requestPath, requestMethod);

            switch (endpoint) {
                case GET_TASKS:
                    handleGetTasks(exchange);
                    break;
                case GET_TASK_BY_ID:
                    handleGetTaskById(exchange);
                    break;
                case POST_TASK:
                    handlePostTask(exchange);
                    break;
                case DELETE_TASK:
                    handleDeleteTask(exchange);
                    break;
                default:
                    sendNotFound(exchange);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(exchange);
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getAllTasks();
        String response = gson.toJson(tasks);
        sendText(exchange, response);
    }

    private void handleGetTaskById(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи");
            return;
        }
        int id = idOpt.get();
        Task task = taskManager.getTaskById(id);
        if (task != null) {
            String response = gson.toJson(task);
            sendText(exchange, response);
        } else {
            sendNotFound(exchange);
        }
    }

    private void handlePostTask(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (requestBody.isEmpty()) {
            writeResponse(exchange, "Тело запроса пусто");
            return;
        }

        Task task;
        try {
            task = gson.fromJson(requestBody, Task.class);
        } catch (Exception e) {
            writeResponse(exchange, "Некорректный JSON");
            return;
        }

        try {
            if (task.getId() == null || task.getId() == 0) {
                taskManager.generateTask(task);
                sendNoContent(exchange);
            } else {
                taskManager.updateTask(task);
                String response = gson.toJson(task);
                sendText(exchange, response);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange);
        }
    }

    private void handleDeleteTask(HttpExchange exchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(exchange);
        if (idOpt.isEmpty()) {
            writeResponse(exchange, "Некорректный идентификатор задачи");
            return;
        }
        int id = idOpt.get();
        taskManager.deleteTask(id);
        sendNoContent(exchange);
    }

    private Optional<Integer> getTaskId(HttpExchange exchange) {
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
        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            if (requestMethod.equals("GET")) {
                return Endpoint.GET_TASKS;
            }
            if (requestMethod.equals("POST")) {
                return Endpoint.POST_TASK;
            }
        }
        if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            try {


                Integer.parseInt(pathParts[2]);
                if (requestMethod.equals("GET")) {
                    return Endpoint.GET_TASK_BY_ID;
                }
                if (requestMethod.equals("DELETE")) {
                    return Endpoint.DELETE_TASK;
                }
            } catch (NumberFormatException e) {
            }
        }

        return Endpoint.UNKNOWN;
    }

    private static class DurationAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
            if (duration == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(duration.toMinutes());
            }
        }

        @Override
        public Duration read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            String value = jsonReader.nextString();
            try {
                long minutes = Long.parseLong(value);
                return Duration.ofMinutes(minutes);
            } catch (NumberFormatException e) {
                throw new IOException("Некорректное значение duration: должно быть числом минут, получено: " + value,
                        e);
            }
        }
    }

    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            if (localDateTime == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(localDateTime.format(FORMATTER));
            }
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL) {
                jsonReader.nextNull();
                return null;
            }
            String dateString = jsonReader.nextString();
            try {
                return LocalDateTime.parse(dateString, FORMATTER);
            } catch (Exception e) {
                throw new IOException("Некорректный формат startTime: ожидался dd-MM-yyyy HH:mm, получено: " + dateString, e);
            }
        }
    }

    enum Endpoint {GET_TASKS, GET_TASK_BY_ID, POST_TASK, DELETE_TASK, UNKNOWN}
}