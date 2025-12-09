// ==================== LogoutServlet.java (inchangé) ====================
package fr.coding.tickets.web.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            String userName = "Utilisateur";
            if (session.getAttribute("user") != null) {
                userName = ((fr.coding.tickets.model.Utilisateur) session.getAttribute("user")).getNom();
            }

            session.invalidate();
            System.out.println("✓ Déconnexion de : " + userName);
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}