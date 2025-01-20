package org.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class WordParser implements FileParser{

    /**
     * 解析Word文件并返回其文本内容
     *
     * @param file Word文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder text = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString();
        }
    }

    /**
     * 根据文件路径解析Word文件并返回其文本内容
     *
     * @param filePath Word文件的路径
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(String filePath) throws IOException {
        File file = new File(filePath);
        return parse(file);
    }

    // 测试方法
    public static void main(String[] args) {

    }
}