package fr.coding.tickets.web.controller;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.service.TicketService;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupérer le message d'erreur depuis la session
        HttpSession session = request.getSession(false);
        if (session != null) {
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
        }

        // Forward vers la JSP
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // Récupération du service depuis le contexte
        TicketService ticketService = (TicketService) getServletContext()
            .getAttribute("ticketService");

        // Tentative d'authentification
        Utilisateur utilisateur = ticketService.authentifier(email, password);

        if (utilisateur != null) {
            // Authentification réussie
            HttpSession session = request.getSession(true);
            session.setAttribute("user", utilisateur);

            System.out.println("✓ Utilisateur connecté : " + utilisateur.getNom() +
                             " (" + utilisateur.getRole() + ")");

            // Redirection vers la liste des événements
            response.sendRedirect(request.getContextPath() + "/events");
        } else {
            // Authentification échouée
            System.out.println("✗ Échec d'authentification pour : " + email);

            HttpSession session = request.getSession(true);
            session.setAttribute("errorMessage", "Email ou mot de passe incorrect");

            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}