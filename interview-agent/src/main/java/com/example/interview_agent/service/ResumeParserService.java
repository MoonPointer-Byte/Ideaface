package com.example.interview_agent.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Service
public class ResumeParserService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeParserService.class);

    /**
     * 根据文件类型解析文件，提取纯文本内容
     * @param file 上传的文件
     * @return 提取的文本内容
     * @throws IOException 文件读取或解析异常
     */
    public String parse(MultipartFile file) throws IOException, IllegalArgumentException {
        String contentType = file.getContentType();
        logger.debug("开始解析文件，类型: {}", contentType);

        if (Objects.equals(contentType, "application/pdf")) {
            return parsePdf(file.getInputStream());
        } else if (Objects.equals(contentType, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return parseDocx(file.getInputStream());
        } else {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
    }

    private String parsePdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            logger.info("PDF文件解析成功，提取字数: {}", text.length());
            return text;
        }
    }

    private String parseDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            String text = extractor.getText();
            logger.info("DOCX文件解析成功，提取字数: {}", text.length());
            return text;
        }
    }
}