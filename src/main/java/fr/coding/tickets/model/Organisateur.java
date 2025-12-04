package fr.coding.tickets.model;

import java.util.ArrayList;
import java.util.List;

public class Organisateur extends Utilisateur {
    private List<Evenement> evenementsOrganises;

    public Organisateur(String nom, String email, String motDePasse) {
        super(nom, email, motDePasse, Role.ORGANISATEUR);
        this.evenementsOrganises = new ArrayList<>();
    }

    public void creerEvenement(Evenement evenement) {
        evenement.setOrganisateur(this);
        this.evenementsOrganises.add(evenement);
    }

    public List<Evenement> getEvenementsOrganises() {
        return new ArrayList<>(evenementsOrganises);
    }

    public void supprimerEvenement(Long evenementId) throws ReservationInvalideException {
        Evenement evenement = evenementsOrganises.stream()
            .filter(e -> e.getId().equals(evenementId))
            .findFirst()
            .orElseThrow(() -> new ReservationInvalideException("Événement introuvable"));
        
        evenementsOrganises.remove(evenement);
    }
}
