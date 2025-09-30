package managers;

import tasks.Task;
import tasks.Epic;
import tasks.Subtask;

import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void removeAllTasks();

    void removeAllEpics();

    void removeAllSubtasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubtaskById(int id);

    Task generateTask(Task task);

    Epic generateEpic(Epic epic);

    Subtask generateSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic newEpic);

    void updateSubtask(Subtask newSubtask);

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    List<Subtask> getSubtasksByEpic(int epicId);

    List<Task> getHistory();

}
