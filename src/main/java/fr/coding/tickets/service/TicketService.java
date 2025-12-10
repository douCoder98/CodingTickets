package fr.coding.tickets.service;

import java.time.LocalDateTime;
import java.util.List;

import fr.coding.tickets.dao.DaoException;
import fr.coding.tickets.dao.EvenementDao;
import fr.coding.tickets.dao.ReservationDao;
import fr.coding.tickets.dao.UtilisateurDao;
import fr.coding.tickets.dao.jdbc.JdbcEvenementDao;
import fr.coding.tickets.dao.jdbc.JdbcReservationDao;
import fr.coding.tickets.dao.jdbc.JdbcUtilisateurDao;
import fr.coding.tickets.model.AnnulationTardiveException;
import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.Organisateur;
import fr.coding.tickets.model.PlacesInsuffisantesException;
import fr.coding.tickets.model.Reservation;
import fr.coding.tickets.model.ReservationInvalideException;
import fr.coding.tickets.model.Role;
import fr.coding.tickets.model.StatutReservation;
import fr.coding.tickets.model.Utilisateur;

public class TicketService {

    // DAOs
    private UtilisateurDao utilisateurDao;
    private EvenementDao evenementDao;
    private ReservationDao reservationDao;

    public TicketService() {
        this.utilisateurDao = new JdbcUtilisateurDao();
        this.evenementDao = new JdbcEvenementDao();
        this.reservationDao = new JdbcReservationDao();

        System.out.println("========================================");
        System.out.println("   INITIALISATION DU SERVICE (DAO)");
        System.out.println("========================================");
        System.out.println("✓ TicketService créé avec accès base de données");
        System.out.println("========================================\n");
    }

    public Utilisateur authentifier(String email, String motDePasse) {
        if (email == null || motDePasse == null) {
            return null;
        }

        try {
            return utilisateurDao.findByEmailAndPassword(email, motDePasse);
        } catch (DaoException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
            return null;
        }
    }

    public List<Evenement> listerEvenements() {
        try {
            return evenementDao.findAll();
        } catch (DaoException e) {
            System.err.println("Erreur lors de la récupération des événements : " + e.getMessage());
            return List.of();
        }
    }

    public List<Evenement> listerEvenementsDisponibles() {
        try {
            return evenementDao.findAllDisponibles();
        } catch (DaoException e) {
            System.err.println("Erreur lors de la récupération des événements disponibles : " + e.getMessage());
            return List.of();
        }
    }

    public Evenement trouverEvenementParId(long id) {
        try {
            return evenementDao.findById(id);
        } catch (DaoException e) {
            System.err.println("Erreur lors de la recherche de l'événement : " + e.getMessage());
            return null;
        }
    }

    public Reservation reserver(Client client, long idEvenement, int nbPlaces)
            throws PlacesInsuffisantesException, ReservationInvalideException {

        if (client == null) {
            throw new ReservationInvalideException("Client non spécifié");
        }

        try {
            // Recharger l'événement depuis la base pour avoir les données à jour
            Evenement evenement = evenementDao.findById(idEvenement);

            if (evenement == null) {
                throw new ReservationInvalideException("Événement introuvable");
            }

            // Vérifie que l'événement est à venir
            if (evenement.getDate().isBefore(LocalDateTime.now())) {
                throw new ReservationInvalideException("Impossible de réserver pour un événement passé");
            }

            // Vérifie les places disponibles
            if (nbPlaces > evenement.getNbPlacesRestantes()) {
                throw new PlacesInsuffisantesException(
                    String.format("Places insuffisantes. Demandé: %d, Disponible: %d",
                                 nbPlaces, evenement.getNbPlacesRestantes())
                );
            }

            // Crée la réservation (avec constructeur vide pour éviter double réservation)
            Reservation reservation = new Reservation();
            reservation.setClient(client);
            reservation.setEvenement(evenement);
            reservation.setNbPlaces(nbPlaces);
            reservation.setMontantTotal(nbPlaces * evenement.getPrixBase());
            reservation.setStatut(StatutReservation.EN_ATTENTE);
            reservation.setDateReservation(LocalDateTime.now());

            // Sauvegarder la réservation en base
            reservationDao.create(reservation);

            //Confirmer la réservation
            reservation.confirmer();
            reservationDao.update(reservation);

            // Mettre à jour les places restantes de l'événement
            evenement.reserverPlaces(nbPlaces);
            evenementDao.update(evenement);

            return reservation;

        } catch (DaoException e) {
            throw new ReservationInvalideException("Erreur lors de la création de la réservation: " + e.getMessage());
        }
    }

    public List<Reservation> listerReservationsClient(Client client) {
        if (client == null) {
            return List.of();
        }

        try {
            return reservationDao.findByClient(client);
        } catch (DaoException e) {
            System.err.println("Erreur lors de la récupération des réservations : " + e.getMessage());
            return List.of();
        }
    }

    public void annulerReservation(long idReservation, Client client)
            throws ReservationInvalideException, AnnulationTardiveException {

        if (client == null) {
            throw new ReservationInvalideException("Client non spécifié");
        }

        try {
            // Trouve la réservation
            Reservation reservation = reservationDao.findById(idReservation);

            if (reservation == null) {
                throw new ReservationInvalideException("Réservation introuvable");
            }

            // Vérifie que la réservation appartient bien au client
            if (!reservation.getClient().getId().equals(client.getId())) {
                throw new ReservationInvalideException("Cette réservation n'appartient pas au client");
            }

            // Annule la réservation (vérifie automatiquement les délais)
            reservation.annuler();

            // Mettre à jour la réservation en base
            reservationDao.update(reservation);

            // Mettre à jour l'événement (remet les places à disposition)
            evenementDao.update(reservation.getEvenement());

            System.out.println("✓ Réservation annulée - ID: " + idReservation +
                              ", Client: " + client.getNom());

        } catch (DaoException e) {
            throw new ReservationInvalideException("Erreur lors de l'annulation: " + e.getMessage());
        }
    }

    public Reservation trouverReservationParId(long id) {
        try {
            return reservationDao.findById(id);
        } catch (DaoException e) {
            System.err.println("Erreur lors de la recherche de la réservation : " + e.getMessage());
            return null;
        }
    }

    // ========== MÉTHODES POUR ORGANISATEURS ==========
    public Evenement creerEvenement(Organisateur organisateur, String titre,
                                   String description, LocalDateTime date,
                                   String lieu, int nbPlaces, double prixBase)
            throws ReservationInvalideException {

        if (organisateur == null) {
            throw new ReservationInvalideException("Organisateur non spécifié");
        }

        if (date.isBefore(LocalDateTime.now())) {
            throw new ReservationInvalideException("La date de l'événement doit être future");
        }

        try {
            Evenement evenement = new Evenement(titre, description, date, lieu, nbPlaces, prixBase);
            evenement.setOrganisateur(organisateur);

            evenementDao.create(evenement);

            System.out.println("✓ Événement créé - ID: " + evenement.getId() +
                              ", Titre: " + titre +
                              ", Organisateur: " + organisateur.getNom());

            return evenement;

        } catch (DaoException e) {
            throw new ReservationInvalideException("Erreur lors de la création de l'événement: " + e.getMessage());
        }
    }

    public List<Evenement> listerEvenementsOrganisateur(Organisateur organisateur) {
        if (organisateur == null) {
            return List.of();
        }

        try {
            return evenementDao.findByOrganisateur(organisateur.getId());
        } catch (DaoException e) {
            System.err.println("Erreur lors de la récupération des événements de l'organisateur : " + e.getMessage());
            return List.of();
        }
    }

    public void supprimerEvenement(long idEvenement, Organisateur organisateur)
            throws ReservationInvalideException {

        try {
            Evenement evenement = evenementDao.findById(idEvenement);

            if (evenement == null) {
                throw new ReservationInvalideException("Événement introuvable");
            }

            if (!evenement.getOrganisateur().getId().equals(organisateur.getId())) {
                throw new ReservationInvalideException("Cet événement n'appartient pas à l'organisateur");
            }

            // Vérifie qu'il n'y a pas de réservations actives
            List<Reservation> reservations = reservationDao.findByEvenement(idEvenement);
            long nbReservationsActives = reservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .count();

            if (nbReservationsActives > 0) {
                throw new ReservationInvalideException(
                    "Impossible de supprimer : " + nbReservationsActives + " réservation(s) active(s)");
            }

            evenementDao.delete(idEvenement);

            System.out.println("✓ Événement supprimé - ID: " + idEvenement);

        } catch (DaoException e) {
            throw new ReservationInvalideException("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    public Utilisateur inscrireUtilisateur(String nom, String email,
                                          String motDePasse, Role role)
            throws ReservationInvalideException {

        try {
            // Vérifie que l'email n'existe pas déjà (via tentative d'authentification)
            Utilisateur existant = utilisateurDao.findByEmailAndPassword(email, motDePasse);
            if (existant != null) {
                throw new ReservationInvalideException("Cet email est déjà utilisé");
            }

            Utilisateur utilisateur;
            if (role == Role.CLIENT) {
                utilisateur = new Client(nom, email, motDePasse);
            } else {
                utilisateur = new Organisateur(nom, email, motDePasse);
            }

            utilisateurDao.create(utilisateur);

            System.out.println("✓ Utilisateur inscrit - " + role + ": " + nom);

            return utilisateur;

        } catch (DaoException e) {
            throw new ReservationInvalideException("Erreur lors de l'inscription: " + e.getMessage());
        }
    }
}