import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public void updateStatus() {
        if (subtasks.isEmpty()) {
            setTaskStatus(TaskStatus.NEW);
            return;
        }

        boolean allDone = true;
        boolean hasInProgress = false;

        for (Subtask subtask : subtasks) {
            TaskStatus status = subtask.getTaskStatus();

            if (status == TaskStatus.IN_PROGRESS) {
                hasInProgress = true;
            }

            if (status != TaskStatus.DONE) {
                allDone = false;
            }
        }

        if (hasInProgress) {
            setTaskStatus(TaskStatus.IN_PROGRESS);
        } else if (allDone) {
            setTaskStatus(TaskStatus.DONE);
        } else {
            setTaskStatus(TaskStatus.NEW);
        }
    }
}

