package bitc.full502.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorage {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Files.createDirectories(Paths.get(uploadDir));
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
        Path target = Paths.get(uploadDir).resolve(filename).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return filename; // 브라우저에서 /uploads/{filename} 으로 접근
    }
}
