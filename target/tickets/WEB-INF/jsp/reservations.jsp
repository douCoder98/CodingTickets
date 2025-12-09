<!-- ==================== reservations.jsp ==================== -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="fr.coding.tickets.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mes réservations - Coding Tickets</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f5f5f5;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .header-content {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .header h1 { font-size: 24px; }
        .user-info {
            display: flex;
            align-items: center;
            gap: 20px;
        }
        .user-name {
            background: rgba(255,255,255,0.2);
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
        }
        .logout-btn {
            background: rgba(255,255,255,0.3);
            color: white;
            padding: 8px 16px;
            text-decoration: none;
            border-radius: 5px;
            font-size: 14px;
            transition: background 0.3s;
        }
        .logout-btn:hover {
            background: rgba(255,255,255,0.4);
        }
        .nav {
            background: white;
            padding: 15px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .nav-content {
            max-width: 1200px;
            margin: 0 auto;
            display: flex;
            gap: 20px;
        }
        .nav a {
            color: #667eea;
            text-decoration: none;
            font-weight: 500;
            transition: color 0.3s;
        }
        .nav a:hover {
            color: #764ba2;
        }
        .nav a.active {
            color: #764ba2;
            border-bottom: 2px solid #764ba2;
            padding-bottom: 3px;
        }
        .container {
            max-width: 1200px;
            margin: 30px auto;
            padding: 0 20px;
        }
        .success, .error {
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
        }
        .success {
            background-color: #d4edda;
            color: #155724;
            border-left: 4px solid #28a745;
        }
        .error {
            background-color: #f8d7da;
            color: #721c24;
            border-left: 4px solid #dc3545;
        }
        .reservation-card {
            background: white;
            border-radius: 10px;
            padding: 25px;
            margin-bottom: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        .reservation-card.CONFIRMEE {
            border-left: 4px solid #28a745;
        }
        .reservation-card.EN_ATTENTE {
            border-left: 4px solid #ffc107;
        }
        .reservation-card.ANNULEE {
            border-left: 4px solid #dc3545;
            opacity: 0.7;
        }
        .reservation-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        .reservation-id {
            font-size: 18px;
            font-weight: 600;
            color: #333;
        }
        .statut-badge {
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
        }
        .statut-confirmee {
            background-color: #d4edda;
            color: #155724;
        }
        .statut-en-attente {
            background-color: #fff3cd;
            color: #856404;
        }
        .statut-annulee {
            background-color: #f8d7da;
            color: #721c24;
        }
        .reservation-info {
            margin: 10px 0;
            color: #555;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .event-title {
            font-size: 18px;
            color: #667eea;
            font-weight: 600;
            margin-bottom: 10px;
        }
        .montant {
            font-size: 20px;
            color: #28a745;
            font-weight: bold;
            margin: 15px 0;
        }
        .cancel-btn {
            background-color: #dc3545;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-weight: 600;
            transition: background-color 0.3s;
            margin-top: 15px;
        }
        .cancel-btn:hover {
            background-color: #c82333;
        }
        .no-reservations {
            text-align: center;
            padding: 60px 20px;
            background: white;
            border-radius: 10px;
            color: #666;
        }
        .no-reservations h2 {
            color: #999;
            margin-bottom: 15px;
        }
        .no-reservations a {
            display: inline-block;
            margin-top: 20px;
            padding: 12px 24px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-weight: 600;
            transition: transform 0.2s;
        }
        .no-reservations a:hover {
            transform: scale(1.05);
        }
    </style>
</head>
<body>
    <%
    Utilisateur user = (Utilisateur) request.getAttribute("user");
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    %>
    
    <!-- Header -->
    <div class="header">
        <div class="header-content">
            <h1>Mes réservations</h1>
            <div class="user-info">
                <span class="user-name">👤 <%= user.getNom() %></span>
                <a href="<%= request.getContextPath() %>/logout" class="logout-btn">🚪 Déconnexion</a>
            </div>
        </div>
    </div>
    
    <!-- Navigation -->
    <div class="nav">
        <div class="nav-content">
            <a href="<%= request.getContextPath() %>/events">📅 Événements</a>
            <a href="<%= request.getContextPath() %>/reservations/history" class="active">📋 Mes réservations</a>
        </div>
    </div>
    
    <!-- Contenu principal -->
    <div class="container">
        <!-- Messages -->
        <% if (successMessage != null && !successMessage.isEmpty()) { %>
            <div class="success">
                <strong>✅ Succès :</strong> <%= successMessage %>
            </div>
        <% } %>
        
        <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
            <div class="error">
                <strong>❌ Erreur :</strong> <%= errorMessage %>
            </div>
        <% } %>
        
        <!-- Liste des réservations -->
        <% if (reservations == null || reservations.isEmpty()) { %>
            <div class="no-reservations">
                <h2>Vous n'avez aucune réservation</h2>
                <p>Parcourez nos événements et réservez vos places dès maintenant !</p>
                <a href="<%= request.getContextPath() %>/events">Voir les événements</a>
            </div>
        <% } else { %>
            <% for (Reservation reservation : reservations) { %>
                <div class="reservation-card <%= reservation.getStatut() %>">
                    <div class="reservation-header">
                        <span class="reservation-id">Réservation #<%= reservation.getId() %></span>
                        <span class="statut-badge statut-<%= reservation.getStatut().toString().toLowerCase().replace("_", "-") %>">
                            <% 
                            String statutText = "";
                            switch (reservation.getStatut()) {
                                case CONFIRMEE: statutText = "✅ Confirmée"; break;
                                case EN_ATTENTE: statutText = "⏳ En attente"; break;
                                case ANNULEE: statutText = "❌ Annulée"; break;
                            }
                            %>
                            <%= statutText %>
                        </span>
                    </div>
                    
                    <div class="event-title">🎭 <%= reservation.getEvenement().getTitre() %></div>
                    
                    <div class="reservation-info">
                        📍 <strong>Lieu :</strong> <%= reservation.getEvenement().getLieu() %>
                    </div>
                    
                    <div class="reservation-info">
                        📅 <strong>Date de l'événement :</strong> 
                        <%= reservation.getEvenement().getDate().format(formatter) %>
                    </div>
                    
                    <div class="reservation-info">
                        🎟️ <strong>Nombre de places :</strong> <%= reservation.getNbPlaces() %>
                    </div>
                    
                    <div class="reservation-info">
                        📆 <strong>Date de réservation :</strong> 
                        <%= reservation.getDateReservation().format(formatter) %>
                    </div>
                    
                    <div class="montant">
                        💰 <%= String.format("%.2f", reservation.getMontantTotal()) %> €
                    </div>
                    
                    <!-- Bouton d'annulation -->
                    <% if (reservation.getStatut() != StatutReservation.ANNULEE) { %>
                        <form method="POST" action="<%= request.getContextPath() %>/reservations/cancel"
                              onsubmit="return confirm('Êtes-vous sûr de vouloir annuler cette réservation ?')">
                            <input type="hidden" name="reservationId" value="<%= reservation.getId() %>">
                            <button type="submit" class="cancel-btn">Annuler la réservation</button>
                        </form>
                    <% } %>
                </div>
            <% } %>
        <% } %>
    </div>
</body>
</html>