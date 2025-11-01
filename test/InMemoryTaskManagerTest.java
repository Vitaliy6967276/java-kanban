import managers.InMemoryTaskManager;
import org.junit.jupiter.api.*;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {


    @Override
    InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }


    @Test
    void generateEpic_shouldHaveNoSubtasksInitially() {
        Epic epic = manager.generateEpic(new Epic("Эпик", ""));
        assertEquals(0, manager.getSubtasksByEpic(epic.getId()).size());
    }

    @Test
    void updateEpic_shouldPreserveSubtasks() {
        Epic epic = manager.generateEpic(new Epic("Старый", ""));
        Subtask subtask = manager.generateSubtask(
                new Subtask("Подзадача", "", epic.getId()));

        Epic updatedEpic = new Epic("Новый", "Описание");
        updatedEpic.setId(epic.getId());
        manager.updateEpic(updatedEpic);

        assertEquals(1, manager.getSubtasksByEpic(epic.getId()).size());
        assertEquals(subtask.getId(),
                manager.getSubtasksByEpic(epic.getId()).get(0).getId());
    }

    @Test
    void deleteEpic_shouldRemoveAllSubtasks() {
        Epic epic = manager.generateEpic(new Epic("Эпик", ""));
        Subtask s1 = manager.generateSubtask(new Subtask("S1", "", epic.getId()));
        Subtask s2 = manager.generateSubtask(new Subtask("S2", "", epic.getId()));

        manager.deleteEpic(epic.getId());

        assertNull(manager.getSubtaskById(s1.getId()));
        assertNull(manager.getSubtaskById(s2.getId()));
        assertNull(manager.getEpicById(epic.getId()));
    }

    @Test
    void getPrioritizedTasks_shouldSortByStartTime() {
        LocalDateTime t1Time = LocalDateTime.of(2025, 10, 1, 12, 0);
        LocalDateTime t2Time = LocalDateTime.of(2025, 10, 1, 10, 0);

        Task t1 = new Task("Поздняя", "", TaskStatus.NEW, Duration.ofHours(1), t1Time);
        Task t2 = new Task("Ранняя", "", TaskStatus.NEW, Duration.ofHours(1), t2Time);


        manager.generateTask(t1);
        manager.generateTask(t2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(t2.getId(), prioritized.get(0).getId()); // Ранняя задача первая
        assertEquals(t1.getId(), prioritized.get(1).getId()); // Поздняя задача вторая
    }
}