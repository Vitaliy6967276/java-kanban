package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        updateEpicTime();
        updateStatus();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void addSubtask(Subtask subtask) {
        if (subtask.getId() == this.getId()) {
            throw new IllegalArgumentException("Подзадача не может иметь тот же ID, что и эпик");
        }
        subtasks.add(subtask);
        updateEpicTime();
        updateStatus();
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks);
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        updateEpicTime();
        updateStatus();
    }

    public void clearSubtasks() {
        subtasks.clear();
        updateEpicTime();
        updateStatus();
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

    public void updateEpicTime() {
        if (subtasks.isEmpty()) {
            super.setDuration(null);
            super.setStartTime(null);
            this.endTime = null;
            return;
        }
        Duration total = Duration.ZERO;
        for (Subtask subtask : subtasks) {
            if (subtask.getDuration() != null) {
                total = total.plus(subtask.getDuration());
            }
        }

        LocalDateTime earliestStart = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        super.setDuration(total);
        super.setStartTime(earliestStart);
        this.endTime = latestEnd;
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

