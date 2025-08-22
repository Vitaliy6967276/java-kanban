public class Subtask extends Task{
    private final Epic epicParent;
    public Subtask(String name, String description, Epic epicParent) {
        super(name, description);
        this.epicParent = epicParent;
    }
    @Override
    public void setTaskStatus(TaskStatus taskStatus) {
        super.setTaskStatus(taskStatus);
        if (epicParent != null) {
            epicParent.updateStatus();
        }
    }
    public Epic getEpicParent() {
        return epicParent;
    }
}

