<!-- ==================== login.jsp ==================== -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="fr.coding.tickets.model.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion - Coding Tickets</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .container {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            max-width: 400px;
            width: 100%;
        }
        h1 {
            color: #333;
            margin-bottom: 10px;
            text-align: center;
        }
        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }
        .error {
            background-color: #fee;
            color: #c33;
            padding: 12px;
            border-radius: 5px;
            margin-bottom: 20px;
            border-left: 4px solid #c33;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            color: #333;
            font-weight: 500;
        }
        input[type="text"],
        input[type="email"],
        input[type="password"] {
            width: 100%;
            padding: 12px;
            border: 2px solid #ddd;
            border-radius: 5px;
            font-size: 14px;
            transition: border-color 0.3s;
        }
        input:focus {
            outline: none;
            border-color: #667eea;
        }
        button {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 5px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
        }
        button:hover {
            transform: translateY(-2px);
        }
        .info-box {
            background-color: #e3f2fd;
            padding: 20px;
            border-radius: 5px;
            margin-top: 30px;
            border-left: 4px solid #2196F3;
        }
        .info-box h3 {
            color: #1976D2;
            margin-bottom: 10px;
            font-size: 16px;
        }
        .info-box ul {
            list-style: none;
            margin-top: 10px;
        }
        .info-box li {
            padding: 5px 0;
            color: #555;
            font-size: 14px;
        }
        .info-box strong {
            color: #333;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Coding Tickets</h1>
        <p class="subtitle">Connexion à votre compte</p>
        
        <!-- Message d'erreur -->
        <% 
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null && !errorMessage.isEmpty()) {
        %>
            <div class="error">
                <strong>❌ Erreur :</strong> <%= errorMessage %>
            </div>
        <% } %>
        
        <!-- Formulaire de connexion -->
        <form method="POST" action="<%= request.getContextPath() %>/login">
            <div class="form-group">
                <label for="email">Email :</label>
                <input type="email" id="email" name="email" required 
                       placeholder="exemple@email.fr"
                       value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>">
            </div>
            
            <div class="form-group">
                <label for="password">Mot de passe :</label>
                <input type="password" id="password" name="password" required
                       placeholder="Votre mot de passe">
            </div>
            
            <button type="submit">Se connecter</button>
        </form>
        
        <!-- Comptes de test -->
        <div class="info-box">
            <h3>📋 Comptes de test disponibles</h3>
            
            <strong>Clients :</strong>
            <ul>
                <li>📧 jean.dupont@email.fr → password123</li>
                <li>📧 marie.martin@email.fr → password456</li>
                <li>📧 pierre.durand@email.fr → password789</li>
            </ul>
            
            <strong style="margin-top: 10px; display: block;">Organisateurs :</strong>
            <ul>
                <li>📧 sophie.legrand@events.fr → org123</li>
                <li>📧 thomas.bernard@events.fr → org456</li>
            </ul>
        </div>
    </div>
</body>
</html>