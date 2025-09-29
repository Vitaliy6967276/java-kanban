import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.util.List;
class InMemoryTaskManagerTest {

    private InMemoryTaskManager manager;
    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void testAddAndFindTasks() {
        Task task = manager.generateTask(new Task("Задача", "Описание"));
        Epic epic = manager.generateEpic(new Epic("Эпик", "Описание"));
        Subtask subtask = manager.generateSubtask(new Subtask("Подзадача", "Описание", epic.getId()));

        assertNotNull(manager.getTaskById(task.getId()));
        assertNotNull(manager.getEpicById(epic.getId()));
        assertNotNull(manager.getSubtaskById(subtask.getId()));
    }

    @Test
    void testIdConflicts() {
        Task task1 = new Task("Задача", "Описание");
        task1.setId(1);
        manager.generateTask(task1);

        Task task2 = new Task("Другая задача", "Другое описание");
        task2.setId(2);
        manager.generateTask(task2);

        assertEquals(task1, manager.getTaskById(1));
        assertEquals(task2, manager.getTaskById(2));
    }
    @Test
    void testTaskImmutability() {
        Task original = new Task("Задача", "Описание");
        original.setTaskStatus(TaskStatus.IN_PROGRESS);

        Task added = manager.generateTask(original);

        assertEquals(original.getName(), added.getName());
        assertEquals(original.getDescription(), added.getDescription());
        assertEquals(original.getTaskStatus(), added.getTaskStatus());
    }
    @Test
    void testHistoryNoDuplicates() {
        Task task = manager.generateTask(new Task("Задача", "Описание"));

        // Посещаем задачу несколько раз
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size()); // Должен быть только один элемент
        assertEquals(task, history.get(0));
    }
    @Test
    void testHistoryReorder() {
        Task task1 = manager.generateTask(new Task("Задача 1", "Описание 1"));
        Task task2 = manager.generateTask(new Task("Задача 2", "Описание 2"));

        // Первое посещение
        manager.getTaskById(task1.getId());
        List<Task> history = manager.getHistory();
        System.out.println("После первого посещения: " + history);
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));

        // Второе посещение
        manager.getTaskById(task2.getId());
        history = manager.getHistory();
        System.out.println("После второго посещения: " + history);
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0)); // Задача 2 должна быть первой
        assertEquals(task1, history.get(1));

        // Повторное посещение task1
        manager.getTaskById(task1.getId());
        history = manager.getHistory();
        System.out.println("После повторного посещения task1: " + history);
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0)); // Теперь task1 должен быть первым
        assertEquals(task2, history.get(1));
    }
    @Test
    void testHistoryRemoval() {
        Task task = manager.generateTask(new Task("Задача", "Описание"));
        manager.getTaskById(task.getId());

        // Получаем текущую историю
        List<Task> history = manager.getHistory();

        // Находим ID задачи в истории
        int taskId = task.getId();

        // Удаляем задачу из истории через менеджер задач
        manager.deleteTask(taskId);

        // Проверяем, что история пуста
        List<Task> newHistory = manager.getHistory();
        assertTrue(newHistory.isEmpty());
    }

}