package fileshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DuplicateFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String uniqueName;
    private String originalFileName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_model_id", referencedColumnName = "id")
    private FileModel fileModel;

    public DuplicateFile(String uniqueName, String originalFileName, FileModel fileModel) {
        this.uniqueName = uniqueName;
        this.originalFileName = originalFileName;
        this.fileModel = fileModel;
    }
}
