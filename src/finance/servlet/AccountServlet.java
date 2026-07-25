package finance.servlet;

import finance.Account;
import finance.dao.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/accounts")
public class AccountServlet extends HttpServlet {
    private AccountDAO accountDAO;

    @Override
    public void init() throws ServletException {
        accountDAO = new AccountDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            String name = request.getParameter("name");
            double initialBalance = Double.parseDouble(request.getParameter("initialBalance"));
            String accountType = request.getParameter("accountType");

            boolean success = accountDAO.addAccount(userId, name, initialBalance, accountType);

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\":\"Account created successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to create account\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid input parameters\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            int userId = Integer.parseInt(request.getParameter("userId"));
            Account[] accounts = accountDAO.getAccountsByUserId(userId);

            // Manually building JSON response for the array
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < accounts.length; i++) {
                Account acc = accounts[i];
                json.append("{")
                        .append("\"accountId\":").append(acc.getAccountId()).append(",")
                        .append("\"name\":\"").append(acc.getName()).append("\",")
                        .append("\"balance\":").append(acc.getBalance())
                        .append("}");

                if (i < accounts.length - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid user ID format\"}");
        }
    }
}