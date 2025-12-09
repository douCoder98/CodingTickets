package fr.coding.tickets.web.controller;

import java.io.IOException;
import java.util.List;

import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.service.TicketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/events")
public class EventListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        TicketService ticketService = (TicketService) getServletContext()
            .getAttribute("ticketService");

        // Récupérer la liste des événements disponibles
        List<Evenement> evenements = ticketService.listerEvenementsDisponibles();

        // Récupérer les messages flash depuis la session
        String successMessage = (String) session.getAttribute("successMessage");
        String errorMessage = (String) session.getAttribute("errorMessage");

        // Mettre les données dans la request pour la JSP
        request.setAttribute("events", evenements);
        request.setAttribute("user", user);

        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("successMessage");
        }

        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("errorMessage");
        }

        // Forward vers la JSP
        request.getRequestDispatcher("/WEB-INF/jsp/events.jsp").forward(request, response);
    }
}