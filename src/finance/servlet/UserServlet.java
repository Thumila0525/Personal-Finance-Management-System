package finance.servlet;

import finance.User;
import finance.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet({"/api/register", "/api/login", "/api/me", "/api/logout"})
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        if ("/api/me".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("user") != null) {
                User user = (User) session.getAttribute("user");
                out.print("{\"id\":" + user.getId() + ", \"name\":\"" + user.getUsername() + "\", \"email\":\"" + user.getEmail() + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"Not authenticated\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Endpoint not found\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Read the raw JSON string sent by JavaScript fetch()
        String body = readRequestBody(request);

        if ("/api/register".equals(path)) {
            String name = extractJsonValue(body, "name");
            String email = extractJsonValue(body, "email");
            String password = extractJsonValue(body, "password");

            User newUser = new User(name, email, password);
            boolean isCreated = userDAO.createUser(newUser);

            if (isCreated) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", newUser);
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\":\"Account created successfully!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create user or email already exists.\"}");
            }

        } else if ("/api/login".equals(path)) {
            String email = extractJsonValue(body, "email");
            String password = extractJsonValue(body, "password");

            User user = userDAO.getUserByEmail(email);

            if (user != null && user.getPassword().equals(password)) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\":\"Login successful!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\":\"Invalid email or password.\"}");
            }

        } else if ("/api/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"message\":\"Logged out successfully.\"}");

        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\":\"Endpoint not found\"}");
        }
    }

    // Reads raw JSON body stream sent by fetch()
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    // Helper method to extract string values from JSON
    private String extractJsonValue(String json, String key) {
        if (json == null || key == null) return "";
        String pattern = "\"" + key + "\"";
        int startKey = json.indexOf(pattern);
        if (startKey == -1) return "";

        int startColon = json.indexOf(":", startKey);
        if (startColon == -1) return "";

        int firstQuote = json.indexOf("\"", startColon);
        if (firstQuote == -1) return "";

        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return "";

        return json.substring(firstQuote + 1, secondQuote);
    }
}