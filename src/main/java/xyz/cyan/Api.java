package xyz.cyan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Api {
    void handler(HttpServletRequest req, HttpServletResponse resp);
}
