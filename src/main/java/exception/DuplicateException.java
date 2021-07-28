package exception;

public class DuplicateException extends RuntimeException {
    public DuplicateException(String e) {
        super(e);
    }
}
