package com.example.cvgenerator.service;

import com.example.cvgenerator.model.CV;
import com.example.cvgenerator.model.User;
import com.example.cvgenerator.repository.CVRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
    private static final int MAX_IMAGE_SIZE_KB = 500; // Максимальний розмір зображення 500 KB

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

            // Зменшуємо розмір зображення, якщо воно більше 500 КБ
            byte[] imageData;
            if (photoFile.getSize() > MAX_IMAGE_SIZE_KB * 1024) { // 500 KB
                imageData = resizeImageForLocalStorage(photoFile);
                System.out.println("Зображення було зменшено з " + (photoFile.getSize() / 1024) + "KB до "
                        + (imageData.length / 1024) + "KB");
            } else {
                imageData = photoFile.getBytes();
            }

            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, imageData);
            cv.setPhotoPath(fileName);

            System.out.println("Фото збережено локально за шляхом: " + filePath.toString());
        } catch (Exception e) {
            System.err.println("Помилка при завантаженні фото локально: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] resizeImageForLocalStorage(MultipartFile file) {
        try {
            // Читаємо оригінальне зображення
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                return file.getBytes();
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
            ImageIO.write(resizedImage, "jpeg", bos);

            return bos.toByteArray();
        } catch (Exception e) {
            try {
                System.err.println("Помилка при зменшенні зображення: " + e.getMessage());
                return file.getBytes(); // Повертаємо оригінальні дані, якщо не вдалося зменшити
            } catch (IOException ioe) {
                System.err.println("Критична помилка при обробці зображення: " + ioe.getMessage());
                return new byte[0]; // Порожній масив у випадку критичної помилки
            }
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

            // Зменшуємо розмір зображення, якщо воно більше 500 КБ
            byte[] imageData;
            if (photoFile.getSize() > MAX_IMAGE_SIZE_KB * 1024) { // 500 KB
                imageData = resizeImageForLocalStorage(photoFile);
                System.out.println("Зображення було зменшено з " + (photoFile.getSize() / 1024) + "KB до "
                        + (imageData.length / 1024) + "KB");
            } else {
                imageData = photoFile.getBytes();
            }

            String fileName = UUID.randomUUID().toString() + "_" + photoFile.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, imageData);
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