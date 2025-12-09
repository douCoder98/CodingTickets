package fr.coding.tickets.model;

import java.time.LocalDateTime;

public class Evenement {
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime date;
    private String lieu;
    private int nbPlacesTotales;
    private int nbPlacesRestantes;
    private double prixBase;
    private Organisateur organisateur;

    public Evenement(String titre, String description, LocalDateTime date,
                     String lieu, int nbPlaces, double prixBase) {
        this.titre = titre;
        this.description = description;
        this.date = date;
        this.lieu = lieu;
        this.nbPlacesTotales = nbPlaces;
        this.nbPlacesRestantes = nbPlaces;
        this.prixBase = prixBase;
    }

    /**
     * RÈGLE MÉTIER : Réserver des places
     * Vérifie qu'il reste suffisamment de places
     * Diminue le nombre de places restantes
     */
    public synchronized void reserverPlaces(int nbPlaces) throws PlacesInsuffisantesException {
        if (nbPlaces <= 0) {
            throw new PlacesInsuffisantesException("Le nombre de places doit être positif");
        }

        if (nbPlaces > nbPlacesRestantes) {
            throw new PlacesInsuffisantesException(
                String.format("Places insuffisantes. Demandé: %d, Disponible: %d",
                             nbPlaces, nbPlacesRestantes)
            );
        }

        this.nbPlacesRestantes -= nbPlaces;
    }

    /**
     * RÈGLE MÉTIER : Annuler des places
     * Augmente le nombre de places restantes
     * Ne peut pas dépasser le total de places
     */
    public synchronized void annulerPlaces(int nbPlaces) {
        if (nbPlaces <= 0) {
            return;
        }

        this.nbPlacesRestantes = Math.min(
            this.nbPlacesRestantes + nbPlaces,
            this.nbPlacesTotales
        );
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getLieu() {
        return lieu;
    }

    public int getNbPlacesTotales() {
        return nbPlacesTotales;
    }

    public int getNbPlacesRestantes() {
        return nbPlacesRestantes;
    }

    public double getPrixBase() {
        return prixBase;
    }

    public Organisateur getOrganisateur() {
        return organisateur;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public void setPrixBase(double prixBase) {
        this.prixBase = prixBase;
    }

    public void setOrganisateur(Organisateur organisateur) {
        this.organisateur = organisateur;
    }
}
