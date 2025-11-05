package handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    Gson gson = new Gson();

    public static class ErrorResponse {
        String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNoContent(HttpExchange h) throws IOException {
        h.sendResponseHeaders(201, 0);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        ErrorResponse errorHolder = new ErrorResponse("Объект не найден");
        String response = gson.toJson(errorHolder);
        byte[] resp = response.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        ErrorResponse errorHolder = new ErrorResponse("Задача пересекается с другими по времени");
        String response = gson.toJson(errorHolder);
        byte[] resp = response.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(400, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendInternalServerError(HttpExchange h) throws IOException {
        ErrorResponse errorHolder = new ErrorResponse("Произошла ошибка при обработке запроса");
        String response = gson.toJson(errorHolder);
        byte[] resp = response.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        h.sendResponseHeaders(500, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void writeResponse(HttpExchange h,
                                 String responseString) throws IOException {
        try (OutputStream os = h.getResponseBody()) {
            h.sendResponseHeaders(400, 0);
            os.write(responseString.getBytes(StandardCharsets.UTF_8));
        }
        h.close();
    }
}