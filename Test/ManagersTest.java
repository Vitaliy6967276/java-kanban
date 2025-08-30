import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
class ManagersTest {
    @Test
    void testManagersUtilityClass() {
        HistoryManager history = Managers.getDefaultHistory();

        assertNotNull(history);
        assertTrue(history instanceof HistoryManager);
        assertSame(Managers.getDefaultHistory(), history);
    }
}