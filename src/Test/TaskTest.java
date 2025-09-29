import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

class TaskTest {

    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("Задача 1", "Описание 1");
        Task task2 = new Task("Задача 2", "Описание 2");

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2);
    }
}