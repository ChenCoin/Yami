package xyz.cyan.server

import xyz.cyan.Api
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NoteList : Api {
    override fun handler(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.writer.print(req.pathInfo + " " + req.method)
    }
}