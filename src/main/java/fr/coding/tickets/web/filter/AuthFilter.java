package fr.coding.tickets.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("✓ AuthFilter initialisé");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        String path = uri.substring(contextPath.length());

        // URLs publiques (accessibles sans authentification)
        boolean isPublicUrl = path.equals("/login") ||
                             path.equals("/");

        if (isPublicUrl) {
            // Laisser passer
            chain.doFilter(request, response);
            return;
        }

        // Vérifier l'authentification
        HttpSession session = httpRequest.getSession(false);
        boolean isAuthenticated = (session != null && session.getAttribute("user") != null);

        if (isAuthenticated) {
            // Utilisateur authentifié, laisser passer
            chain.doFilter(request, response);
        } else {
            // Non authentifié, rediriger vers login
            System.out.println("⚠ Accès non autorisé à : " + path + " - Redirection vers /login");
            httpResponse.sendRedirect(contextPath + "/login");
        }
    }

    @Override
    public void destroy() {
        System.out.println("AuthFilter détruit");
    }
}
