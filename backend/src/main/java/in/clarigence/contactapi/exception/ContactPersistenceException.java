package in.clarigence.contactapi.exception;

public class ContactPersistenceException extends RuntimeException {

    public ContactPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
