package fileshare;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


import java.util.logging.Logger;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1")
public class FileController {

    private static final Logger logger = Logger.getLogger(FileController.class.getName());
    private final FileStorageService fileStorageService;

    @Value("${uploads.dir}")
    private String uploadsDir;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam MultipartFile file) throws IOException {
        String fileLocation= fileStorageService.store(file);
        System.out.println(fileLocation);
        return ResponseEntity
                .status(201)
                .header("Location", fileLocation)
                .build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        String[] fileDetails = fileStorageService.download(id);
        Path path = Path.of(uploadsDir, fileDetails[0]);
        if (Files.exists(path)) {
            try {
                byte[] file = Files.readAllBytes(path);
                return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=" + fileDetails[1])
                        .header("Content-Type", fileDetails[2])
                        .body(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/info")
    public ResponseEntity<?> info() {

        try (Stream<Path> files = Files.walk(Paths.get(uploadsDir))) {
            long numOfFiles = files.filter(p -> p.toFile().isFile()).count();
            long totalSize = Files.walk(Paths.get(uploadsDir))
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();

            return ResponseEntity.ok().body(
                    Map.of("total_files", numOfFiles, "total_bytes", totalSize)
            );

        } catch (IOException e) {
            logger.warning("Directory does not exist or an error occurred while calculating the file info. " + e.getMessage());
            return ResponseEntity.badRequest().body("Directory does not exist or an error occurred while calculating the file info.");
        }
    }
}
