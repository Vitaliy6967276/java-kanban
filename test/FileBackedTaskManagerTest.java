import managers.FileBackedTaskManager;
import org.junit.jupiter.api.*;
import tasks.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File tempFile;

    @Override
    FileBackedTaskManager createManager() {
        try {
            tempFile = File.createTempFile("test_", ".csv");
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать временный файл", e);
        }
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) tempFile.delete();
    }

    @Test
    void save_shouldWriteToFile() throws Exception {
        manager.generateTask(new Task("Задача", ""));
        manager.forceSaveForTests();
        assertTrue(tempFile.length() > 0);
    }
}