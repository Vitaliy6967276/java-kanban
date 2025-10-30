import history.HistoryManager;
import history.InMemoryHistoryManager;
import tasks.Task;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void getHistory_empty_shouldReturnEmptyList() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой при инициализации");
        assertEquals(0, history.size());
    }

    @Test
    void add_singleTask_shouldBeInHistory() {
        Task task = new Task("Задача 1", "Описание");
        task.setId(1);
        historyManager.add(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "В истории должна быть 1 задача");
        assertEquals(task.getId(), history.get(0).getId());
    }

    @Test
    void add_multipleTasks_shouldFollowLRUOrder() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        Task t2 = new Task("T2", "");
        t2.setId(2);
        Task t3 = new Task("T3", "");
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(t1.getId(), history.get(0).getId()); // старейшая
        assertEquals(t2.getId(), history.get(1).getId());
        assertEquals(t3.getId(), history.get(2).getId()); // самая новая
    }

    @Test
    void add_duplicateTask_shouldUpdatePosition() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        Task t2 = new Task("T2", "");
        t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t1); // повтор t1


        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t2.getId(), history.get(0).getId()); // t2 теперь старейшая
        assertEquals(t1.getId(), history.get(1).getId()); // t1 — самая новая
    }

    @Test
    void remove_fromBeginning_shouldRemoveAndPreserveOrder() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        Task t2 = new Task("T2", "");
        t2.setId(2);
        Task t3 = new Task("T3", "");
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);
        historyManager.remove(t1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t2.getId(), history.get(0).getId());
        assertEquals(t3.getId(), history.get(1).getId());
    }

    @Test
    void remove_fromMiddle_shouldRemoveAndPreserveOrder() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        Task t2 = new Task("T2", "");
        t2.setId(2);
        Task t3 = new Task("T3", "");
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);
        historyManager.remove(t2.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t1.getId(), history.get(0).getId());
        assertEquals(t3.getId(), history.get(1).getId());
    }

    @Test
    void remove_fromEnd_shouldRemove() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        Task t2 = new Task("T2", "");
        t2.setId(2);
        Task t3 = new Task("T3", "");
        t3.setId(3);

        historyManager.add(t1);
        historyManager.add(t2);
        historyManager.add(t3);
        historyManager.remove(t3.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(t1.getId(), history.get(0).getId());
        assertEquals(t2.getId(), history.get(1).getId());
    }

    @Test
    void remove_nonExistingId_shouldDoNothing() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        historyManager.add(t1);

        historyManager.remove(999); // id нет в истории

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(t1.getId(), history.get(0).getId());
    }

    @Test
    void remove_allTasks_shouldClearHistory() {
        Task t1 = new Task("T1", "");
        t1.setId(1);
        Task t2 = new Task("T2", "");
        t2.setId(2);

        historyManager.add(t1);
        historyManager.add(t2);

        historyManager.remove(t1.getId());
        historyManager.remove(t2.getId());

        assertTrue(historyManager.getHistory().isEmpty());
    }
}