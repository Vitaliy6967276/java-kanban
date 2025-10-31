package managers;

import history.HistoryManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected volatile int idCounter = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(
            Task::getStartTime,
            Comparator.nullsLast(Comparator.naturalOrder())
    ));

    private int generateId() {
        return ++idCounter;
    }

    protected void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    protected void addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    protected void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic parentEpic = epics.get(subtask.getEpicId());
        if (parentEpic != null) {
            parentEpic.addSubtask(subtask);
        }
    }

    public boolean isTaskOverlappingWithOthers(Task task) {
        if (task.getStartTime() == null || task.getDuration() == null) {
            return false;
        }
        List<Task> allTasks = new ArrayList<>();
        allTasks.addAll(getAllTasks());
        allTasks.addAll(getAllSubtasks());
        return allTasks.stream()
                .filter(existingTask -> existingTask.getId() != task.getId())
                .anyMatch(existingTask -> task.isOverlapping(existingTask));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
        for (int id : tasks.keySet()) {
            historyManager.remove(id);
            Task task = tasks.get(id);
            if (task != null && task.getStartTime() != null) {
                prioritizedTasks.remove(task);
            }
        }
        tasks.clear();
    }

    @Override
    public void removeAllEpics() {
        epics.values().stream()
                .flatMap(epic -> epic.getSubtasks().stream())
                .forEach(subtask -> {
                    historyManager.remove(subtask.getId());
                    if (subtask.getStartTime() != null) {
                        prioritizedTasks.remove(subtask);
                    }
                });

        epics.keySet().forEach(id -> {
            historyManager.remove(id);
            Epic epic = epics.get(id);
            if (epic.getStartTime() != null) {
                prioritizedTasks.remove(epic);
            }
        });
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void removeAllSubtasks() {
        subtasks.keySet().forEach(id -> {
            historyManager.remove(id);
            Subtask subtask = subtasks.get(id);
            if (subtask != null && subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
        });
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            epic.updateStatus();
            epic.updateEpicTime();
        });
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
        if (isTaskOverlappingWithOthers(task)) {
            throw new IllegalArgumentException(
                    "Невозможно добавить задачу: пересечение по времени с другой задачей (ID=" + task.getId() + ")"
            );
        }
        int id = generateId();
        task.setId(id);
        tasks.put(id, task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
        if (isTaskOverlappingWithOthers(subtask)) {
            throw new IllegalArgumentException(
                    "Невозможно добавить подзадачу: пересечение по времени с другой задачей (ID=" + subtask.getId() + ")"
            );
        }
        int id = generateId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        parentEpic.addSubtask(subtask);
        parentEpic.updateStatus();
        parentEpic.updateEpicTime();
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return subtask;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            return;
        }
        if (isTaskOverlappingWithOthers(task)) {
            throw new IllegalArgumentException(
                    "Невозможно обновить задачу: пересечение по времени с другой задачей (ID=" + task.getId() + ")"
            );
        }
        Task oldTask = tasks.get(task.getId());
        if (oldTask.getStartTime() != null) {
            prioritizedTasks.remove(oldTask);
        }
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
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
        Epic parentEpic = epics.get(existingSubtask.getEpicId());
        if (isTaskOverlappingWithOthers(newSubtask)) {
            throw new IllegalArgumentException(
                    "Невозможно обновить подзадачу: пересечение по времени с другой задачей (ID=" + newSubtask.getId() + ")");
        }
        boolean epicChanged = !Objects.equals(newSubtask.getEpicId(), existingSubtask.getEpicId());
        Epic newEpic = null;
        if (epicChanged) {
            newEpic = epics.get(newSubtask.getEpicId());
            if (newEpic == null) {
                throw new IllegalArgumentException("Эпик с ID " + newSubtask.getEpicId() + " не существует");
            }
            parentEpic.removeSubtask(existingSubtask);
            newEpic.addSubtask(newSubtask);
        }

        if (existingSubtask.getStartTime() != null) {
            prioritizedTasks.remove(existingSubtask);
        }
        if (newSubtask.getStartTime() != null) {
            prioritizedTasks.add(newSubtask);
        }
        subtasks.put(newSubtask.getId(), newSubtask);
        if (epicChanged) {
            parentEpic.updateStatus();
            parentEpic.updateEpicTime();
            newEpic.updateStatus();
            newEpic.updateEpicTime();
        } else {
            parentEpic.updateStatus();
            parentEpic.updateEpicTime();
        }
    }

    @Override
    public void deleteTask(int id) {
        Task task = tasks.remove(id);
        historyManager.remove(id);
        if (task != null && task.getStartTime() != null) {
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            if (epic.getStartTime() != null) {
                prioritizedTasks.remove(epic);
            }
            for (Subtask subtask : epic.getSubtasks()) {
                historyManager.remove(subtask.getId());
                subtasks.remove(subtask.getId());
                if (subtask.getStartTime() != null) {
                    prioritizedTasks.remove(subtask);
                }
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
            parentEpic.updateEpicTime();
            historyManager.remove(id);
            if (subtask.getStartTime() != null) {
                prioritizedTasks.remove(subtask);
            }
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



