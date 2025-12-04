package fr.coding.tickets.model;

import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private LocalDateTime dateReservation;
    private int nbPlaces;
    private double montantTotal;
    private StatutReservation statut;
    private Client client;
    private Evenement evenement;

    /**
     * RÈGLE MÉTIER : Création d'une réservation
     * Réserve automatiquement les places dans l'événement
     * Calcule le montant total = nbPlaces * prixBase
     * Statut initial : EN_ATTENTE
     */
    public Reservation(Client client, Evenement evenement, int nbPlaces) 
            throws PlacesInsuffisantesException {
        
        if (client == null || evenement == null) {
            throw new IllegalArgumentException("Client et événement requis");
        }
        
        if (nbPlaces <= 0) {
            throw new IllegalArgumentException("Le nombre de places doit être positif");
        }

        // Tente de réserver les places
        evenement.reserverPlaces(nbPlaces);
        
        this.client = client;
        this.evenement = evenement;
        this.nbPlaces = nbPlaces;
        this.dateReservation = LocalDateTime.now();
        this.montantTotal = calculerMontant();
        this.statut = StatutReservation.EN_ATTENTE;
        
        client.ajouterReservation(this);
    }

    /**
     * RÈGLE MÉTIER : Calcul du montant
     * montantTotal = nbPlaces * prixBase
     */
    private double calculerMontant() {
        return this.nbPlaces * this.evenement.getPrixBase();
    }

    /**
     * RÈGLE MÉTIER : Confirmer la réservation
     */
    public void confirmer() throws ReservationInvalideException {
        if (this.statut == StatutReservation.ANNULEE) {
            throw new ReservationInvalideException("Impossible de confirmer une réservation annulée");
        }
        this.statut = StatutReservation.CONFIRMEE;
    }

    /**
     * RÈGLE MÉTIER : Annulation d'une réservation
     * - Vérifie que l'événement n'est pas trop proche (J-1 minimum)
     * - Change le statut à ANNULEE
     * - Remet les places à disposition dans l'événement
     */
    public void annuler() throws AnnulationTardiveException, ReservationInvalideException {
        if (this.statut == StatutReservation.ANNULEE) {
            throw new ReservationInvalideException("Réservation déjà annulée");
        }

        // Règle : annulation autorisée jusqu'à J-1
        LocalDateTime limiteAnnulation = evenement.getDate().minusDays(1);
        if (LocalDateTime.now().isAfter(limiteAnnulation)) {
            throw new AnnulationTardiveException(
                "Annulation impossible : l'événement a lieu dans moins de 24h"
            );
        }

        // Annulation validée
        this.statut = StatutReservation.ANNULEE;
        this.evenement.annulerPlaces(this.nbPlaces);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public LocalDateTime getDateReservation() {
        return dateReservation;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public StatutReservation getStatut() {
        return statut;
    }

    public Client getClient() {
        return client;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setStatut(StatutReservation statut) {
        this.statut = statut;
    }
}
