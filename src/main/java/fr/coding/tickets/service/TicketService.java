package fr.coding.tickets.service;

import fr.coding.tickets.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TicketService {
    
    // Collections
    private List<Utilisateur> utilisateurs;
    private List<Evenement> evenements;
    private List<Reservation> reservations;
    
    // Générateurs d'ID, on les utilise pour simuler des IDs auto-incrémentés
    private AtomicLong utilisateurIdGenerator;
    private AtomicLong evenementIdGenerator;
    private AtomicLong reservationIdGenerator;

    /**
     * Constructeur - Initialisation les collections et les données de test
     */
    public TicketService() {
        this.utilisateurs = new ArrayList<>();
        this.evenements = new ArrayList<>();
        this.reservations = new ArrayList<>();
        
        this.utilisateurIdGenerator = new AtomicLong(1);
        this.evenementIdGenerator = new AtomicLong(1);
        this.reservationIdGenerator = new AtomicLong(1);
        
        // Initialisation des données de test
        initialiserDonnees();
    }

    /**
     * Initialisation des données de test
     */
    private void initialiserDonnees() {
        // Création de clients
        Client client1 = new Client("Jean Dupont", "jean.dupont@email.fr", "password123");
        client1.setId(utilisateurIdGenerator.getAndIncrement());
        
        Client client2 = new Client("Marie Martin", "marie.martin@email.fr", "password456");
        client2.setId(utilisateurIdGenerator.getAndIncrement());
        
        Client client3 = new Client("Pierre Durand", "pierre.durand@email.fr", "password789");
        client3.setId(utilisateurIdGenerator.getAndIncrement());
        
        utilisateurs.add(client1);
        utilisateurs.add(client2);
        utilisateurs.add(client3);
        
        // Création d'organisateurs
        Organisateur org1 = new Organisateur("Sophie Legrand", "sophie.legrand@events.fr", "org123");
        org1.setId(utilisateurIdGenerator.getAndIncrement());
        
        Organisateur org2 = new Organisateur("Thomas Bernard", "thomas.bernard@events.fr", "org456");
        org2.setId(utilisateurIdGenerator.getAndIncrement());
        
        utilisateurs.add(org1);
        utilisateurs.add(org2);
        
        // Création d'événements
        Evenement event1 = new Evenement(
            "Concert Rock - The Beatles Tribute",
            "Une soirée inoubliable avec le meilleur tribute band des Beatles",
            LocalDateTime.now().plusDays(15),
            "Zénith de Paris",
            150,
            45.50
        );
        event1.setId(evenementIdGenerator.getAndIncrement());
        event1.setOrganisateur(org1);
        
        Evenement event2 = new Evenement(
            "Festival Jazz d'été",
            "3 jours de jazz avec les plus grands artistes internationaux",
            LocalDateTime.now().plusDays(30),
            "Parc de la Villette",
            500,
            75.00
        );
        event2.setId(evenementIdGenerator.getAndIncrement());
        event2.setOrganisateur(org1);
        
        Evenement event3 = new Evenement(
            "Théâtre - Le Misanthrope",
            "La célèbre pièce de Molière revisitée",
            LocalDateTime.now().plusDays(7),
            "Comédie Française",
            80,
            35.00
        );
        event3.setId(evenementIdGenerator.getAndIncrement());
        event3.setOrganisateur(org2);
        
        Evenement event4 = new Evenement(
            "Stand-up Comedy Night",
            "Une soirée d'humour avec les meilleurs humoristes français",
            LocalDateTime.now().plusDays(20),
            "Olympia",
            200,
            28.00
        );
        event4.setId(evenementIdGenerator.getAndIncrement());
        event4.setOrganisateur(org2);
        
        // Un événement proche pour tester les réservations imminentes
        Evenement event5 = new Evenement(
			"Exposition d'art contemporain",
			"Découvrez les œuvres des artistes émergents",
			LocalDateTime.now().plusDays(1),
			"Centre Pompidou",
			300,
			12.00
		);
        event5.setId(evenementIdGenerator.getAndIncrement());
        event5.setOrganisateur(org2);
        
        
        evenements.add(event1);
        evenements.add(event2);
        evenements.add(event3);
        evenements.add(event4);
        evenements.add(event5);
        
        org1.creerEvenement(event1);
        org1.creerEvenement(event2);
        org2.creerEvenement(event3);
        org2.creerEvenement(event4);
        org2.creerEvenement(event5);
        
        System.out.println("=== Données initialisées ===");
        System.out.println("Clients créés : " + utilisateurs.stream()
            .filter(u -> u.getRole() == Role.CLIENT).count());
        System.out.println("Organisateurs créés : " + utilisateurs.stream()
            .filter(u -> u.getRole() == Role.ORGANISATEUR).count());
        System.out.println("Événements créés : " + evenements.size());
    }

    /**
     * Authentification d'un utilisateur avec son email et mot de passe
     * @return l'utilisateur authentifié ou null si échec
     */
    public Utilisateur authentifier(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            return null;
        }
        
        return utilisateurs.stream()
            .filter(u -> u.getEmail().equals(email) && 
                        motDePasse.equals(motDePasse)) // En production, utiliser hash
            .findFirst()
            .orElse(null);
    }

    /**
     * Liste tous les événements disponibles
     */
    public List<Evenement> listerEvenements() {
        return new ArrayList<>(evenements);
    }

    /**
     * Liste des événements à venir avec des places disponibles
     */
    public List<Evenement> listerEvenementsDisponibles() {
        LocalDateTime maintenant = LocalDateTime.now();
        return evenements.stream()
            .filter(e -> e.getDate().isAfter(maintenant))
            .filter(e -> e.getNbPlacesRestantes() > 0)
            .collect(Collectors.toList());
    }

    /**
     * Trouve un événement par son ID
     */
    public Evenement trouverEvenementParId(long id) {
        return evenements.stream()
            .filter(e -> e.getId() != null && e.getId() == id)
            .findFirst()
            .orElse(null);
    }

    /**
     * Crée une réservation pour un client
     * RÈGLE MÉTIER : Vérifie les places disponibles et crée la réservation
     */
    public Reservation reserver(Client client, long idEvenement, int nbPlaces) 
            throws PlacesInsuffisantesException, ReservationInvalideException {
        
        if (client == null) {
            throw new ReservationInvalideException("Client non spécifié");
        }
        
        // Vérifie que le client existe dans le système
        if (!utilisateurs.contains(client)) {
            throw new ReservationInvalideException("Client non enregistré dans le système");
        }
        
        // Trouve l'événement
        Evenement evenement = trouverEvenementParId(idEvenement);
        if (evenement == null) {
            throw new ReservationInvalideException("Événement introuvable");
        }
        
        // Vérifie que l'événement est à venir
        if (evenement.getDate().isBefore(LocalDateTime.now())) {
            throw new ReservationInvalideException("Impossible de réserver pour un événement passé");
        }
        
        // Crée la réservation (vérifie automatiquement les places)
        Reservation reservation = new Reservation(client, evenement, nbPlaces);
        reservation.setId(reservationIdGenerator.getAndIncrement());
        
        // Ajoute à la liste des réservations
        reservations.add(reservation);
        
        System.out.println("✓ Réservation créée - ID: " + reservation.getId() + 
                          ", Client: " + client.getNom() + 
                          ", Événement: " + evenement.getTitre() + 
                          ", Places: " + nbPlaces);
        
        return reservation;
    }

    /**
     * Liste toutes les réservations d'un client
     */
    public List<Reservation> listerReservationsClient(Client client) {
        if (client == null) {
            return new ArrayList<>();
        }
        
        return reservations.stream()
            .filter(r -> r.getClient().getId().equals(client.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Liste les réservations actives d'un client (non annulées)
     */
    public List<Reservation> listerReservationsActivesClient(Client client) {
        if (client == null) {
            return new ArrayList<>();
        }
        
        return reservations.stream()
            .filter(r -> r.getClient().getId().equals(client.getId()))
            .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
            .collect(Collectors.toList());
    }

    /**
     * Annulation d'une réservation
     * RÈGLE MÉTIER : Vérifie que la réservation appartient au client et respecte les délais
     */
    public void annulerReservation(long idReservation, Client client) 
            throws ReservationInvalideException, AnnulationTardiveException {
        
        if (client == null) {
            throw new ReservationInvalideException("Client non spécifié");
        }
        
        // Trouve la réservation
        Reservation reservation = reservations.stream()
            .filter(r -> r.getId() != null && r.getId() == idReservation)
            .findFirst()
            .orElseThrow(() -> new ReservationInvalideException("Réservation introuvable"));
        
        // Vérifie que la réservation appartient bien au client
        if (!reservation.getClient().getId().equals(client.getId())) {
            throw new ReservationInvalideException("Cette réservation n'appartient pas au client");
        }
        
        // Annule la réservation (vérifie automatiquement les délais)
        reservation.annuler();
        
        System.out.println("✓ Réservation annulée - ID: " + idReservation + 
                          ", Client: " + client.getNom());
    }

    /**
     * Trouver une réservation par son ID
     */
    public Reservation trouverReservationParId(long id) {
        return reservations.stream()
            .filter(r -> r.getId() != null && r.getId() == id)
            .findFirst()
            .orElse(null);
    }

    // ========== MÉTHODES POUR ORGANISATEURS ==========

    /**
     * Créer un nouvel événement (réservé aux organisateurs)
     */
    public Evenement creerEvenement(Organisateur organisateur, String titre, 
                                   String description, LocalDateTime date, 
                                   String lieu, int nbPlaces, double prixBase) 
            throws ReservationInvalideException {
        
        if (organisateur == null) {
            throw new ReservationInvalideException("Organisateur non spécifié");
        }
        
        if (!utilisateurs.contains(organisateur)) {
            throw new ReservationInvalideException("Organisateur non enregistré");
        }
        
        if (date.isBefore(LocalDateTime.now())) {
            throw new ReservationInvalideException("La date de l'événement doit être future");
        }
        
        Evenement evenement = new Evenement(titre, description, date, lieu, nbPlaces, prixBase);
        evenement.setId(evenementIdGenerator.getAndIncrement());
        evenement.setOrganisateur(organisateur);
        
        evenements.add(evenement);
        organisateur.creerEvenement(evenement);
        
        System.out.println("✓ Événement créé - ID: " + evenement.getId() + 
                          ", Titre: " + titre + 
                          ", Organisateur: " + organisateur.getNom());
        
        return evenement;
    }

    /**
     * Liste des événements d'un organisateur
     */
    public List<Evenement> listerEvenementsOrganisateur(Organisateur organisateur) {
        if (organisateur == null) {
            return new ArrayList<>();
        }
        
        return evenements.stream()
            .filter(e -> e.getOrganisateur() != null && 
                        e.getOrganisateur().getId().equals(organisateur.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Supprimer un événement (si aucune réservation active)
     */
    public void supprimerEvenement(long idEvenement, Organisateur organisateur) 
            throws ReservationInvalideException {
        
        Evenement evenement = trouverEvenementParId(idEvenement);
        if (evenement == null) {
            throw new ReservationInvalideException("Événement introuvable");
        }
        
        if (!evenement.getOrganisateur().getId().equals(organisateur.getId())) {
            throw new ReservationInvalideException("Cet événement n'appartient pas à l'organisateur");
        }
        
        // Vérifier qu'il n'y a pas de réservations actives
        long nbReservationsActives = reservations.stream()
            .filter(r -> r.getEvenement().getId().equals(idEvenement))
            .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
            .count();
        
        if (nbReservationsActives > 0) {
            throw new ReservationInvalideException(
                "Impossible de supprimer : " + nbReservationsActives + " réservation(s) active(s)");
        }
        
        evenements.remove(evenement);
        organisateur.supprimerEvenement(idEvenement);
        
        System.out.println("✓ Événement supprimé - ID: " + idEvenement);
    }


    /**
     * Inscrire un nouvel utilisateur
     */
    public Utilisateur inscrireUtilisateur(String nom, String email, 
                                          String motDePasse, Role role) 
            throws ReservationInvalideException {
        
        // Vérifier que l'email n'existe pas déjà
        boolean emailExiste = utilisateurs.stream()
            .anyMatch(u -> u.getEmail().equals(email));
        
        if (emailExiste) {
            throw new ReservationInvalideException("Cet email est déjà utilisé");
        }
        
        Utilisateur utilisateur;
        if (role == Role.CLIENT) {
            utilisateur = new Client(nom, email, motDePasse);
        } else {
            utilisateur = new Organisateur(nom, email, motDePasse);
        }
        
        utilisateur.setId(utilisateurIdGenerator.getAndIncrement());
        utilisateurs.add(utilisateur);
        
        System.out.println("✓ Utilisateur inscrit - " + role + ": " + nom);
        
        return utilisateur;
    }

    // Getters pour accès direct
    public List<Utilisateur> getUtilisateurs() {
        return new ArrayList<>(utilisateurs);
    }

    public List<Evenement> getEvenements() {
        return new ArrayList<>(evenements);
    }

    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }
}

