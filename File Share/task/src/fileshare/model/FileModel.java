package fileshare.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FileModel {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String name;
    private String originalName;
    private String contentType;
    private byte[] contentHash;

    public FileModel(String name, String originalName, String contentType, byte[] contentHash) {
        this.name = name;
        this.originalName = originalName;
        this.contentType = contentType;
        this.contentHash = contentHash;
    }
}
