package fr.coding.tickets.web.controller;

import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.model.Role;
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

@WebServlet("/events")
public class EventListServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		Utilisateur user = (Utilisateur) session.getAttribute("user");

		TicketService ticketService = (TicketService) getServletContext().getAttribute("ticketService");

		List<Evenement> evenements = ticketService.listerEvenementsDisponibles();

		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("    <meta charset='UTF-8'>");
		out.println("    <title>Événements disponibles</title>");
		out.println("    <style>");
		out.println(
				"        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; }");
		out.println(
				"        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }");
		out.println("        h1 { color: #333; margin: 0; }");
		out.println("        .user-info { text-align: right; }");
		out.println(
				"        .logout-btn { padding: 8px 16px; background-color: #dc3545; color: white; text-decoration: none; border-radius: 4px; }");
		out.println(
				"        .event-card { border: 1px solid #ddd; padding: 20px; margin-bottom: 20px; border-radius: 8px; background-color: #f9f9f9; }");
		out.println(
				"        .event-title { color: #007bff; font-size: 20px; font-weight: bold; margin-bottom: 10px; }");
		out.println("        .event-info { margin: 5px 0; }");
		out.println("        .event-price { color: #28a745; font-weight: bold; font-size: 18px; }");
		out.println("        .reserve-form { margin-top: 15px; display: flex; gap: 10px; align-items: center; }");
		out.println("        .reserve-form input { padding: 8px; width: 80px; }");
		out.println(
				"        .reserve-form button { padding: 8px 16px; background-color: #28a745; color: white; border: none; cursor: pointer; border-radius: 4px; }");
		out.println("        .reserve-form button:hover { background-color: #218838; }");
		out.println("        .nav-links { margin-bottom: 20px; }");
		out.println("        .nav-links a { margin-right: 15px; color: #007bff; text-decoration: none; }");
		out.println("        .nav-links a:hover { text-decoration: underline; }");
		out.println("        .no-events { text-align: center; padding: 40px; color: #666; }");
		out.println("    </style>");
		out.println("</head>");
		out.println("<body>");

		out.println("    <div class='header'>");
		out.println("        <h1>Événements disponibles</h1>");
		out.println("        <div class='user-info'>");
		out.println("            <p>Connecté en tant que : <strong>" + user.getNom() + "</strong> (" + user.getRole()
				+ ")</p>");
		out.println("            <a href='logout' class='logout-btn'>Se déconnecter</a>");
		out.println("        </div>");
		out.println("    </div>");

		out.println("    <div class='nav-links'>");
		out.println("        <a href='events'>📅 Événements</a>");
		if (user.getRole() == Role.CLIENT) {
			out.println("        <a href='reservations/history'>📋 Mes réservations</a>");
		}
		out.println("    </div>");

		if (evenements.isEmpty()) {
			out.println("    <div class='no-events'>");
			out.println("        <h2>Aucun événement disponible pour le moment</h2>");
			out.println("    </div>");
		} else {
			if (user.getRole() == Role.CLIENT) {
				for (Evenement event : evenements) {
					out.println("    <div class='event-card'>");
					out.println("        <div class='event-title'>" + event.getTitre() + "</div>");
					out.println(
							"        <div class='event-info'>📍 <strong>Lieu :</strong> " + event.getLieu() + "</div>");
					out.println("        <div class='event-info'>📅 <strong>Date :</strong> "
							+ event.getDate().format(formatter) + "</div>");
					out.println("        <div class='event-info'>📝 <strong>Description :</strong> "
							+ event.getDescription() + "</div>");
					out.println("        <div class='event-info'>🎟️ <strong>Places disponibles :</strong> "
							+ event.getNbPlacesRestantes() + " / " + event.getNbPlacesTotales() + "</div>");
					out.println("        <div class='event-price'>💰 " + event.getPrixBase() + " €</div>");

					out.println("        <form class='reserve-form' method='POST' action='reservations/create'>");
					out.println("            <input type='hidden' name='eventId' value='" + event.getId() + "'>");
					out.println("            <label for='nbPlaces'>Nombre de places :</label>");
					out.println("            <input type='number' id='nbPlaces' name='nbPlaces' value='1' min='1' max='"
							+ event.getNbPlacesRestantes() + "' required>");
					out.println("            <button type='submit'>Réserver</button>");
					out.println("        </form>");
					out.println("    </div>");
				}
			} else {
				for (Evenement event : evenements) {
					if (event.getOrganisateur().getId() == user.getId()) {
						out.println("    <div class='event-card'>");
						out.println("        <div class='event-title'>" + event.getTitre() + "</div>");
						out.println("        <div class='event-info'>📍 <strong>Lieu :</strong> " + event.getLieu()
								+ "</div>");
						out.println("        <div class='event-info'>📅 <strong>Date :</strong> "
								+ event.getDate().format(formatter) + "</div>");
						out.println("        <div class='event-info'>📝 <strong>Description :</strong> "
								+ event.getDescription() + "</div>");
						out.println("        <div class='event-info'>🎟️ <strong>Places disponibles :</strong> "
								+ event.getNbPlacesRestantes() + " / " + event.getNbPlacesTotales() + "</div>");
						out.println("        <div class='event-price'>💰 " + event.getPrixBase() + " €</div>");
						out.println("    </div>");
					}
				}
			}
		}

		out.println("</body>");
		out.println("</html>");
	}
}
