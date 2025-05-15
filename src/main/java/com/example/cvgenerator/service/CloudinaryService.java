package com.example.cvgenerator.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;
    private final boolean cloudinaryEnabled;

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
            // Генеруємо унікальне ім'я файлу
            String publicId = "cv_photo_" + UUID.randomUUID().toString();

            // Завантажуємо файл до Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
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
            e.printStackTrace();
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