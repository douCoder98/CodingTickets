// ==================== ReservationCreateServlet.java ====================
package fr.coding.tickets.web.controller;

import java.io.IOException;

import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Reservation;
import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.service.TicketService;
import fr.coding.tickets.model.PlacesInsuffisantesException;
import fr.coding.tickets.model.ReservationInvalideException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/reservations/create")
public class ReservationCreateServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        // Vérifier que c'est un client
        if (!(user instanceof Client)) {
            session.setAttribute("errorMessage", "Seuls les clients peuvent réserver");
            response.sendRedirect(request.getContextPath() + "/events");
            return;
        }

        Client client = (Client) user;

        try {
            long eventId = Long.parseLong(request.getParameter("eventId"));
            int nbPlaces = Integer.parseInt(request.getParameter("nbPlaces"));

            TicketService ticketService = (TicketService) getServletContext()
                .getAttribute("ticketService");

            Reservation reservation = ticketService.reserver(client, eventId, nbPlaces);
            reservation.confirmer();

            // Message de succès
            session.setAttribute("successMessage",
                "Réservation confirmée ! " + nbPlaces + " place(s) réservée(s) pour un montant de " +
                String.format("%.2f", reservation.getMontantTotal()) + "€");

            response.sendRedirect(request.getContextPath() + "/reservations/history");

        } catch (PlacesInsuffisantesException e) {
            session.setAttribute("errorMessage", "Places insuffisantes : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/events");

        } catch (ReservationInvalideException e) {
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/events");

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Paramètres invalides");
            response.sendRedirect(request.getContextPath() + "/events");
        }
    }
}