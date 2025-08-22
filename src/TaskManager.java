import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    public final HashMap<Integer, Task> tasks = new HashMap<>();
    public final HashMap<Integer, Epic> epics = new HashMap<>();
    public final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private static volatile int idCounter = 0;
    protected synchronized int generateId() {
        return ++idCounter;
    }

    public ArrayList<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        return allTasks;
    }
    public ArrayList<Epic> getAllEpics() {
        ArrayList<Epic> allEpics = new ArrayList<>();
        allEpics.addAll(epics.values());
        return allEpics;
    }
    public ArrayList<Subtask> getAllSubtasks() {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        allSubtasks.addAll(subtasks.values());
        return allSubtasks;
    }
    public void removeAllTasks() {
        tasks.clear();
    }
    public void removeAllEpics() {
        epics.clear();
    }
    public void removeAllSubtasks() {
        subtasks.clear();
    }
    public Task getTaskById(int id) {
        return tasks.get(id);
    }
    public Epic getEpicById(int id) {
        return epics.get(id);
    }
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }
    public Task generateTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }
    public Epic generateEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }
    public Subtask generateSubtask(Subtask subtask) {
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        Epic parentEpic = subtask.getEpicParent();
        if (parentEpic != null) {
            parentEpic.addSubtask(subtask);
        }
        return subtask;
    }
    public void updateTask(Task task) {
        if(tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }
    public void updateEpic(Epic epic) {
        if(epics.containsKey(epic.getId())) {
            epics.put(epic.getId(), epic);
        }
    }
    public void updateSubtask(Subtask subtask) {
        if(subtasks.containsKey(subtask.getId())) {
            subtasks.put(subtask.getId(), subtask);
        }
    }
    public void deleteTask(int id) {
        tasks.remove(id);
    }
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
        }
    }

    public void deleteSubtask(int id) {
        subtasks.remove(id);
    }
    public ArrayList<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return new ArrayList<>();
    }
}

