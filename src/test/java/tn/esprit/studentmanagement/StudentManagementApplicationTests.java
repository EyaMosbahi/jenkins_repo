package tn.esprit.studentmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class StudentManagementApplicationTests {

    @Test
    void contextLoads() {
        // Vérification que le contexte Spring se charge correctement
        assertTrue(true, "Application context should load successfully");
    }

}
