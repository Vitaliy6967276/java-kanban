import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
class SubtaskTest {

    @Test
    void testSubtaskEqualityById() {
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1", 1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2", 1);

        subtask1.setId(1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2);
    }

}