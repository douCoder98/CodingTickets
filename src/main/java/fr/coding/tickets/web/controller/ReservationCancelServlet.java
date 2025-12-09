package fr.coding.tickets.web.controller;

import java.io.IOException;

import fr.coding.tickets.model.AnnulationTardiveException;
import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.ReservationInvalideException;
import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.service.TicketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/reservations/cancel")
public class ReservationCancelServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Utilisateur user = (Utilisateur) session.getAttribute("user");

        if (!(user instanceof Client)) {
            response.sendRedirect(request.getContextPath() + "/events");
            return;
        }

        Client client = (Client) user;

        try {
            long reservationId = Long.parseLong(request.getParameter("reservationId"));

            TicketService ticketService = (TicketService) getServletContext()
                .getAttribute("ticketService");

            ticketService.annulerReservation(reservationId, client);

            session.setAttribute("successMessage", "Réservation annulée avec succès");

        } catch (AnnulationTardiveException e) {
            session.setAttribute("errorMessage", "Impossible d'annuler : " + e.getMessage());

        } catch (ReservationInvalideException e) {
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());

        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "Paramètres invalides");
        }

        response.sendRedirect(request.getContextPath() + "/reservations/history");
    }
}