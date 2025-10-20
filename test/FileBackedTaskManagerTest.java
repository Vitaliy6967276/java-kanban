import managers.FileBackedTaskManager;
import org.junit.jupiter.api.*;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test_tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldSaveAndLoadEmptyManager() throws IOException {
        // Сохраняем пустое состояние
        manager.save();

        // Проверяем, что файл создан и содержит только заголовок
        String content = Files.readString(tempFile.toPath());
        assertEquals("id,type,name,status,description,epic\n", content);

        // Загружаем из пустого файла (должен остаться пустым)
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubtasks().isEmpty());
    }

    @Test
    void shouldSaveAndLoadSingleTask() {
        Task task = new Task("Задача 1", "Описание 1");
        manager.generateTask(task);

        // Перезагружаем менеджер из файла
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loaded.getAllTasks();
        assertEquals(1, tasks.size());
        Task loadedTask = tasks.get(0);
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(TaskStatus.NEW, loadedTask.getTaskStatus());
    }

    @Test
    void shouldSaveAndLoadEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика");
        manager.generateEpic(epic);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Epic> epics = loaded.getAllEpics();
        assertEquals(1, epics.size());
        Epic loadedEpic = epics.get(0);
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(TaskStatus.NEW, loadedEpic.getTaskStatus());
    }

    @Test
    void shouldSaveAndLoadSubtaskWithEpicId() {
        Epic epic = new Epic("Родительский эпик", "Описание");
        manager.generateEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic.getId());
        manager.generateSubtask(subtask);

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        List<Subtask> subtasks = loaded.getAllSubtasks();
        assertEquals(1, subtasks.size());

        Subtask loadedSubtask = subtasks.get(0);
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(epic.getId(), loadedSubtask.getEpicId());  // Ключевая проверка!
    }
}
