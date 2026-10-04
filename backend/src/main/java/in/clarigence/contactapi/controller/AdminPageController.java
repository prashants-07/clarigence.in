package in.clarigence.contactapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping({"/admin", "/admin/"})
    public String adminDashboard() {
        return "forward:/admin/index.html";
    }
}
