import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet("/api/admin-stats")
public class AdminStatsServlet extends HttpServlet {

    // Enable CORS for frontend to call this servlet
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set CORS headers
        setCORSHeaders(response);

        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Create JSON response with admin statistics
            JSONObject stats = new JSONObject();
            stats.put("totalProducts", 156);
            stats.put("totalUsers", 89);
            stats.put("totalOrders", 234);
            stats.put("revenue", 45670.50);
            stats.put("serverTime", System.currentTimeMillis());
            stats.put("serverStatus", "Active");
            stats.put("message", "Statistics fetched from Java Servlet Backend");

            // Send JSON response
            PrintWriter out = response.getWriter();
            out.print(stats.toString());
            out.flush();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // Helper method to set CORS headers
    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}
