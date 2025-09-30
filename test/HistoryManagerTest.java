import static org.junit.jupiter.api.Assertions.*;

import managers.InMemoryTaskManager;
import tasks.Task;
import org.junit.jupiter.api.*;

import java.util.List;

class HistoryManagerTest {
    private InMemoryTaskManager manager;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        task1 = manager.generateTask(new Task("Задача 1", "Описание 1"));
        task2 = manager.generateTask(new Task("Задача 2", "Описание 2"));
    }

    @Test
    void testHistoryNoDuplicates() {
        manager.getTaskById(task1.getId());
        manager.getTaskById(task1.getId());
        manager.getTaskById(task1.getId());

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void testHistoryRemoval() {
        manager.getTaskById(task1.getId());
        List<Task> history = manager.getHistory();
        manager.deleteTask(task1.getId());
        List<Task> newHistory = manager.getHistory();
        assertTrue(newHistory.isEmpty());
    }

    @Test
    void testHistoryReorder() {
        // Первое посещение task1
        manager.getTaskById(task1.getId());
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));

        // Второе посещение task2
        manager.getTaskById(task2.getId());
        history = manager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(1)); // task2 должен быть первым
        assertEquals(task1, history.get(0)); // task1 должен быть вторым

        // Повторное посещение task1
        manager.getTaskById(task1.getId());
        history = manager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(1)); // теперь task1 должен быть первым
        assertEquals(task2, history.get(0)); // task2 должен быть вторым
    }
}
