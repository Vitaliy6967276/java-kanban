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
    }

    @Override
    public Duration getDuration() {
        Duration total = Duration.ZERO;
        for (Subtask subtask : subtasks) {
            if (subtask.getDuration() != null) {
                total = total.plus(subtask.getDuration());
            }
        }
        return total;
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(startTime -> startTime != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);
    }

    @Override
    public LocalDateTime getEndTime() {
        return subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
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
            this.endTime = null;
            return;
        }

        boolean allDone = true;
        boolean hasInProgress = false;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;

        for (Subtask subtask : subtasks) {
            TaskStatus status = subtask.getTaskStatus();

            if (status == TaskStatus.IN_PROGRESS) {
                hasInProgress = true;
            }

            if (status != TaskStatus.DONE) {
                allDone = false;
            }
            LocalDateTime start = subtask.getStartTime();
            LocalDateTime end = subtask.getEndTime();
            if (start != null) {
                if (earliestStart == null || start.isBefore(earliestStart)) {
                    earliestStart = start;
                }
            }
            if (end != null) {
                if (latestEnd == null || end.isAfter(latestEnd)) {
                    latestEnd = end;
                }
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

