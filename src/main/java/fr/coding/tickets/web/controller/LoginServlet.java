package fr.coding.tickets.web.controller;

import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.service.TicketService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String errorMessage = (String) request.getSession().getAttribute("errorMessage");
        if (errorMessage != null) {
            request.getSession().removeAttribute("errorMessage");
        }
        
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <title>Connexion - Système de Billetterie</title>");
        out.println("    <style>");
        out.println("        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }");
        out.println("        h1 { color: #333; text-align: center; }");
        out.println("        .form-group { margin-bottom: 15px; }");
        out.println("        label { display: block; margin-bottom: 5px; font-weight: bold; }");
        out.println("        input[type='text'], input[type='password'] { width: 100%; padding: 8px; box-sizing: border-box; }");
        out.println("        button { width: 100%; padding: 10px; background-color: #007bff; color: white; border: none; cursor: pointer; font-size: 16px; }");
        out.println("        button:hover { background-color: #0056b3; }");
        out.println("        .error { color: red; background-color: #ffebee; padding: 10px; border-radius: 4px; margin-bottom: 15px; }");
        out.println("        .info { color: #333; background-color: #e3f2fd; padding: 15px; border-radius: 4px; margin-top: 20px; }");
        out.println("        .info h3 { margin-top: 0; }");
        out.println("        .info ul { margin: 10px 0; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <h1>Connexion</h1>");
        
        if (errorMessage != null) {
            out.println("    <div class='error'>" + errorMessage + "</div>");
        }
        
        out.println("    <form method='POST' action='login'>");
        out.println("        <div class='form-group'>");
        out.println("            <label for='email'>Email :</label>");
        out.println("            <input type='text' id='email' name='email' required>");
        out.println("        </div>");
        out.println("        <div class='form-group'>");
        out.println("            <label for='password'>Mot de passe :</label>");
        out.println("            <input type='password' id='password' name='password' required>");
        out.println("        </div>");
        out.println("        <button type='submit'>Se connecter</button>");
        out.println("    </form>");
        
        out.println("    <div class='info'>");
        out.println("        <h3>Comptes de test disponibles :</h3>");
        out.println("        <strong>Clients :</strong>");
        out.println("        <ul>");
        out.println("            <li>jean.dupont@email.fr / password123</li>");
        out.println("            <li>marie.martin@email.fr / password456</li>");
        out.println("            <li>pierre.durand@email.fr / password789</li>");
        out.println("        </ul>");
        out.println("        <strong>Organisateurs :</strong>");
        out.println("        <ul>");
        out.println("            <li>sophie.legrand@events.fr / org123</li>");
        out.println("            <li>thomas.bernard@events.fr / org456</li>");
        out.println("        </ul>");
        out.println("    </div>");
        
        out.println("</body>");
        out.println("</html>");
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
            response.sendRedirect(request.getContextPath() + "/reservations/history");
    }
       else {
            // Authentification échouée
            System.out.println("✗ Échec d'authentification pour : " + email);
            
            HttpSession session = request.getSession(true);
            session.setAttribute("errorMessage", "Email ou mot de passe incorrect");
            
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}
