package mas.curs.infsys.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutController {
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contacts") 
    public String contacts() {
        return "contacts";
    }
}
