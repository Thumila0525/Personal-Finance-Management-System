package finance.servlet;

import finance.ExpenseCategory;
import finance.IncomeCategory;
import finance.dao.TransactionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/transactions")
public class TransactionServlet extends HttpServlet {
    private TransactionDAO transactionDAO;

    @Override
    public void init() throws ServletException {
        transactionDAO = new TransactionDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String type = request.getParameter("type");
            double amount = Double.parseDouble(request.getParameter("amount"));
            boolean success = false;

            if ("INCOME".equalsIgnoreCase(type)) {
                int accountId = Integer.parseInt(request.getParameter("accountId"));
                IncomeCategory category = IncomeCategory.valueOf(request.getParameter("category").toUpperCase());
                success = transactionDAO.addIncome(accountId, amount, category);

            } else if ("EXPENSE".equalsIgnoreCase(type)) {
                int accountId = Integer.parseInt(request.getParameter("accountId"));
                ExpenseCategory category = ExpenseCategory.valueOf(request.getParameter("category").toUpperCase());
                success = transactionDAO.addExpense(accountId, amount, category);

            } else if ("TRANSFER".equalsIgnoreCase(type)) {
                int fromAccountId = Integer.parseInt(request.getParameter("fromAccountId"));
                int toAccountId = Integer.parseInt(request.getParameter("toAccountId"));
                success = transactionDAO.addTransfer(fromAccountId, toAccountId, amount);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid transaction type\"}");
                return;
            }

            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\":\"Transaction recorded successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to record transaction\"}");
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid input data or unmapped category\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Server error occurred processing the transaction\"}");
        }
    }
}