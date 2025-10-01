import static org.junit.jupiter.api.Assertions.*;

import tasks.Epic;
import tasks.Subtask;
import org.junit.jupiter.api.*;

class EpicTest {

    @Test
    void testEpicEqualityById() {
        Epic epic1 = new Epic("Эпик 1", "Описание 1");
        Epic epic2 = new Epic("Эпик 2", "Описание 2");

        epic1.setId(1);
        epic2.setId(1);

        assertEquals(epic1, epic2);
    }

    @Test
    void testEpicCannotAddSelfAsSubtask() {
        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(1);
        Subtask selfSubtask = new Subtask("Подзадача", "Описание", epic.getId());
        selfSubtask.setId(epic.getId());
        assertThrows(IllegalArgumentException.class, () -> {
            epic.addSubtask(selfSubtask);
        });
    }
}