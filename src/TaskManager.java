import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    public final HashMap<Integer, Task> tasks = new HashMap<>();
    public final HashMap<Integer, Epic> epics = new HashMap<>();
    public final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private volatile int idCounter = 0;
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
        subtasks.clear();
        epics.clear();
    }

    public void removeAllSubtasks() {
        ArrayList<Epic> epicsToUpdate = new ArrayList<>(epics.values());
        subtasks.clear();

        for (Epic epic : epicsToUpdate) {
            epic.clearSubtasks();
            epic.updateStatus();
        }
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
        int epicId = subtask.getEpicId();
        Epic parentEpic = epics.get(epicId);
        if (parentEpic == null) {
            throw new IllegalArgumentException("Эпик с ID " + epicId + " не существует");
        }

        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        parentEpic.addSubtask(subtask);
        parentEpic.updateStatus();

        return subtask;
    }

    public void updateTask(Task task) {
        if(tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }
    public void updateEpic(Epic newEpic) {
        if (!epics.containsKey(newEpic.getId())) {
            return; // Если эпика нет, ничего не делаем
        }
        Epic existingEpic = epics.get(newEpic.getId());
        existingEpic.setName(newEpic.getName());
        existingEpic.setDescription(newEpic.getDescription());
    }
    public void updateSubtask(Subtask newSubtask) {
        if (!subtasks.containsKey(newSubtask.getId())) {
            return;
        }
        Subtask existingSubtask = subtasks.get(newSubtask.getId());
        if (newSubtask.getEpicId() == existingSubtask.getEpicId()) {
            Epic parentEpic = epics.get(existingSubtask.getEpicId());
            parentEpic.removeSubtask(existingSubtask);
            parentEpic.addSubtask(newSubtask);
            subtasks.put(newSubtask.getId(), newSubtask);
            parentEpic.updateStatus();
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
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic parentEpic = epics.get(subtask.getEpicId());
            parentEpic.removeSubtask(subtask);
            subtasks.remove(id);
            parentEpic.updateStatus();
        }
    }
    public ArrayList<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return new ArrayList<>();
    }
}

