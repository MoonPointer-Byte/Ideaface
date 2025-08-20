package com.example.interview_agent.service;

import com.example.interview_agent.client.SparkClient;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Service
public class ResumeAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(ResumeAssessmentService.class);

    @Autowired
    private SparkClient sparkClient;

    /**
     * 从上传的文件中提取文本，并调用AI大模型评估简历
     * @param positionId 目标岗位ID
     * @param file 上传的简历文件
     * @return AI生成的评估报告（Markdown格式）
     * @throws IOException 如果文件读取失败
     */
    public String assessResumeFromFile(String positionId, MultipartFile file) throws IOException {
        log.info("接收到对岗位 '{}' 的简历评估请求，文件: {}", positionId, file.getOriginalFilename());

        // 1. 从文件中提取纯文本
        String resumeText = extractTextFromFile(file);
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new IllegalArgumentException("无法从文件中提取有效的文本内容，请检查文件格式或内容。");
        }
        log.info("文件文本提取成功，总字数: {}", resumeText.length());

        // 2. 获取岗位信息
        String positionTitle = getPositionTitleFor(positionId);
        String positionDescription = getPositionDescriptionFor(positionId);

        // 3. 构建高质量的Prompt
        String prompt = buildAssessmentPrompt(positionTitle, positionDescription, resumeText);
        log.debug("发送给星火大模型的Prompt构建完成。");

        try {
            // 3. 直接调用 SparkClient 中已有的 askQuestion 方法
            log.info("正在调用SparkClient...");
            String assessmentReport = sparkClient.askQuestion(prompt);
            log.info("成功从星火大模型获取评估报告。");
            return assessmentReport;
        } catch (Exception e) {
            log.error("调用SparkClient评估简历时发生严重错误", e);
            // 将检查型异常转换为运行时异常，以便上层控制器统一处理
            throw new RuntimeException("与AI服务通信时出错，请稍后重试。", e);
        }
    }

    /**
     * 根据文件类型，使用对应的库来提取文本
     */
    private String extractTextFromFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        log.info("文件内容类型: {}", contentType);

        try (InputStream inputStream = file.getInputStream()) {
            if (Objects.equals(contentType, "application/pdf")) {
                return parsePdf(inputStream);
            } else if (Objects.equals(contentType, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                return parseDocx(inputStream);
            } else {
                // 您也可以在这里添加对.doc(HWPF)或.txt的支持
                throw new IllegalArgumentException("不支持的文件类型: " + contentType);
            }
        }
    }

    /**
     * 使用 PDFBox 解析PDF文件
     */
    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 使用 Apache POI 解析DOCX文件
     */
    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    /**
     * 构建用于简历评估的详细Prompt
     */
    private String buildAssessmentPrompt(String positionTitle, String positionDescription, String resumeText) {
        // 为保护隐私和控制token，可以对简历进行适当截断
        String summarizedResume = resumeText.length() > 4000 ? resumeText.substring(0, 4000) + "..." : resumeText;

        return String.format(
                "你是一位顶级的技术招聘专家和资深HR，你的任务是基于候选人的简历和目标岗位，提供一份专业、深入、结构化的评估报告。\n\n" +
                        "---背景信息---\n" +
                        "1.  **目标岗位**: %s\n" +
                        "2.  **岗位核心要求**: %s\n" +
                        "3.  **候选人简历全文**:\n---\n%s\n---\n\n" +
                        "---你的任务与指令---\n" +
                        "请严格按照下面的Markdown格式输出你的评估报告，确保内容客观、有建设性，并直接关联简历内容与岗位要求。\n\n" +
                        "### 1. 综合评价与岗位匹配度\n" +
                        "- **一句话总结**: (用一句话概括候选人的整体水平和潜力)\n" +
                        "- **匹配度评估**: (量化评估，例如：高、中、低)，并简要说明理由。\n" +
                        "- **核心结论**: (详细阐述你认为候选人是否值得进入下一轮面试，为什么？)\n\n" +
                        "### 2. 亮点分析 (Strengths Analysis)\n" +
                        "(列出3-4个最突出的优点，每个优点都必须有简历中的具体例子作为支撑)\n" +
                        "- **亮点一**: (例如：项目经验丰富，体现在...)\n" +
                        "- **亮点二**: (例如：技术栈吻合度高，简历中提到的...技术正是岗位所需)\n" +
                        "- **亮点三**: (例如：具备优秀的学习能力或成果，体现在...)\n\n" +
                        "### 3. 潜在风险与待考察点 (Potential Risks & Points to Verify)\n" +
                        "(列出2-3个你从简历中看到的潜在疑点或在面试中需要进一步确认的地方)\n" +
                        "- **风险一**: (例如：项目描述过于宽泛，需考察其真实贡献度)\n" +
                        "- **风险二**: (例如：技术栈虽广但可能不深，需考察...技术的掌握程度)\n\n" +
                        "### 4. 建议面试问题 (Suggested Interview Questions)\n" +
                        "(根据你的分析，设计3个有针对性的面试问题，帮助面试官深入了解候选人)\n" +
                        "- **问题一 (针对项目)**: (例如：关于您在XX项目中的角色，您能详细说明...吗？)\n" +
                        "- **问题二 (针对技能)**: (例如：简历中提到您熟悉XX技术，能否谈谈您在...场景下的应用经验？)\n" +
                        "- **问题三 (针对风险点)**: (例如：针对“风险一”提出一个考察问题)",
                positionTitle, positionDescription, summarizedResume
        );
    }

    // --- 复用您项目中的辅助方法 ---
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