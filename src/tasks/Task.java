package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {

    protected String name;
    protected String description;
    protected Integer id;
    protected TaskStatus taskStatus;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this(name, description, TaskStatus.NEW, null, null);
    }

    public Task(String name, String description, TaskStatus taskStatus) {
        this(name, description, taskStatus, null, null);
    }

    public Task(String name, String description, TaskStatus taskStatus,
                Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.taskStatus = taskStatus;
        this.duration = duration;
        this.startTime = startTime;
    }

    public boolean isOverlapping(Task other) {
        if (this.startTime == null || this.duration == null ||
                other.startTime == null || other.duration == null) {
            return false;
        }
        LocalDateTime thisEnd = this.getEndTime();
        LocalDateTime otherEnd = other.getEndTime();
        return this.startTime.isBefore(otherEnd) && other.startTime.isBefore(thisEnd);
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s\n" +
                        "Название: %s\n" +
                        "Описание: %s\n" +
                        "Статус: %s\n",
                id, name, description, taskStatus
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return getId() == task.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
