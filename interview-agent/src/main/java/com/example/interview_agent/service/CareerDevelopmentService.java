package com.example.interview_agent.service;

import com.example.interview_agent.client.SparkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 负责处理与职业发展路径规划相关的业务逻辑。
 * 该服务接收用户的当前状态和目标，并利用AI大模型生成个性化的发展规划。
 */
@Service
public class CareerDevelopmentService {

    private static final Logger log = LoggerFactory.getLogger(CareerDevelopmentService.class);

    @Autowired
    private SparkClient sparkClient;


    public String generatePlan(String currentSkills, String targetPositionId) {
        log.info("开始为目标岗位 '{}' 生成职业发展规划...", targetPositionId);

        // 1. 获取目标岗位的详细信息
        String targetPositionTitle = getPositionTitleFor(targetPositionId);
        String targetPositionDescription = getPositionDescriptionFor(targetPositionId);

        // 2. 构建一个高质量的、面向职业发展的Prompt
        String prompt = buildCareerPlanPrompt(currentSkills, targetPositionTitle, targetPositionDescription);
        log.debug("生成发展规划的Prompt已构建。");

        // 3. 调用SparkClient与AI交互，并处理潜在的异常
        try {
            log.info("正在调用SparkClient来生成规划...");
            String plan = sparkClient.askQuestion(prompt);
            log.info("成功从AI获取职业发展规划。");
            return plan;
        } catch (Exception e) {
            log.error("调用SparkClient生成发展规划时发生严重错误", e);
            // 将底层异常包装成一个更通用的运行时异常，方便上层统一处理
            throw new RuntimeException("AI服务在规划未来时遇到了点麻烦，请稍后再试。", e);
        }
    }

    /**
     * 构建一个高质量的、面向职业发展的Prompt。
     * 这是实现“智能”规划的核心，通过详细的指令引导AI产出结构化、有价值的内容。
     *
     * @param currentSkills           用户的当前技能描述。
     * @param targetPositionTitle     目标岗位的名称 (例如 "人工智能工程师")。
     * @param targetPositionDescription 目标岗位的核心要求。
     * @return 准备发送给AI的完整Prompt字符串。
     */
    private String buildCareerPlanPrompt(String currentSkills, String targetPositionTitle, String targetPositionDescription) {
        return String.format(
                "你是一位顶级的职业规划导师和资深技术总监，你的任务是为一位用户提供一份从现状到目标的、高度个性化且可执行的职业发展路径。\n\n" +
                        "---用户背景---\n" +
                        "1.  **用户当前技能与经验**:\n---\n%s\n---\n" +
                        "2.  **用户的目标岗位**: %s\n" +
                        "3.  **目标岗位核心要求**: %s\n\n" +
                        "---你的任务与指令---\n" +
                        "请严格按照下面的Markdown格式，为用户输出一份详细的职业发展规划报告。报告必须有深度、有条理，且所有建议都必须紧密围绕用户的现状和目标。\n\n" +
                        "### 1. 核心能力差距分析 (Skill Gap Analysis)\n" +
                        "(精准识别用户当前技能与目标岗位要求之间的核心差距，分点列出，例如：缺乏大规模分布式系统经验、工程化能力不足等。)\n\n" +
                        "### 2. 定制化学习路径 (Customized Learning Path)\n" +
                        "#### **理论知识强化**\n" +
                        "(推荐2-3本必读的核心经典书籍或高质量的技术博客/专栏，并说明为什么推荐它们。)\n" +
                        "#### **在线课程/认证**\n" +
                        "(推荐1-2个高质量的在线课程平台（如Coursera, Udacity, B站等）上的具体课程，最好附上链接，并说明该课程能弥补哪个技能短板。)\n\n" +
                        "### 3. 实战项目建议 (Practical Project Suggestions)\n" +
                        "(设计1-2个可以放入个人GitHub的、能充分体现目标岗位能力的实战项目。项目描述要具体，包含技术选型建议和要实现的核心功能点。)\n\n" +
                        "### 4. 职业发展阶梯 (Career Ladder Suggestions)\n" +
                        "(如果从当前到目标岗位跨度较大，建议一个或两个可以作为跳板的中间职位，并说明该职位能帮助积累哪些关键经验。)\n\n" +
                        "### 5. 总结与鼓励\n" +
                        "(用一段富有洞察力和鼓励性的话语作为结尾，激励用户开始行动。)",
                currentSkills, targetPositionTitle, targetPositionDescription
        );
    }

    /**
     * 根据岗位ID获取岗位名称。
     * 与项目中其他服务保持一致，确保数据同源。
     */
    private String getPositionTitleFor(String positionId) {
        return switch (positionId) {
            case "ai-engineer" -> "人工智能工程师";
            case "backend-developer" -> "后端开发工程师";
            case "product-manager" -> "产品经理";
            case "frontend-developer" -> "前端开发工程师";
            case "algorithm-engineer" -> "算法工程师";
            case "qa-engineer" -> "测试工程师";
            case "big-data-engineer" -> "大数据开发工程师";
            case "devops-engineer" -> "运维工程师";
            default -> "通用岗位";
        };
    }

    /**
     * 根据岗位ID获取岗位核心描述。
     * 与项目中其他服务保持一致，确保数据同源。
     */
    private String getPositionDescriptionFor(String positionId) {
        return switch (positionId) {
            case "ai-engineer" -> "熟练掌握Python，熟悉至少一种深度学习框架（如PyTorch, TensorFlow），对常用的机器学习算法（如LR, GBDT, CNN, RNN）有深入理解，具备优秀的算法设计和实现能力。";
            case "backend-developer" -> "精通Java或Go语言，深入理解Spring Boot/Spring Cloud或Gin等主流框架，熟悉MySQL/PostgreSQL和Redis/MongoDB，具备高并发、高可用系统设计能力，熟悉容器化技术（Docker, K8s）。";
            case "product-manager" -> "具备出色的用户洞察和市场分析能力，能够独立完成市场调研、竞品分析、需求文档撰写（PRD），逻辑思维清晰，善于沟通协调，能够驱动项目顺利上线。";
            case "frontend-developer" -> "精通JavaScript/TypeScript，熟悉Vue或React框架，了解前端工程化、性能优化和浏览器原理。";
            case "algorithm-engineer" -> "扎实的算法与数据结构基础，精通至少一门编程语言（如C++, Python），有ACM/ICPC等竞赛经验者优先。";
            case "qa-engineer" -> "熟悉软件测试理论和流程，掌握至少一种自动化测试框架（如Selenium, Appium），具备接口测试、性能测试经验。";
            case "big-data-engineer" -> "熟悉Hadoop生态（HDFS, MapReduce, Hive, Spark），具备大规模数据处理和分析系统开发经验。";
            case "devops-engineer" -> "熟悉Linux操作系统，精通Shell/Python脚本，熟练使用CI/CD工具（Jenkins, GitLab CI），了解Docker和Kubernetes。";
            default -> "具备良好的沟通能力、逻辑思维能力、学习能力和团队合作精神。";
        };
    }
}