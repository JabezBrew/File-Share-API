package fileshare;

import fileshare.errors.NotFoundException;
import fileshare.errors.SizeException;
import fileshare.errors.UnsupportedMediaException;
import fileshare.model.DuplicateFile;
import fileshare.model.FileModel;
import fileshare.repository.DuplicateFileRepository;
import fileshare.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    @Value("${uploads.dir}")
    private String uploadsDir;
    private static final Logger logger = Logger.getLogger(FileStorageService.class.getName());
    private static final long MAX_DIRECTORY_SIZE = 200_000L;
    private static final List<String> allowedFileTypes = List.of("image/png", "image/jpeg", "text/plain");

    private final FileRepository fileRepository;
    private final DuplicateFileRepository duplicateFileRepository;

    public FileStorageService(FileRepository fileRepository, DuplicateFileRepository duplicateFileRepository) {
        this.fileRepository = fileRepository;
        this.duplicateFileRepository = duplicateFileRepository;
    }

    @Transactional
    public String store(MultipartFile file) throws IOException {
        String uniqueName = String.valueOf(UUID.randomUUID());
        Path path = Path.of(uploadsDir, uniqueName);
        byte[] contentHash = calculateContentHash(file);
        Optional<FileModel> storedFile = fileRepository.findByContentHash(contentHash);

        if (file.getSize() < 50_000) {

            if (MAX_DIRECTORY_SIZE - getDirectorySize() > file.getSize() || ( (MAX_DIRECTORY_SIZE - getDirectorySize() < file.getSize()) &&  storedFile.isPresent()) ) {

                if (allowedFileTypes.contains(file.getContentType())) {

                    if (verifyFileContent(file)) {

                        if (storedFile.isEmpty()) {
                            FileModel uploadFileModel = new FileModel(uniqueName, file.getOriginalFilename(),
                                    file.getContentType(), contentHash);
                            fileRepository.save(uploadFileModel);
                            file.transferTo(path); // this will save the file to disk with uniqueName
                        } else {
                            DuplicateFile duplicateFile = new DuplicateFile(uniqueName, file.getOriginalFilename(), storedFile.get());
                            duplicateFileRepository.save(duplicateFile);
                        }
                        return "http://localhost:8888/api/v1/download/" + uniqueName;

                    } else {
                        throw new UnsupportedMediaException();
                    }

                } else {
                    throw new UnsupportedMediaException();
                }
            } else {
                throw new SizeException();
            }

        } else {
            throw new SizeException();
        }
    }

    @Transactional
    public String[] download(String identifier) {
        System.out.println(identifier);
        Optional<FileModel> fileModel = fileRepository.findByName(identifier);
        Optional<DuplicateFile> duplicateFile = duplicateFileRepository.findByUniqueName(identifier);
        // index 0 = unique name; index 1 = original name; index 2 = content-type
        String[] fileDetails = new String[3];

        if (fileModel.isEmpty() && duplicateFile.isPresent()) {
            System.out.println("In here");
            FileModel fileModel1 = duplicateFileRepository.findFileModelByDuplicateFileName(identifier).get();
            fileDetails[0] = fileModel1.getName();
            fileDetails[1] = duplicateFile.get().getOriginalFileName();
            fileDetails[2] = fileModel1.getContentType();
        } else if (fileModel.isEmpty()) {
            throw new NotFoundException();
        } else {
            System.out.println("In here 2");
            fileDetails[0] = fileModel.get().getName();
            fileDetails[1] = fileModel.get().getOriginalName();
            fileDetails[2] = fileModel.get().getContentType();
        }
        return fileDetails;
    }



    public boolean verifyFileContent(MultipartFile file) throws IOException {
        byte[] contents = file.getBytes();
        byte[] pngSignature = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] jpgSignatureStart = new byte[] {(byte) 0xFF, (byte) 0xD8};
        byte[] jpgSignatureEnd = new byte[] {(byte) 0xFF, (byte) 0xD9};
        boolean mediaTypeIsValid = false;

        if (Objects.equals(file.getContentType(), "image/png")) {
            mediaTypeIsValid = Arrays.equals(pngSignature, Arrays.copyOfRange(contents, 0, pngSignature.length));
        } else if (Objects.equals(file.getContentType(), "image/jpeg")) {
            mediaTypeIsValid = Arrays.equals(jpgSignatureStart, Arrays.copyOfRange(contents, 0, jpgSignatureStart.length)) &&
                    Arrays.equals(jpgSignatureEnd, Arrays.copyOfRange(contents, contents.length - jpgSignatureEnd.length, contents.length));
        } else {

            try {
                CharsetDecoder utf8Decoder = StandardCharsets.UTF_8.newDecoder();
                utf8Decoder.reset();
                utf8Decoder.decode(ByteBuffer.wrap(contents));
                // no exception - the byte array contains only valid UTF-8 byte sequences hence it's a text/plain file
                mediaTypeIsValid = true;
            } catch (CharacterCodingException e) {
                logger.warning("The byte array has some sequences that are not valid UTF-8 byte sequences. " + e.getMessage());
            }
        }
        return mediaTypeIsValid;
    }

    public long getDirectorySize() {
        long directorySize;
        try (Stream<Path> files = Files.walk(Paths.get(uploadsDir))) {
            directorySize = files.filter(entity -> entity.toFile().isFile())
                    .mapToLong(entity -> entity.toFile().length())
                    .sum();
        } catch (IOException e) {
            directorySize = 0;
            logger.warning("Directory does not exist or an error occurred while calculating the file info. " + e.getMessage());
        }
        return directorySize;
    }

    public byte[] calculateContentHash(MultipartFile file) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(file.getBytes());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }

    }

}