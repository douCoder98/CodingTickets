<!-- ==================== events.jsp ==================== -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="fr.coding.tickets.model.*" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Événements - Coding Tickets</title>
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
        .events-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
            gap: 25px;
        }
        .event-card {
            background: white;
            border-radius: 10px;
            padding: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            transition: transform 0.3s, box-shadow 0.3s;
        }
        .event-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }
        .event-title {
            color: #667eea;
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 15px;
        }
        .event-info {
            margin: 10px 0;
            color: #555;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .event-price {
            font-size: 24px;
            color: #28a745;
            font-weight: bold;
            margin: 15px 0;
        }
        .reservation-form {
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid #eee;
            display: flex;
            gap: 10px;
            align-items: center;
        }
        .reservation-form label {
            font-weight: 500;
            color: #333;
        }
        .reservation-form input[type="number"] {
            width: 80px;
            padding: 8px;
            border: 2px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
        }
        .reservation-form button {
            padding: 10px 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
        }
        .reservation-form button:hover {
            transform: scale(1.05);
        }
        .no-events {
            text-align: center;
            padding: 60px 20px;
            background: white;
            border-radius: 10px;
            color: #666;
        }
        .no-events h2 {
            color: #999;
            margin-bottom: 10px;
        }
        .badge {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
        }
        .badge-success {
            background-color: #d4edda;
            color: #155724;
        }
        .badge-warning {
            background-color: #fff3cd;
            color: #856404;
        }
    </style>
</head>
<body>
    <%
    Utilisateur user = (Utilisateur) request.getAttribute("user");
    List<Evenement> events = (List<Evenement>) request.getAttribute("events");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm");
    %>
    
    <!-- Header -->
    <div class="header">
        <div class="header-content">
            <h1>Événements disponibles</h1>
            <div class="user-info">
                <span class="user-name">👤 <%= user.getNom() %> (<%= user.getRole() %>)</span>
                <a href="<%= request.getContextPath() %>/logout" class="logout-btn">🚪 Déconnexion</a>
            </div>
        </div>
    </div>
    
    <!-- Navigation -->
    <div class="nav">
        <div class="nav-content">
            <a href="<%= request.getContextPath() %>/events" class="active">📅 Événements</a>
            <% if (user.getRole() == Role.CLIENT) { %>
                <a href="<%= request.getContextPath() %>/reservations/history">📋 Mes réservations</a>
            <% } %>
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
        
        <!-- Liste des événements -->
        <% if (events == null || events.isEmpty()) { %>
            <div class="no-events">
                <h2>Aucun événement disponible</h2>
                <p>Revenez plus tard pour découvrir de nouveaux événements !</p>
            </div>
        <% } else { %>
            <div class="events-grid">
                <% for (Evenement event : events) { %>
                    <div class="event-card">
                        <div class="event-title"><%= event.getTitre() %></div>
                        
                        <div class="event-info">
                            📍 <strong>Lieu :</strong> <%= event.getLieu() %>
                        </div>
                        
                        <div class="event-info">
                            📅 <strong>Date :</strong> <%= event.getDate().format(formatter) %>
                        </div>
                        
                        <div class="event-info">
                            📝 <%= event.getDescription() %>
                        </div>
                        
                        <div class="event-info">
                            🎟️ <strong>Places :</strong> 
                            <%= event.getNbPlacesRestantes() %> / <%= event.getNbPlacesTotales() %>
                            <% if (event.getNbPlacesRestantes() > 10) { %>
                                <span class="badge badge-success">Disponible</span>
                            <% } else if (event.getNbPlacesRestantes() > 0) { %>
                                <span class="badge badge-warning">Places limitées</span>
                            <% } %>
                        </div>
                        
                        <div class="event-price">
                            💰 <%= String.format("%.2f", event.getPrixBase()) %> €
                        </div>
                        
                        <!-- Formulaire de réservation (uniquement pour les clients) -->
                        <% if (user.getRole() == Role.CLIENT && event.getNbPlacesRestantes() > 0) { %>
                            <form class="reservation-form" method="POST" 
                                  action="<%= request.getContextPath() %>/reservations/create">
                                <input type="hidden" name="eventId" value="<%= event.getId() %>">
                                <label for="nbPlaces_<%= event.getId() %>">Places :</label>
                                <input type="number" id="nbPlaces_<%= event.getId() %>" name="nbPlaces" 
                                       value="1" min="1" max="<%= event.getNbPlacesRestantes() %>" required>
                                <button type="submit">Réserver</button>
                            </form>
                        <% } %>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>
</body>
</html>