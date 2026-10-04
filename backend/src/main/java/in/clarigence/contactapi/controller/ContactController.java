package in.clarigence.contactapi.controller;

import in.clarigence.contactapi.dto.ContactRequest;
import in.clarigence.contactapi.dto.ContactResponse;
import in.clarigence.contactapi.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ContactResponse> createContact(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(request));
    }
}
