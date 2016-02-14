package de.hybris.platform.ycommercewebservices.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping(value="/hello", produces = "text/html; charset=utf-8")
    public @ResponseBody String sayHello(HttpServletRequest request) {
        return "Hello World!";
    }

}