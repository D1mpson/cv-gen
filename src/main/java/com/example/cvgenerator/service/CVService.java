package com.example.cvgenerator.service;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.CVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CVService {

    private final CVRepository cvRepository;
    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @Value("${app.upload.dir:/app/uploads}")
    private String baseUploadDir;

    private final String UPLOAD_DIR;

    @Autowired
    public CVService(CVRepository cvRepository, UserService userService, CloudinaryService cloudinaryService) {
        this.cvRepository = cvRepository;
        this.userService = userService;
        this.cloudinaryService = cloudinaryService;
        this.UPLOAD_DIR = baseUploadDir + "/photos/";

        try {
            File uploadPathDir = new File(UPLOAD_DIR);
            if (!uploadPathDir.exists()) {
                boolean created = uploadPathDir.mkdirs();
                if (created) {
                    System.out.println("Директорію створено: " + UPLOAD_DIR);
                } else {
                    System.err.println("Не вдалося створити директорію: " + UPLOAD_DIR);
                }
            }
        } catch (Exception e) {
            System.err.println("Помилка при створенні директорії: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CV createCV(CV cv, MultipartFile photoFile) throws IOException {
        // Встановлюємо поточного користувача
        User currentUser = userService.getCurrentUser();
        cv.setUser(currentUser);

        // Обробка завантаження фото
        if (photoFile != null && !photoFile.isEmpty()) {
            // Спочатку намагаємося завантажити до Cloudinary, якщо він налаштований
            if (cloudinaryService.isCloudinaryEnabled()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(photoFile);
                    if (imageUrl != null) {
                        cv.setPhotoPath(imageUrl);
                        System.out.println("Фото успішно завантажено до Cloudinary: " + imageUrl);
                    } else {
                        // Якщо Cloudinary не спрацював, використовуємо локальне зберігання
                        uploadToLocalStorage(cv, photoFile);
                    }
                } catch (Exception e) {
                    System.err.println("Помилка при завантаженні до Cloudinary. Використовуємо локальне сховище: " + e.getMessage());
                    // Якщо щось пішло не так з Cloudinary, використовуємо локальне зберігання
                    uploadToLocalStorage(cv, photoFile);
                }
            } else {
                // Якщо Cloudinary не налаштований, використовуємо локальне зберігання
                uploadToLocalStorage(cv, photoFile);
            }
        }

        // Ініціалізуємо колекції, якщо вони null
        if (cv.getPortfolioLinks() == null) {
            cv.setPortfolioLinks(List.of());
        }

        if (cv.getKnownLanguages() == null) {
            cv.setKnownLanguages(List.of());
        }

        return cvRepository.save(cv);
    }

    private void uploadToLocalStorage(CV cv, MultipartFile photoFile) {
        try {
            // Переконаємось, що директорія існує
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photoFile.getBytes());
            cv.setPhotoPath(fileName);

            System.out.println("Фото збережено локально за шляхом: " + filePath.toString());
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні фото локально: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<CV> getAllCVsByUser(User user) {
        return cvRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<CV> getAllCVsForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return getAllCVsByUser(currentUser);
    }

    public Optional<CV> getCVById(Long id) {
        return cvRepository.findById(id);
    }

    public void updateCV(CV cv, MultipartFile photoFile) throws IOException {
        CV existingCV = cvRepository.findById(cv.getId())
                .orElseThrow(() -> new RuntimeException("CV з ID " + cv.getId() + " не знайдено"));

        // Перевірка чи CV належить поточному користувачу
        User currentUser = userService.getCurrentUser();
        if (!existingCV.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("У вас немає прав для редагування цього CV");
        }

        existingCV.setName(cv.getName());
        existingCV.setAboutMe(cv.getAboutMe());
        existingCV.setHobbies(cv.getHobbies());

        existingCV.setSoftSkills(cv.getSoftSkills());
        existingCV.setHardSkills(cv.getHardSkills());
        existingCV.setOtherLanguages(cv.getOtherLanguages());
        existingCV.setEducation(cv.getEducation());
        existingCV.setCourses(cv.getCourses());
        existingCV.setWorkExperience(cv.getWorkExperience());

        if (cv.getPortfolioLinks() != null) {
            existingCV.setPortfolioLinks(cv.getPortfolioLinks());
        }

        if (cv.getKnownLanguages() != null) {
            existingCV.setKnownLanguages(cv.getKnownLanguages());
        }

        existingCV.setFont(cv.getFont());
        existingCV.setLanguage(cv.getLanguage());

        // Якщо шаблон змінився, оновлюємо його
        if (cv.getTemplate() != null) {
            existingCV.setTemplate(cv.getTemplate());
        }

        // Обробка нового фото
        if (photoFile != null && !photoFile.isEmpty()) {
            String oldPhotoPath = existingCV.getPhotoPath();

            // Спочатку спробуємо завантажити фото до Cloudinary
            if (cloudinaryService.isCloudinaryEnabled()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(photoFile);
                    if (imageUrl != null) {
                        // Видаляємо старе фото, якщо воно було на Cloudinary
                        if (oldPhotoPath != null && oldPhotoPath.contains("cloudinary.com")) {
                            cloudinaryService.deleteFile(oldPhotoPath);
                        } else if (oldPhotoPath != null && !oldPhotoPath.isEmpty()) {
                            // Видаляємо локальний файл, якщо такий був
                            try {
                                Path oldPath = Paths.get(UPLOAD_DIR + oldPhotoPath);
                                Files.deleteIfExists(oldPath);
                            } catch (Exception e) {
                                System.err.println("Не вдалося видалити локальне фото: " + e.getMessage());
                            }
                        }

                        existingCV.setPhotoPath(imageUrl);
                        System.out.println("Фото успішно оновлено в Cloudinary: " + imageUrl);
                    } else {
                        // Якщо Cloudinary не спрацював, використовуємо локальне зберігання
                        updateLocalPhoto(existingCV, photoFile, oldPhotoPath);
                    }
                } catch (Exception e) {
                    System.err.println("Помилка при оновленні фото в Cloudinary. Використовуємо локальне сховище: " + e.getMessage());
                    // Якщо щось пішло не так з Cloudinary, використовуємо локальне зберігання
                    updateLocalPhoto(existingCV, photoFile, oldPhotoPath);
                }
            } else {
                // Якщо Cloudinary не налаштований, використовуємо локальне зберігання
                updateLocalPhoto(existingCV, photoFile, oldPhotoPath);
            }
        }

        cvRepository.save(existingCV);
    }

    private void updateLocalPhoto(CV cv, MultipartFile photoFile, String oldPhotoPath) {
        try {
            // Видалення старого фото, якщо воно існує і не є Cloudinary URL
            if (oldPhotoPath != null && !oldPhotoPath.isEmpty() && !oldPhotoPath.startsWith("http")) {
                Path oldPath = Paths.get(UPLOAD_DIR + oldPhotoPath);
                Files.deleteIfExists(oldPath);
            }

            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, photoFile.getBytes());
            cv.setPhotoPath(fileName);

            System.out.println("Фото оновлено локально за шляхом: " + filePath.toString());
        } catch (Exception e) {
            System.err.println("Помилка при оновленні фото локально: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteCV(Long id) {
        CV cv = cvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CV з ID " + id + " не знайдено"));

        User currentUser = userService.getCurrentUser();
        if (!cv.getUser().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("У вас немає прав для видалення цього CV");
        }

        String photoPath = cv.getPhotoPath();

        // Видаляємо фото з Cloudinary, якщо це Cloudinary URL
        if (photoPath != null && !photoPath.isEmpty() && photoPath.contains("cloudinary.com")) {
            try {
                cloudinaryService.deleteFile(photoPath);
            } catch (Exception e) {
                System.err.println("Помилка при видаленні фото з Cloudinary: " + e.getMessage());
            }
        }
        // Або видаляємо локальний файл
        else if (photoPath != null && !photoPath.isEmpty()) {
            try {
                Path localPhotoPath = Paths.get(UPLOAD_DIR + photoPath);
                Files.deleteIfExists(localPhotoPath);
            } catch (IOException e) {
                System.err.println("Не вдалося видалити локальне фото: " + e.getMessage());
            }
        }

        cvRepository.delete(cv);
    }
}