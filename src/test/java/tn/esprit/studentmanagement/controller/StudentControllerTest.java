package tn.esprit.studentmanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
    class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testApplicationStarts() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void testGetAllStudents() {
        String response = restTemplate.getForObject("http://localhost:" + port + "/api/students", String.class);
        assertThat(response).isNotNull();
    }

    @Test
    void testStudentEndpointExists() {
        String response = restTemplate.getForObject("http://localhost:" + port + "/api/students", String.class);
        assertThat(response).isNotNull();
    }
}
