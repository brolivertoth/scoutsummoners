package ch.scs.scoutsummoners.service;

import ch.scs.scoutsummoners.entity.Attachment;
import ch.scs.scoutsummoners.entity.Comment;
import ch.scs.scoutsummoners.entity.LargeEventPlan;
import ch.scs.scoutsummoners.entity.User;
import ch.scs.scoutsummoners.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public Attachment saveAttachment(MultipartFile file, User uploadedBy, LargeEventPlan plan, Comment comment) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file to disk
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create attachment entity
        Attachment attachment = new Attachment(
                originalFilename,
                filePath.toString(),
                file.getContentType(),
                file.getSize(),
                uploadedBy
        );
        attachment.setLargeEventPlan(plan);
        attachment.setComment(comment);

        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getPlanAttachments(LargeEventPlan plan) {
        return attachmentRepository.findByLargeEventPlan(plan);
    }

    public List<Attachment> getCommentAttachments(Comment comment) {
        return attachmentRepository.findByComment(comment);
    }

    public Attachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id).orElse(null);
    }

    public void deleteAttachment(Long id) throws IOException {
        Attachment attachment = attachmentRepository.findById(id).orElse(null);
        if (attachment != null) {
            // Delete file from disk
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
            
            // Delete from database
            attachmentRepository.deleteById(id);
        }
    }
}
