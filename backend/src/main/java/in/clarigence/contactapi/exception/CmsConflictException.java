package in.clarigence.contactapi.exception;
public class CmsConflictException extends RuntimeException {
    public CmsConflictException(){super("This content changed in another session. Reload it before saving.");}
}
