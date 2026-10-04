package in.clarigence.contactapi.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("The email or password is incorrect.");
    }
}
