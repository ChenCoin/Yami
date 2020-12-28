package xyz.cyan;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import xyz.cyan.db.Database;
import xyz.cyan.server.NoteList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main extends HttpServlet {

    private List<Router> routers() {
        // method should be UpperCase
        return Arrays.asList(
                new Router("GET", "/", NoteList.class),
                new Router("GET", "/api", NoteList.class)
        );
    }

    public static void main(String[] args) throws Exception {
        Database db = new Database();
        db.init();
        db.add();
        db.query();

        if (true) return;

        int port = 8080;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception ignore) {
        }

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new Main()), "/*");
        server.setHandler(context);
        server.start();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestPath = req.getRequestURI().toUpperCase();
        String requestMethod = req.getMethod().toUpperCase();
        for (Router router : routers()) {
            String path = router.path;
            String method = router.method;
            if (path.equals(requestPath) && method.equals(requestMethod)) {
                try {
                    router.api.newInstance().handler(req, resp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private static class Router {
        String method;
        String path;
        Class<? extends Api> api;

        Router(String method, String path, Class<? extends Api> api) {
            this.method = method;
            this.path = path;
            this.api = api;
        }
    }
}