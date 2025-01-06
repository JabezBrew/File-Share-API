package fileshare.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.PAYLOAD_TOO_LARGE)
public class SizeException extends RuntimeException {
    public SizeException() {
        super();
    }
}
