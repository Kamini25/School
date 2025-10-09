// package com.example.School;

// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

// @SpringBootTest
// class SchoolApplicationTests {

// 	@Test
// 	void contextLoads() {
// 	}

// }

package com.example.School;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/api/test/usser")
    public String user() {
        return "Hello, authenticated user!";
    }
}
