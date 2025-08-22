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
        return subtasks;
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

            switch (status) {
                case NEW:
                    allDone = false;
                    hasInProgress = false;
                    break;

                case IN_PROGRESS:
                    allDone = false;
                    hasInProgress = true;
                    break;

                case DONE:
                    break;
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

