package com.geekymv.spring;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public class HelloController extends AbstractController {

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response) throws Exception {
        System.out.println("hello = " + request.getMethod());

        PrintWriter writer = response.getWriter();
        writer.println("hello = " + request.getMethod());
        writer.close();

        return null;
    }
}
