public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        Task task1 = manager.generateTask(new Task("Задача 1", "Описание задачи 1"));
        Task task2 = manager.generateTask(new Task("Задача 2", "Описание задачи 2"));
        Epic epic1 = manager.generateEpic(new Epic("Эпик 1", "Описание эпика 1"));
        Subtask subtask1 = manager.generateSubtask(new Subtask("Подзадача 1.1", "Описание подзадачи 1.1", epic1.getId()));
        Subtask subtask2 = manager.generateSubtask(new Subtask("Подзадача 1.2", "Описание подзадачи 1.2", epic1.getId()));
        Epic epic2 = manager.generateEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Subtask subtask3 = manager.generateSubtask(new Subtask("Подзадача 2.1", "Описание подзадачи 2.1", epic2.getId()));

        System.out.println("Все задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println("- " + task.getName() + " (ID: " + task.getId() + ")");
        }
        System.out.println("\nВсе эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println("- " + epic.getName() + " (ID: " + epic.getId() + ")");
        }
        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println("- " + subtask.getName() + " (ID: " + subtask.getId() + ")");
        }
        task1.setTaskStatus(TaskStatus.IN_PROGRESS);
        subtask1.setTaskStatus(TaskStatus.DONE);
        subtask2.setTaskStatus(TaskStatus.IN_PROGRESS);
        subtask3.setTaskStatus(TaskStatus.DONE);
        System.out.println("\nПосле изменения статусов:");
        System.out.println("Статус задачи 1: " + task1.getTaskStatus());
        System.out.println("Статус подзадачи 1.1: " + subtask1.getTaskStatus());
        System.out.println("Статус подзадачи 1.2: " + subtask2.getTaskStatus());
        System.out.println("Статус эпика 1: " + epic1.getTaskStatus());
        System.out.println("Статус подзадачи 2.1: " + subtask3.getTaskStatus());
        System.out.println("Статус эпика 2: " + epic2.getTaskStatus());
        System.out.println("\nУдаляем задачу 2 и эпик 1");
        manager.deleteTask(task2.getId());
        manager.deleteEpic(epic1.getId());
        System.out.println("\nПосле удаления:");
        System.out.println("Все задачи: " + manager.getAllTasks().size());
        System.out.println("Все эпики: " + manager.getAllEpics().size());
        System.out.println("Все подзадачи: " + manager.getAllSubtasks().size());
    }
}

