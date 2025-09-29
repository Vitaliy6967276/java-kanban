import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public void addSubtask(Subtask subtask) {
        if (subtask.getId() == this.getId()) {
            throw new IllegalArgumentException("Подзадача не может иметь тот же ID, что и эпик");
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return getId() == epic.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

