package managers;

import exceptions.ManagerSaveException;
import tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        loadFromFile();
    }

    private void save() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("id,type,name,status,description,epic,duration,startTime,endTime\n");


            for (Task task : getAllTasks()) {
                sb.append(toString(task)).append("\n");
            }
            for (Epic epic : getAllEpics()) {
                sb.append(toString(epic)).append("\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                sb.append(toString(subtask)).append("\n");
            }

            Files.writeString(file.toPath(), sb.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }


    public void forceSaveForTests() {
        save();
    }

    private String toString(Task task) {
        TaskType type;
        String epicId = "";
        String durationStr = "";
        String startTimeStr = "";
        String endTimeStr = "";


        if (task instanceof Epic) {
            type = TaskType.EPIC;
            if (task.getDuration() != null) {
                durationStr = String.valueOf(task.getDuration().toMinutes());
            }
            if (task.getStartTime() != null) {
                startTimeStr = task.getStartTime().toString();
            }
            if (task.getEndTime() != null) {
                endTimeStr = task.getEndTime().toString();
            }
        } else if (task instanceof Subtask) {
            type = TaskType.SUBTASK;
            epicId = String.valueOf(((Subtask) task).getEpicId());
            if (task.getDuration() != null) {
                durationStr = String.valueOf(task.getDuration().toMinutes());
            }
            if (task.getStartTime() != null) {
                startTimeStr = task.getStartTime().toString();
            }
            if (task.getEndTime() != null) {
                endTimeStr = task.getEndTime().toString();
            }
        } else {
            type = TaskType.TASK;
            if (task.getDuration() != null) {
                durationStr = String.valueOf(task.getDuration().toMinutes());
            }
            if (task.getStartTime() != null) {
                startTimeStr = task.getStartTime().toString();
            }
            if (task.getEndTime() != null) {
                endTimeStr = task.getEndTime().toString();
            }
        }


        return String.format("%d,%s,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getName(),
                task.getTaskStatus(),
                task.getDescription(),
                epicId,
                durationStr,
                startTimeStr,
                endTimeStr);
    }

    private Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        TaskStatus status = TaskStatus.valueOf(parts[3]);
        String description = parts[4];
        String epicIdStr = parts[5];
        String durationStr = parts[6];
        String startTimeStr = parts[7];
        String endTimeStr = parts[8];
        Duration duration = null;
        if (!durationStr.isEmpty()) {
            long durationMinutes = Long.parseLong(durationStr);
            if (durationMinutes > 0) {
                duration = Duration.ofMinutes(durationMinutes);
            }
        }
        LocalDateTime startTime = null;
        if (!startTimeStr.isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr);
        }
        LocalDateTime endTime = null;
        if (!endTimeStr.isEmpty()) {
            endTime = LocalDateTime.parse(endTimeStr);
        }

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status, duration, startTime);
                task.setId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setTaskStatus(status);
                epic.setId(id);
                epic.setStartTime(startTime);
                epic.setEndTime(endTime);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(epicIdStr);
                Subtask subtask = new Subtask(name, description, epicId, duration, startTime);
                subtask.setTaskStatus(status);
                subtask.setId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private void loadFromFile() {
        if (!file.exists()) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.size() <= 1) return;

            int maxId = 0;
            List<Task> tasksToAdd = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) continue;
                Task task = fromString(line);
                tasksToAdd.add(task);
                if (task.getId() > maxId) {
                    maxId = task.getId();
                }
            }

            this.idCounter = maxId + 1;

            for (Task task : tasksToAdd) {
                if (task instanceof Epic) {
                    addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    addSubtask((Subtask) task);
                } else {
                    addTask(task);
                }
            }

            for (Epic epic : epics.values()) {
                epic.updateStatus();
                epic.updateEpicTime();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return new FileBackedTaskManager(file);
    }

    @Override
    public Task generateTask(Task task) {
        Task result = super.generateTask(task);
        save();
        return result;
    }

    @Override
    public Epic generateEpic(Epic epic) {
        Epic result = super.generateEpic(epic);
        save();
        return result;
    }

    @Override
    public Subtask generateSubtask(Subtask subtask) {
        Subtask result = super.generateSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        save();
    }

    @Override
    public void updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }
}
