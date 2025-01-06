package fileshare.repository;

import fileshare.model.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileModel, Long> {
    Optional<FileModel> findByName(String name);
    Optional<FileModel> findByContentHash(byte[] contentHash);
}
