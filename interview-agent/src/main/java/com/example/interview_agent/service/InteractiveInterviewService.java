package com.example.interview_agent.service;

import com.example.interview_agent.client.SparkClient;
import com.example.interview_agent.dto.BehavioralData;
import com.example.interview_agent.dto.EmotionData;
import com.example.interview_agent.entity.InterviewReport;
import com.example.interview_agent.repository.InterviewReportRepository;
import com.example.interview_agent.repository.InterviewSessionRepository;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger; // 1. 手动导入 Logger
import org.slf4j.LoggerFactory; // 2. 手动导入 LoggerFactory
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InteractiveInterviewService {

    // 3. 手动创建名为 log 的日志记录器实例
    private static final Logger log = LoggerFactory.getLogger(InteractiveInterviewService.class);

    // 您的原始代码中也有一个名为logger的实例，为保持一致性，我们都使用log。
    // 如果您想继续使用logger，请将本文件所有 log.info() 等调用改为 logger.info()

    private final InterviewSessionRepository sessionRepository;
    private final InterviewReportRepository reportRepository;

    @Autowired
    private SparkClient sparkClient;

    @Autowired
    private InterviewAnalysisService interviewAnalysisService;

    private final Map<String, InterviewSession> sessions = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public InteractiveInterviewService(InterviewSessionRepository sessionRepository, InterviewReportRepository reportRepository) {
        this.sessionRepository = sessionRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public InterviewSession startInterview(String sessionId, String positionId) throws Exception {
        log.info("[{}] 服务层: 准备为岗位 '{}' 生成面试题。", sessionId, positionId);

        InterviewSession session = getOrCreateSession(sessionId, positionId);

        if (!positionId.equals(session.getPositionId())) {
            log.warn("[{}] 会话的岗位ID与请求不符，更新为: {}", sessionId, positionId);
            session.setPositionId(positionId);
        }

        String resumeText = session.getResumeText();
        if (!StringUtils.hasText(resumeText)) {
            log.error("[{}] 致命错误：尝试生成问题，但会话中没有简历文本。", sessionId);
            throw new IllegalStateException("无法生成面试题，因为会话中缺少简历信息。");
        }

        String positionTitle = getPositionTitleFor(positionId);
        String positionDescription = getPositionDescriptionFor(positionId);
        String summarizedResume = resumeText.length() > 2500 ? resumeText.substring(0, 2500) + "..." : resumeText;

        if (reportRepository.findBySessionId(sessionId).isEmpty()) {
            InterviewReport report = new InterviewReport();
            report.setSessionId(sessionId);
            report.setPositionId(positionId);
            report.setPositionTitle(positionTitle);
            report.setCreateTime(LocalDateTime.now());
            report.setStatus("ONGOING");
            reportRepository.save(report);
            log.info("[{}] 已为此会话创建初始面试报告。", sessionId);
        }

        String resumePromptSection = "【候选人简历核心内容】\n---\n" + summarizedResume + "\n---\n";

        String prompt = "你是一位顶级的AI面试官，面试风格专业、精准且富有洞察力。你正在对一位应聘【" + positionTitle + "】岗位的候选人进行面试。\n\n" +
                "【岗位核心职责描述】: " + positionDescription + "\n\n" +
                resumePromptSection + "\n" +
                "【你的核心任务】:\n" +
                "请严格根据我提供的【问题模板】和【候选人简历核心内容】，为本次面试设计一套包含8个问题的面试题。你必须确保生成的问题与候选人的简历背景和所应聘岗位高度相关。\n\n" +
                "【问题模板 - 请严格遵守此结构】:\n" +
                "1. **问题1 (开场介绍)**: 设计一个引导性的开场问题，要求候选人结合简历进行一个全面的自我介绍，并点出自己的核心优势。\n" +
                "2. **问题2 (简历项目问题 1)**: [必须] 根据候选人的简历内容，选择一个看起来最核心或最复杂的项目，提出一个深入的技术实现细节问题。\n" +
                "3. **问题3 (简历项目问题 2)**: [必须] 根据候选人的简历内容，选择另一个项目，询问候选人该项目中遇到的最大挑战以及如何解决的。\n" +
                "4. **问题4 (业务场景题 1)**: [必须] 结合岗位职责和候选人的项目经验，设计一个具体的、开放式的业务场景题。\n" +
                "5. **问题5 (算法题 1)**: [必须] 设计一道与岗位技术栈相关的【中等难度】编程算法题。要求候选人描述解题思路即可。\n" +
                "6. **问题6 (业务场景题 2)**: [必须] 设计另一个业务场景题，可以侧重于系统稳定性、故障排查或性能优化。\n" +
                "7. **问题7 (算法题 2)**: [必须] 设计另一道与岗位技术栈相关的【中等难度】算法题，类型可以与上一题不同。同样要求候选人描述思路。\n" +
                "8. **问题8 (业务场景题 3 / 综合素质)**: 设计一个更宏观的业务场景题，或者一个考察候选人软技能的问题。\n\n" +
                "【最终输出格式要求】:\n" +
                "你必须，也只能返回一个JSON格式的字符串数组，其中包含你按顺序设计的8个问题。不要添加任何题号、解释、介绍或其他无关文字。";

        String questionsJson = sparkClient.askQuestion(prompt);
        List<String> questions = parseQuestions(questionsJson, positionId);

        session.setQuestions(questions);
        log.info("[{}] 基于简历的面试题已生成，共 {} 道。", sessionId, questions.size());
        return session;
    }

    public InterviewSession processAnswer(String sessionId, String userAnswerText, EmotionData emotionData, BehavioralData behavioralData) throws Exception {
        InterviewSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalStateException("面试会话未找到或已结束: " + sessionId);
        }

        String currentQuestion = session.getQuestions().get(session.getCurrentQuestionIndex());
        log.info("[{}] 正在为问题 '{}' 的回答进行评价...", sessionId, currentQuestion);

        String emotionContext = formatEmotionDataForPrompt(emotionData);
        String behavioralContext = formatBehavioralDataForPrompt(behavioralData);

        String evalPrompt = String.format(
                "你是一位顶级的AI面试官，具备深度分析能力。请严格按照下面的结构和要求，对候选人的表现进行综合评价。\n\n" +
                        "--- 任务背景 ---\n" +
                        "- **面试问题**: \"%s\"\n" +
                        "- **候选人回答**: \"%s\"\n\n" +
                        "--- 候选人状态分析（辅助信息） ---\n" +
                        "- **情绪状态总结**: %s\n" +
                        "- **行为状态总结**: %s\n\n" +
                        "--- 你的任务与指令 ---\n" +
                        "1.  **核心职责**: 你的评价【核心】必须基于候选人回答的【内容质量】。这是最重要的评判标准。\n" +
                        "2.  **情境融合 (必须执行)**: 你【必须】将“状态分析”作为上下文，巧妙地融入到你的评价中，以提供更全面的反馈。下面是一些【优秀范例】供你学习：\n" +
                        "    - **范例1 (内容好 + 状态好)**: \"回答逻辑清晰，展现了自信（情绪状态显示开心/中性，视线稳定）。\"\n" +
                        "    - **范例2 (内容好 + 状态紧张)**: \"内容切中要点。尽管状态显示略显紧张，但回答依然很有条理。\"\n" +
                        "    - **范例3 (内容差 + 状态差)**: \"回答未能抓住问题关键，且专注度不高（视线频繁偏离）。\"\n" +
                        "3.  **输出要求**: 你的最终输出【必须】是纯粹的评价文本，且【严格限制在50字以内】。禁止包含任何“好的”、“评价如下：”等多余的前缀或解释。\n\n" +
                        "**最终评价**: ",
                currentQuestion, userAnswerText, emotionContext, behavioralContext
        );

        log.info("[{}] 发送给大模型的最终Prompt: {}", sessionId, evalPrompt);
        String evaluation = sparkClient.askQuestion(evalPrompt);

        InterviewSession.QaPair qaPair = new InterviewSession.QaPair();
        qaPair.setQuestion(currentQuestion);
        qaPair.setAnswer(userAnswerText);
        qaPair.setEvaluation(evaluation);
        session.getHistory().add(qaPair);

        session.getUserAnswers().add(userAnswerText);
        session.getAnswerEvaluations().add(evaluation);
        session.setCurrentQuestionIndex(session.getCurrentQuestionIndex() + 1);
        sessionRepository.save(session);
        log.info("[{}] 回答评价完成，索引更新为: {}", sessionId, session.getCurrentQuestionIndex());
        return session;
    }

    private String formatEmotionDataForPrompt(EmotionData data) {
        if (data == null) { return "不可用"; }
        DecimalFormat df = new DecimalFormat("#.##");
        return String.format("中性: %s, 开心: %s, 惊讶: %s, 悲伤: %s, 愤怒: %s", df.format(data.getNeutral()), df.format(data.getHappy()), df.format(data.getSurprised()), df.format(data.getSad()), df.format(data.getAngry()));
    }

    private String formatBehavioralDataForPrompt(BehavioralData data) {
        if (data == null) { return "不可用"; }
        String genderText = "未知";
        if (data.getEstimatedGender() != null) { genderText = data.getEstimatedGender().equalsIgnoreCase("male") ? "男" : "女"; }
        return String.format("年龄/性别约: %s岁/%s, 主要视线: %s", data.getEstimatedAge() != null ? data.getEstimatedAge() : "未知", genderText, data.getGazeDirection() != null ? data.getGazeDirection() : "未知");
    }

    public String getNextQuestion(String sessionId) {
        InterviewSession session = sessions.get(sessionId);
        if (session == null) { log.warn("[{}] 尝试获取下一题，但会话不存在。", sessionId); return null; }
        int nextIndex = session.getCurrentQuestionIndex();
        if (nextIndex < session.getQuestions().size()) {
            return session.getQuestions().get(nextIndex);
        } else {
            return null;
        }
    }

    public InterviewSession endInterview(String sessionId) {
        log.info("[{}] 交互式面试结束，正在清理内存会话...", sessionId);
        return sessions.remove(sessionId);
    }

    private List<String> parseQuestions(String questionsJson, String positionId) {
        try {
            String cleanedJson = cleanJsonString(questionsJson);
            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> questions = gson.fromJson(cleanedJson, listType);
            if (questions != null && !questions.isEmpty()) { return questions; }
        } catch (JsonSyntaxException e) { log.error("解析大模型返回的问题JSON失败", e); }
        return getFallbackQuestionsFor(positionId);
    }

    private String getPositionTitleFor(String positionId) {
        return switch (positionId) {
            case "ai-engineer" -> "人工智能工程师";
            case "backend-developer" -> "后端开发工程师";
            case "product-manager" -> "产品经理";
            case "frontend-developer"->"前端开发工程师";
            case "algorithm-engineer"->"算法工程师";
            case "qa-engineer"->"测试工程师";
            case "big-data-engineer"->"大数据开发工程师";
            case "devops-engineer"->"运维工程师";
            default -> "通用岗位";
        };
    }

    private String getPositionDescriptionFor(String positionId) {
        return switch (positionId) {
            case "ai-engineer" -> "熟练掌握Python，熟悉至少一种深度学习框架（如PyTorch, TensorFlow），对常用的机器学习算法（如LR, GBDT, CNN, RNN）有深入理解，具备优秀的算法设计和实现能力。";
            case "backend-developer" -> "精通Java或Go语言，深入理解Spring Boot/Spring Cloud或Gin等主流框架，熟悉MySQL/PostgreSQL和Redis/MongoDB，具备高并发、高可用系统设计能力，熟悉容器化技术（Docker, K8s）。";
            case "product-manager" -> "具备出色的用户洞察和市场分析能力，能够独立完成市场调研、竞品分析、需求文档撰写（PRD），逻辑思维清晰，善于沟通协调，能够驱动项目顺利上线。";
            default -> "具备良好的沟通能力、逻辑思维能力、学习能力和团队合作精神。";
        };
    }

    private List<String> getFallbackQuestionsFor(String positionId) {
        return switch (positionId) {
            case "ai-engineer" -> List.of("请先用一分钟时间做一个自我介绍，突出你的核心技术优势和项目经验。", "你为什么选择人工智能这个领域？你对AI未来的发展有什么看法？", "请解释一下卷积神经网络（CNN）的基本工作原理。", "【业务场景题】如果让你设计一个电商平台的商品推荐系统，你会如何入手？请描述你的技术选型、数据来源和评估指标。", "【算法题】请描述一下如何用代码实现一个简单的梯度下降算法来优化线性回归模型。", "在你简历提到的XX项目中，你遇到的最大技术挑战是什么？你是如何解决的？", "【算法题】给你一个未排序的整数数组，请设计一个时间复杂度为O(n)的算法，找到其中第K大的元素。请描述你的思路。", "【综合素质】你如何保持对AI领域最新技术和论文的关注和学习？");
            case "backend-developer" -> List.of("请先做一个自我介绍，重点说明你的技术栈和项目背景。", "你为什么选择后端开发作为你的职业方向？你认为一个优秀的后端工程师需要具备哪些素质？", "请解释一下TCP的三次握手和四次挥手过程。", "【业务场景题】如何设计一个高并发的秒杀系统？请从前端、后端、数据库、缓存等多个层面阐述你的方案。", "【算法题】请用你熟悉的语言，描述一下如何实现一个LRU（最近最-少使用）缓存淘汰算法。", "在你简历中提到的XX系统中，数据库慢查询是如何排查和优化的？", "【算法题】给你一个字符串，请找出其中不含有重复字符的最长子串的长度。请描述你的思路。", "【综合素质】当线上系统出现紧急故障时，你的排查思路和处理流程是怎样的？");
            default -> List.of("请先做一个简单的自我介绍。", "谈谈你对我们公司和你所应聘的这个岗位的理解。", "你认为你最大的优点和缺点分别是什么？", "分享一个你过去遇到的最困难的项目挑战以及你是如何解决的。", "【算法题】请描述一下二分查找算法的实现思路及其适用条件。", "【业务场景题】如果你是项目负责人，如何平衡产品质量、功能范围和上线时间这三者的关系？", "【算法题】请描述一下快速排序的基本思想。", "你对未来的职业发展有什么规划？");
        };
    }

    private String cleanJsonString(String jsonString) {
        String cleaned = jsonString.trim();
        if (cleaned.startsWith("```json")) { cleaned = cleaned.substring(7); }
        if (cleaned.endsWith("```")) { cleaned = cleaned.substring(0, cleaned.length() - 3); }
        return cleaned.trim();
    }

    public InterviewReport getReport(String sessionId) {
        return reportRepository.findById(sessionId).orElseThrow(() -> new IllegalStateException("找不到ID为 " + sessionId + " 的面试报告。"));
    }

    public void saveResumeTextToSession(String sessionId, String positionId, String resumeText) {
        InterviewSession session = sessions.computeIfAbsent(sessionId, id -> {
            log.info("[{}] 会话不存在，正在为岗位 '{}' 创建新的会话实例。", id, positionId);
            return new InterviewSession(id, positionId);
        });
        if (!positionId.equals(session.getPositionId())) {
            log.warn("[{}] 会话已存在，但岗位ID不匹配。正在更新岗位ID为 '{}'。", sessionId, positionId);
            session.setPositionId(positionId);
        }
        session.setResumeText(resumeText);
        log.info("[{}] 简历文本已成功保存到会话中。", sessionId);
    }

    private InterviewSession getOrCreateSession(String sessionId, String positionId) {
        return sessions.computeIfAbsent(sessionId, id -> {
            log.info("[{}] 会话不存在，正在为岗位 '{}' 创建新的会话实例。", id, positionId);
            return new InterviewSession(id, positionId);
        });
    }

    @Transactional
    public void finish(String sessionId) {
        log.info("接收到面试结束请求，会话ID: {}", sessionId);

        InterviewSession session = sessions.get(sessionId);

        if (session == null) {
            log.warn("无法在内存中找到会话ID: {}。可能服务已重启或会话已过期。将跳过报告生成。", sessionId);
            return;
        }

        interviewAnalysisService.generateReportFromSessionAsync(session);

        sessions.remove(sessionId);
        log.info("[{}] 已触发异步报告生成任务，并已从内存中清理会话。", sessionId);
    }
}