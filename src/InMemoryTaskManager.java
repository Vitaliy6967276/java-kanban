import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private volatile int idCounter = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private int generateId() {
        return ++idCounter;
    }

    @Override
    public List<Task> getAllTasks() {
        ArrayList<Task> allTasks = new ArrayList<>();
        allTasks.addAll(tasks.values());
        return allTasks;
    }

    @Override
    public List<Epic> getAllEpics() {
        ArrayList<Epic> allEpics = new ArrayList<>();
        allEpics.addAll(epics.values());
        return allEpics;
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        ArrayList<Subtask> allSubtasks = new ArrayList<>();
        allSubtasks.addAll(subtasks.values());
        return allSubtasks;
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void removeAllSubtasks() {
        ArrayList<Epic> epicsToUpdate = new ArrayList<>(epics.values());
        subtasks.clear();

        for (Epic epic : epicsToUpdate) {
            epic.clearSubtasks();
            epic.updateStatus();
        }
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Task generateTask(Task task) {
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        return task;
    }

    @Override
    public Epic generateEpic(Epic epic) {
        int id = generateId();
        epic.setId(id);
        epics.put(id, epic);
        return epic;
    }

    @Override
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

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        }
    }

    @Override
    public void updateEpic(Epic newEpic) {
        if (!epics.containsKey(newEpic.getId())) {
            return; // Если эпика нет, ничего не делаем
        }
        Epic existingEpic = epics.get(newEpic.getId());
        existingEpic.setName(newEpic.getName());
        existingEpic.setDescription(newEpic.getDescription());
    }

    @Override
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

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic parentEpic = epics.get(subtask.getEpicId());
            parentEpic.removeSubtask(subtask);
            subtasks.remove(id);
            parentEpic.updateStatus();
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}



