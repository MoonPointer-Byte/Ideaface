package com.example.interview_agent.config;

import com.example.interview_agent.entity.*;
import com.example.interview_agent.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private LearningPathRepository learningPathRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        User testUser = null;
        if (userRepository.count() == 0) {
            testUser = new User("testuser", "test@example.com", passwordEncoder.encode("password123"));
            userRepository.save(testUser);
        } else {
            testUser = userRepository.findByUsername("testuser").orElse(null);
        }

        Question question1 = null;
        if (questionRepository.count() == 0) {
            question1 = new Question("解释一下什么是RESTful API?", "请说明其核心原则，如无状态、统一接口等。", "backend-developer", "简单");
            Question question2 = new Question("在Java中，HashMap和ConcurrentHashMap有什么区别？", "请从线程安全、锁机制和底层数据结构角度回答。", "backend-developer", "中等");
            Question question3 = new Question("什么是神经网络中的梯度消失问题？", "有哪些常见的解决方法，如ReLU激活函数、Batch Normalization等？", "ai-engineer", "中等");
            Question question4 = new Question("请描述一下你对用户体验的理解。", "并举例说明一个你认为用户体验做得好的App及其原因。", "product-manager", "简单");
            questionRepository.saveAll(List.of(question1, question2, question3, question4));
        } else {
            question1 = questionRepository.findById(1L).orElse(null);
        }

        if (learningPathRepository.count() == 0) {
            learningPathRepository.saveAll(List.of(
                    new LearningPath("AI工程师入门到精通", "从Python基础、数学原理，到深度学习框架和项目实战的完整路径。"),
                    new LearningPath("后端开发专家之路", "覆盖Java、Spring、数据库、缓存、消息队列和分布式系统核心技术。")
            ));
        }

        if (courseRepository.count() == 0) {
            courseRepository.saveAll(List.of(
                    new Course("吴恩达机器学习", "斯坦福大学经典机器学习课程，AI领域必修课。", "https://www.coursera.org/learn/machine-learning", "Coursera", "ai-engineer"),
                    new Course("尚硅谷JavaWeb教程", "全面覆盖JavaWeb核心技术，适合Java后端入门。", "https://www.bilibili.com/video/BV1Y7411K7zz", "Bilibili", "backend-developer"),
                    new Course("人人都是产品经理", "产品经理入门必读，系统性建立产品思维。", "https://www.woshipm.com/", "Website", "product-manager")
            ));
        }

        if (commentRepository.count() == 0 && testUser != null && question1 != null) {
            Comment comment1 = new Comment();
            comment1.setContent("这个问题很经典，回答时一定要突出HTTP方法和状态码的意义。");
            comment1.setCreateTime(LocalDateTime.now().minusDays(1));
            comment1.setUser(testUser);
            comment1.setQuestion(question1);

            Comment comment2 = new Comment();
            comment2.setContent("补充一下，还可以谈谈幂等性这个概念，这是加分项！");
            comment2.setCreateTime(LocalDateTime.now());
            comment2.setUser(testUser);

            comment2.setQuestion(question1);

            commentRepository.saveAll(List.of(comment1, comment2));
        }
    }
}