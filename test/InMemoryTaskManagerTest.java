import static org.junit.jupiter.api.Assertions.*;

import Managers.InMemoryTaskManager;
import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;
import Tasks.TaskStatus;
import org.junit.jupiter.api.*;

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
}