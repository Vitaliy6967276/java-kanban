import managers.TaskManager;
import tasks.*;
import org.junit.jupiter.api.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    void epicStatus_allNew_shouldBeNew() {
        Epic epic = manager.generateEpic(new Epic("Эпик", ""));
        Subtask s1 = new Subtask("S1", "", epic.getId());
        s1.setTaskStatus(TaskStatus.NEW);
        Subtask s2 = new Subtask("S2", "", epic.getId());
        s2.setTaskStatus(TaskStatus.NEW);
        manager.generateSubtask(s1);
        manager.generateSubtask(s2);

        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getTaskStatus());
    }

    @Test
    void epicStatus_allDone_shouldBeDone() {
        Epic epic = manager.generateEpic(new Epic("Эпик", ""));
        Subtask s1 = new Subtask("S1", "", epic.getId());
        s1.setTaskStatus(TaskStatus.DONE);
        Subtask s2 = new Subtask("S2", "", epic.getId());
        s2.setTaskStatus(TaskStatus.DONE);
        manager.generateSubtask(s1);
        manager.generateSubtask(s2);

        assertEquals(TaskStatus.DONE, manager.getEpicById(epic.getId()).getTaskStatus());
    }

    @Test
    void epicStatus_mixedNewDone_shouldBeNew() {
        Epic epic = manager.generateEpic(new Epic("Эпик", ""));
        Subtask s1 = new Subtask("S1", "", epic.getId());
        s1.setTaskStatus(TaskStatus.NEW);
        Subtask s2 = new Subtask("S2", "", epic.getId());
        s2.setTaskStatus(TaskStatus.DONE);
        manager.generateSubtask(s1);
        manager.generateSubtask(s2);

        assertEquals(TaskStatus.NEW, manager.getEpicById(epic.getId()).getTaskStatus());
    }

    @Test
    void epicStatus_anyInProgress_shouldBeInProgress() {
        Epic epic = manager.generateEpic(new Epic("Эпик", ""));
        Subtask s = new Subtask("S", "", epic.getId());
        s.setTaskStatus(TaskStatus.IN_PROGRESS);
        manager.generateSubtask(s);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicById(epic.getId()).getTaskStatus());
    }

    @Test
    void subtask_shouldHaveEpicId() {
        Epic epic = manager.generateEpic(new Epic("Родитель", ""));
        Subtask subtask = new Subtask("Подзадача", "", epic.getId());
        Subtask saved = manager.generateSubtask(subtask);

        assertEquals(epic.getId(), saved.getEpicId());
    }

    @Test
    void taskIntervals_shouldNotOverlap() {
        LocalDateTime start1 = LocalDateTime.of(2025, 10, 1, 10, 0);
        Duration dur1 = Duration.ofHours(2);
        LocalDateTime start2 = LocalDateTime.of(2025, 10, 1, 11, 0); // пересекается

        Task t1 = new Task("T1", "", TaskStatus.NEW, dur1, start1);
        manager.generateTask(t1);

        Task t2 = new Task("T2", "", TaskStatus.NEW, Duration.ofHours(1), start2);
        assertThrows(IllegalArgumentException.class, () -> manager.generateTask(t2));
    }

    @Test
    void taskIntervals_nonOverlapping_shouldAllow() {
        LocalDateTime start1 = LocalDateTime.of(2025, 10, 1, 10, 0);
        Duration dur1 = Duration.ofHours(2);
        LocalDateTime start2 = LocalDateTime.of(2025, 10, 1, 13, 0); // не пересекается

        Task t1 = new Task("T1", "", TaskStatus.NEW, dur1, start1);
        manager.generateTask(t1);

        Task t2 = new Task("T2", "", TaskStatus.NEW, Duration.ofHours(1), start2);
        assertDoesNotThrow(() -> manager.generateTask(t2));
    }
}