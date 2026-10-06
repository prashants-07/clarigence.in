package in.clarigence.contactapi.service;

import jakarta.mail.MessagingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.net.ssl.SSLException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

/** Do not log raw provider messages: they can contain credentials or enquiry contents. */
final class MailFailureDiagnostic {
    private MailFailureDiagnostic() { }

    static String describe(Throwable failure) {
        String diagnostic = describe(failure, Collections.newSetFromMap(new IdentityHashMap<>()));
        return diagnostic.isEmpty() ? "SMTP send failed; check sender/recipient configuration and provider delivery logs (raw error omitted to protect secrets)" : diagnostic;
    }

    private static String describe(Throwable failure, Set<Throwable> seen) {
        if (failure == null || !seen.add(failure)) return "";
        if (failure instanceof MailAuthenticationException || failure instanceof jakarta.mail.AuthenticationFailedException)
            return "SMTP authentication rejected; verify username and password/app password";
        if (failure instanceof SocketTimeoutException)
            return "SMTP connection/read/write timed out; check host, port and outbound SMTP restrictions";
        if (failure instanceof ConnectException)
            return "SMTP connection refused/unreachable; check host, port and hosting network policy";
        if (failure instanceof UnknownHostException)
            return "SMTP hostname could not be resolved; check host and DNS";
        if (failure instanceof SSLException)
            return "SMTP TLS negotiation failed; check port, STARTTLS/SSL and certificate configuration";
        String nested = describe(failure.getCause(), seen);
        if (!nested.isEmpty()) return nested;
        if (failure instanceof MessagingException messaging) {
            nested = describe(messaging.getNextException(), seen);
            if (!nested.isEmpty()) return nested;
        }
        if (failure instanceof MailSendException sending) {
            for (Exception nestedFailure : sending.getMessageExceptions()) {
                nested = describe(nestedFailure, seen);
                if (!nested.isEmpty()) return nested;
            }
        }
        if (failure instanceof org.eclipse.angus.mail.smtp.SMTPAddressFailedException rejected)
            return "SMTP recipient rejected with status " + rejected.getReturnCode() + "; check recipient and provider restrictions";
        if (failure instanceof org.eclipse.angus.mail.smtp.SMTPSendFailedException rejected)
            return "SMTP send rejected with status " + rejected.getReturnCode() + "; check sender verification and provider limits";
        return "";
    }
}
