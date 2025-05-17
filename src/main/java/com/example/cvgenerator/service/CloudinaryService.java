package com.example.cvgenerator.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final boolean cloudinaryEnabled;
    private static final int MAX_IMAGE_SIZE_KB = 500; // Максимальний розмір зображення 500 KB

    @Autowired
    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
        this.cloudinaryEnabled = (cloudinary != null);

        if (this.cloudinaryEnabled) {
            System.out.println("CloudinaryService ініціалізовано успішно.");
        } else {
            System.out.println("CloudinaryService не активовано. Використовується локальне зберігання файлів.");
        }
    }

    public boolean isCloudinaryEnabled() {
        return cloudinaryEnabled;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        if (!cloudinaryEnabled) {
            return null; // Повертаємо null, щоб використовувати локальне зберігання
        }

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // Перевіряємо розмір файлу
            long fileSizeKB = file.getSize() / 1024;
            byte[] imageData;

            if (fileSizeKB > MAX_IMAGE_SIZE_KB) {
                // Зменшуємо розмір зображення
                imageData = resizeImage(file.getBytes());
                System.out.println("Зображення було зменшено з " + fileSizeKB + "KB до "
                        + (imageData.length / 1024) + "KB");
            } else {
                imageData = file.getBytes();
            }

            // Генеруємо унікальне ім'я файлу
            String publicId = "cv_photo_" + UUID.randomUUID().toString();

            // Завантажуємо файл до Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    imageData,
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "cv_generator",
                            "resource_type", "auto"
                    )
            );

            // Отримуємо URL завантаженого зображення
            String imageUrl = (String) uploadResult.get("secure_url");
            System.out.println("Фото успішно завантажено до Cloudinary: " + imageUrl);

            return imageUrl;
        } catch (IOException e) {
            System.err.println("Помилка при завантаженні зображення до Cloudinary: " + e.getMessage());
            e.printStackTrace();
            return null; // Повертаємо null, щоб використовувати локальне зберігання
        }
    }

    // Метод для зменшення розміру зображення
    private byte[] resizeImage(byte[] imageData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(bis);

        if (originalImage == null) {
            return imageData; // Повертаємо оригінальні дані, якщо не можемо прочитати зображення
        }

        // Визначаємо нові розміри зі збереженням пропорцій
        int maxWidth = 800;
        int maxHeight = 800;

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        double ratio = (double) width / height;

        if (width > maxWidth) {
            width = maxWidth;
            height = (int) (width / ratio);
        }

        if (height > maxHeight) {
            height = maxHeight;
            width = (int) (height * ratio);
        }

        // Створюємо зменшене зображення
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g.dispose();

        // Зберігаємо зображення в байтовий масив з високим стисненням
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Зберігаємо як JPEG з якістю 0.7
        ImageIO.write(resizedImage, "jpeg", bos);

        return bos.toByteArray();
    }

    public void deleteFile(String imageUrl) {
        if (!cloudinaryEnabled) {
            return; // Не робимо нічого, якщо Cloudinary не налаштовано
        }

        if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.contains("cloudinary.com")) {
            return;
        }

        try {
            // Отримуємо public_id з URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId == null) {
                return;
            }

            // Видаляємо файл з Cloudinary
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Фото успішно видалено з Cloudinary: " + publicId);
        } catch (Exception e) {
            System.err.println("Помилка при видаленні зображення з Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        // Приклад URL: https://res.cloudinary.com/your-cloud-name/image/upload/v1234567890/cv_generator/cv_photo_abc123.jpg

        try {
            if (!imageUrl.contains("/cv_generator/")) {
                return null;
            }

            // Отримуємо частину URL після '/cv_generator/'
            String folder = "cv_generator/";
            int startIndex = imageUrl.indexOf(folder) + folder.length();
            int endIndex = imageUrl.lastIndexOf(".");

            if (endIndex == -1) {
                endIndex = imageUrl.length();
            }

            String publicId = "cv_generator/" + imageUrl.substring(startIndex, endIndex);
            return publicId;
        } catch (Exception e) {
            System.err.println("Помилка при отриманні public_id з URL: " + e.getMessage());
            return null;
        }
    }
}