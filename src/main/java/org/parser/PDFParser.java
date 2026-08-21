package org.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFParser implements FileParser{

    /**
     * 解析PDF文件并返回其文本内容
     *
     * @param file PDF文件
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(document);
        }
    }

    /**
     * 根据文件路径解析PDF文件并返回其文本内容
     *
     * @param filePath PDF文件的路径
     * @return 解析后的字符串
     * @throws IOException 如果发生I/O错误
     */
    public  String parse(String filePath) throws IOException {
        File file = new File(filePath);
        return parse(file);
    }

    // 测试方法
    public static void main(String[] args) {
        try {
            PDFParser parser = new PDFParser();
            String content = parser.parse("C:\\Users\\19664\\Desktop\\2311.12351v2.pdf");
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}