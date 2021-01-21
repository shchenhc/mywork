package elm.data.test.myservlet;

import java.io.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    private String message;

    public void init() {
        message = "Hello my friend!!";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        CustomerData cd = new CustomerData();
        try {
            cd.getShopToken();
            cd.getShopOrderList();
            cd.getShopOrderDetail();
        }catch (Exception e) {
            e.printStackTrace();
        }
        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("<h1>sign:" + cd.SHA256Sign + "</h1>");
        out.println("<h1>TOKENsign:" + cd.TokenURL + "</h1>");
        out.println("<h1>TOKEN:" + cd.Token + "</h1>");
        out.println("<h1>OrderDetailReturn:" + cd.OrderDetailReturn + "</h1>");
        out.println("</body></html>");
    }

/*    public void doPost(HttpServletRequest request,HttpServletResponse response) throws IOException{
        response.setContentType("text/html");

        // Hello
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }*/

    public void destroy() {
    }
}