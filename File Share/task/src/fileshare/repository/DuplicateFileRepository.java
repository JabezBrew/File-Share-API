package fileshare.repository;

import fileshare.model.DuplicateFile;
import fileshare.model.FileModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Repository
public interface DuplicateFileRepository extends JpaRepository<DuplicateFile, Long> {
    Optional<DuplicateFile> findByUniqueName(String name);

    @Query("SELECT d.fileModel FROM DuplicateFile d WHERE d.uniqueName = ?1")
    Optional<FileModel> findFileModelByDuplicateFileName(String uniqueName);
}
