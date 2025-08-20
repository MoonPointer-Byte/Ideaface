package com.example.interview_agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest // 告诉Spring Boot这是一个集成测试，会加载完整的应用上下文
@AutoConfigureMockMvc // 自动配置MockMvc，这是我们用来模拟HTTP请求的核心对象
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc; // 注入MockMvc

    @Test
    void testSearchQuestions_whenNoParams_shouldReturnAll() throws Exception {
        // 测试不带任何参数的情况
        mockMvc.perform(get("/api/questions"))
                .andExpect(status().isOk()) // 期望HTTP状态码是200 (OK)
                .andExpect(jsonPath("$", hasSize(4))); // 期望返回的JSON数组长度为4
    }

    @Test
    void testSearchQuestions_whenValidId_shouldReturnOne() throws Exception {
        // 测试有效的ID
        mockMvc.perform(get("/api/questions").param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // 期望数组长度为1
                .andExpect(jsonPath("$[0].title", is("在Java中，HashMap和ConcurrentHashMap有什么区别？"))); // 期望返回的第一个元素的title正确
    }

//    @Test
//    void testSearchQuestions_whenValidDifficulty_shouldReturnMatching() throws Exception {
//        // 测试有效的难度（不区分大小写）
//        mockMvc.perform(get("/api/questions").param("difficulty", "hard"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].id", is(2))); // 期望返回的题目ID是3
//    }

//    @Test
//    void testSearchQuestions_whenIdAndDifficultyMatch_shouldReturnOne() throws Exception {
//        // 测试ID和难度组合匹配
//        mockMvc.perform(get("/api/questions").param("id", "4").param("difficulty", "Easy"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(1)))
//                .andExpect(jsonPath("$[0].id", is(4)));
//    }
//
//    @Test
//    void testSearchQuestions_whenIdAndDifficultyNotMatch_shouldReturnEmpty() throws Exception {
//        // 测试ID和难度组合不匹配
//        mockMvc.perform(get("/api/questions").param("id", "1").param("difficulty", "Hard"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(0))); // 期望返回空数组
//    }
}