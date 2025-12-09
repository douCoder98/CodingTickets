package fr.coding.tickets.listener;

import fr.coding.tickets.service.TicketService;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;


@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        System.out.println("========================================");
        System.out.println("   INITIALISATION DE L'APPLICATION");
        System.out.println("========================================");

        // Création et initialisation du service
        TicketService ticketService = new TicketService();

        // Stockage dans le contexte
        context.setAttribute("ticketService", ticketService);

        System.out.println("✓ TicketService créé et stocké dans le contexte");
        System.out.println("========================================\n");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application arrêtée");
    }
}
