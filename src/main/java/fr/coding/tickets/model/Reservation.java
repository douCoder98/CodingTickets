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

    public Reservation() {
    }

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

    private double calculerMontant() {
        return this.nbPlaces * this.evenement.getPrixBase();
    }

    public void confirmer() throws ReservationInvalideException {
        if (this.statut == StatutReservation.ANNULEE) {
            throw new ReservationInvalideException("Impossible de confirmer une réservation annulée");
        }
        this.statut = StatutReservation.CONFIRMEE;
    }

    public void annuler() throws AnnulationTardiveException, ReservationInvalideException {
        if (this.statut == StatutReservation.ANNULEE) {
            throw new ReservationInvalideException("Réservation déjà annulée");
        }

        LocalDateTime limiteAnnulation = evenement.getDate().minusDays(1);
        if (LocalDateTime.now().isAfter(limiteAnnulation)) {
            throw new AnnulationTardiveException(
                "Annulation impossible : l'événement a lieu dans moins de 24h"
            );
        }

        this.statut = StatutReservation.ANNULEE;
        this.evenement.annulerPlaces(this.nbPlaces);
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) {
        this.dateReservation = dateReservation;
    }

    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int nbPlaces) { this.nbPlaces = nbPlaces; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public Evenement getEvenement() { return evenement; }
    public void setEvenement(Evenement evenement) { this.evenement = evenement; }
}
