package fr.coding.tickets.web.controller;

import fr.coding.tickets.model.*;
import fr.coding.tickets.service.TicketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/reservations/history")
public class ReservationHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Utilisateur user = (Utilisateur) session.getAttribute("user");
        
        if (!(user instanceof Client)) {
            response.sendRedirect(request.getContextPath() + "/events");
            return;
        }
        
        Client client = (Client) user;
        
        TicketService ticketService = (TicketService) getServletContext()
            .getAttribute("ticketService");
        
        List<Reservation> reservations = ticketService.listerReservationsClient(client);
        
        String successMessage = (String) session.getAttribute("successMessage");
        String errorMessage = (String) session.getAttribute("errorMessage");
        if (successMessage != null) session.removeAttribute("successMessage");
        if (errorMessage != null) session.removeAttribute("errorMessage");
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <title>Mes réservations</title>");
        out.println("    <style>");
        out.println("        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; }");
        out.println("        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }");
        out.println("        h1 { color: #333; margin: 0; }");
        out.println("        .nav-links { margin-bottom: 20px; }");
        out.println("        .nav-links a { margin-right: 15px; color: #007bff; text-decoration: none; }");
        out.println("        .reservation-card { border: 1px solid #ddd; padding: 20px; margin-bottom: 20px; border-radius: 8px; }");
        out.println("        .reservation-card.confirmee { background-color: #d4edda; border-color: #c3e6cb; }");
        out.println("        .reservation-card.annulee { background-color: #f8d7da; border-color: #f5c6cb; }");
        out.println("        .reservation-card.en-attente { background-color: #fff3cd; border-color: #ffeaa7; }");
        out.println("        .reservation-title { font-size: 18px; font-weight: bold; margin-bottom: 10px; }");
        out.println("        .reservation-info { margin: 5px 0; }");
        out.println("        .cancel-btn { padding: 8px 16px; background-color: #dc3545; color: white; border: none; cursor: pointer; border-radius: 4px; margin-top: 10px; }");
        out.println("        .cancel-btn:hover { background-color: #c82333; }");
        out.println("        .success { color: #155724; background-color: #d4edda; padding: 10px; border-radius: 4px; margin-bottom: 15px; }");
        out.println("        .error { color: #721c24; background-color: #f8d7da; padding: 10px; border-radius: 4px; margin-bottom: 15px; }");
        out.println("        .no-reservations { text-align: center; padding: 40px; color: #666; }");
        out.println("        .statut-badge { padding: 5px 10px; border-radius: 4px; font-weight: bold; display: inline-block; }");
        out.println("        .statut-confirmee { background-color: #28a745; color: white; }");
        out.println("        .statut-annulee { background-color: #dc3545; color: white; }");
        out.println("        .statut-en-attente { background-color: #ffc107; color: black; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        
        out.println("    <div class='header'>");
        out.println("        <h1>📋 Mes réservations</h1>");
        out.println("    </div>");
        
        out.println("    <div class='nav-links'>");
        out.println("        <a href='../events'>📅 Événements</a>");
        out.println("        <a href='history'>📋 Mes réservations</a>");
        out.println("        <a href='../logout'>🚪 Se déconnecter</a>");
        out.println("    </div>");
        
        if (successMessage != null) {
            out.println("    <div class='success'>" + successMessage + "</div>");
        }
        if (errorMessage != null) {
            out.println("    <div class='error'>" + errorMessage + "</div>");
        }
        
        if (reservations.isEmpty()) {
            out.println("    <div class='no-reservations'>");
            out.println("        <h2>Vous n'avez aucune réservation</h2>");
            out.println("        <p><a href='../events'>Parcourir les événements disponibles</a></p>");
            out.println("    </div>");
        } else {
            for (Reservation res : reservations) {
                String statutClass = res.getStatut().toString().toLowerCase().replace("_", "-");
                
                out.println("    <div class='reservation-card " + statutClass + "'>");
                out.println("        <div class='reservation-title'>Réservation #" + res.getId() + "</div>");
                out.println("        <div class='reservation-info'>🎭 <strong>Événement :</strong> " + res.getEvenement().getTitre() + "</div>");
                out.println("        <div class='reservation-info'>📍 <strong>Lieu :</strong> " + res.getEvenement().getLieu() + "</div>");
                out.println("        <div class='reservation-info'>📅 <strong>Date événement :</strong> " + res.getEvenement().getDate().format(formatter) + "</div>");
                out.println("        <div class='reservation-info'>🎟️ <strong>Nombre de places :</strong> " + res.getNbPlaces() + "</div>");
                out.println("        <div class='reservation-info'>💰 <strong>Montant total :</strong> " + res.getMontantTotal() + " €</div>");
                out.println("        <div class='reservation-info'>📆 <strong>Date réservation :</strong> " + res.getDateReservation().format(formatter) + "</div>");
                
                String statutBadgeClass = "statut-" + statutClass;
                out.println("        <div class='reservation-info'><strong>Statut :</strong> <span class='statut-badge " + statutBadgeClass + "'>" + res.getStatut() + "</span></div>");
                
                if (res.getStatut() != StatutReservation.ANNULEE) {
                    out.println("        <form method='POST' action='cancel' style='display: inline;'>");
                    out.println("            <input type='hidden' name='reservationId' value='" + res.getId() + "'>");
                    out.println("            <button type='submit' class='cancel-btn' onclick='return confirm(\"Êtes-vous sûr de vouloir annuler cette réservation ?\")'>Annuler la réservation</button>");
                    out.println("        </form>");
                }
                
                out.println("    </div>");
            }
        }
        
        out.println("</body>");
        out.println("</html>");
    }
}
