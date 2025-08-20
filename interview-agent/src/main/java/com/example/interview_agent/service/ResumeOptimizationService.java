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
public class ResumeOptimizationService {

    private static final Logger log = LoggerFactory.getLogger(ResumeOptimizationService.class);

    @Autowired
    private SparkClient sparkClient;

    /**
     * 解析简历文件，根据目标岗位进行分析，并生成优化建议。
     * @param targetPositionId 目标岗位的ID。
     * @param resumeFile 用户上传的简历文件。
     * @return Markdown格式的优化建议字符串。
     * @throws IOException 如果文件解析失败。
     */
    public String optimizeResume(String targetPositionId, MultipartFile resumeFile) throws IOException {
        log.info("开始为目标岗位 '{}' 进行简历优化", targetPositionId);

        // 1. 从上传的文件中提取文本
        String resumeText = extractTextFromFile(resumeFile);
        if (resumeText == null || resumeText.trim().isEmpty()) {
            throw new IllegalArgumentException("无法从简历文件中提取有效文本。");
        }
        log.info("成功从简历中提取 {} 个字符。", resumeText.length());

        // 2. 获取目标岗位的详细信息
        String positionTitle = getPositionTitleFor(targetPositionId);
        String positionDescription = getPositionDescriptionFor(targetPositionId);

        // 3. 构建专业的中文优化Prompt
        String prompt = buildOptimizationPrompt(positionTitle, positionDescription, resumeText);
        log.debug("简历优化Prompt已构建。");

        // 4. 调用AI服务
        try {
            return sparkClient.askQuestion(prompt);
        } catch (Exception e) {
            log.error("调用SparkClient进行简历优化时发生错误", e);
            throw new RuntimeException("AI优化引擎当前不可用，请稍后再试。", e);
        }
    }

    /**
     * 构建一个高质量的、面向简历优化的中文Prompt。
     * 该Prompt引导AI扮演资深HR和技术专家的角色，提供结构化、可落地的反馈。
     */
    private String buildOptimizationPrompt(String positionTitle, String positionDescription, String resumeText) {
        String summarizedResume = resumeText.length() > 4000 ? resumeText.substring(0, 4000) + "..." : resumeText;

        return String.format(
                "你是一位顶级的职业规划导师和资深技术招聘官，专注于科技行业。你的任务是基于用户当前的简历和目标岗位，提供一份专业、深入、可执行的简历优化方案。\n\n" +
                        "---背景信息---\n" +
                        "1.  **目标岗位**: %s\n" +
                        "2.  **目标岗位核心要求**: %s\n" +
                        "3.  **用户现有简历**:\n---\n%s\n---\n\n" +
                        "---你的任务与指令---\n" +
                        "你的反馈必须专业、有建设性、且鼓舞人心。请严格按照下面的Markdown格式，输出你的优化报告。不要添加任何额外的开场白或总结。\n\n" +
                        "### 1. 整体印象与核心建议\n" +
                        "(对简历给人的第一印象进行点评。排版是否清晰？核心优势是否突出？并给出1-2条最重要的、可以立刻改进的建议。)\n\n" +
                        "### 2. 逐项优化建议\n" +
                        "(针对简历的每个核心模块，提供具体的、可操作的优化点，并给出优秀的修改范例。)\n\n" +
                        "#### **个人信息与摘要**\n" +
                        "- **问题点**: (例如：个人摘要部分较为宽泛，未能突出与 **%s** 岗位的匹配度。)\n" +
                        // **修正处**: 将 `30%` 和 `50%` 中的 `%` 转义为 `%%`
                        "- **优化建议**: (例如：建议使用STAR法则或量化成果来重写。例如，将 `负责后端功能开发` 修改为 `主导开发了新的缓存模块，通过引入Redis和优化数据结构，使核心API的响应时间降低了30%%，QPS提升了50%%。`)\n" +
                        "- **优秀范例**: (提供一个简短的、可以直接参考的摘要范例)\n\n" +
                        "#### **工作/实习经历**\n" +
                        "- **问题点**: (例如：项目描述偏向于罗列技术，而非体现业务价值和个人贡献。)\n" +
                        "- **优化建议**: (例如：在描述每个项目时，都应思考：我解决了什么问题？我带来了什么价值？用数字来量化你的成果。)\n\n" +
                        "#### **项目经验**\n" +
                        "- **问题点**: (例如：缺少项目所使用的技术栈列表，招聘官无法快速判断技术匹配度。)\n" +
                        "- **优化建议**: (例如：为每个项目补充 `技术栈` 标签，如 `技术栈: Spring Boot, MySQL, Redis, Docker`。并附上可访问的项目链接或GitHub仓库地址。)\n\n" +
                        "#### **技能清单**\n" +
                        "- **问题点**: (例如：技能列表较为杂乱。)\n" +
                        "- **优化建议**: (例如：对技能进行分类，如 `编程语言`、`框架与库`、`数据库`、`工具与中间件`，让结构一目了然。)\n\n" +
                        "### 3. 最终润色与关键词匹配\n" +
                        "(提供关于简历格式、错别字检查的最终建议，并提醒用户检查简历中是否包含了目标岗位描述中的核心关键词，如 `分布式系统`, `高并发` 等。)",
                positionTitle, positionDescription, summarizedResume, positionTitle
        );
    }

    // --- 复用的辅助方法 (无需改动) ---
    private String extractTextFromFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        log.info("文件内容类型: {}", contentType);

        try (InputStream inputStream = file.getInputStream()) {
            if (Objects.equals(contentType, "application/pdf")) {
                try (PDDocument document = PDDocument.load(inputStream)) {
                    return new PDFTextStripper().getText(document);
                }
            } else if (Objects.equals(contentType, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                try (XWPFDocument doc = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            } else {
                throw new IllegalArgumentException("不支持的文件类型: " + contentType);
            }
        }
    }

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
            default -> "具备良好的沟通能力、逻辑思维能力、学习能力和团队合作精神。";
        };
    }
}