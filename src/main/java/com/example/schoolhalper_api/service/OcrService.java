package com.example.schoolhalper_api.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class OcrService {
    private static final List<String> SUPPORTED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png"
    );

    public String extractTextFromImage(byte[] imageData) throws IOException, TesseractException {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("src/main/resources/tessdata");
            tesseract.setLanguage("rus+eng");

            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage image = ImageIO.read(bis);

            if (image == null) {
                throw new IOException("Не удалось прочитать изображение");
            }

            return tesseract.doOCR(image).trim();
        } catch (TesseractException e) {
            throw new TesseractException("Ошибка распознавания текста: " + e.getMessage(), e);
        }
    }

    public boolean verifyCertificate(String extractedText) {
        if (extractedText == null || extractedText.isEmpty()) {
            return false;
        }

        String lowerText = extractedText.toLowerCase();
        List<String> keywords = Arrays.asList(
                "учитель",
                "преподаватель",
                "педагог",
                "образование",
                "диплом",
                "квалификация",
                "сертификат"
        );

        return keywords.stream().anyMatch(lowerText::contains);
    }

    public void validateImageFile(MultipartFile file) throws IllegalArgumentException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        if (!SUPPORTED_MIME_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла");
        }

        if (file.getSize() > 5_000_000) {
            throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
        }
    }
}