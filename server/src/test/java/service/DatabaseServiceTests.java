package service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseServiceTests {

    @Test
    public void clearDB() {
        // Clear the database
        assertDoesNotThrow(DatabaseService::clearDatabase);
    }
}
