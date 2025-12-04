package fr.coding.tickets.model;

import java.util.ArrayList;
import java.util.List;

public class Client extends Utilisateur {
    private List<Reservation> reservations;

    public Client(String nom, String email, String motDePasse) {
        super(nom, email, motDePasse, Role.CLIENT);
        this.reservations = new ArrayList<>();
    }

    public void ajouterReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }

    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }

    public void annulerReservation(Long reservationId) 
            throws ReservationInvalideException, AnnulationTardiveException {
        Reservation reservation = reservations.stream()
            .filter(r -> r.getId().equals(reservationId))
            .findFirst()
            .orElseThrow(() -> new ReservationInvalideException("Réservation introuvable"));
        
        reservation.annuler();
    }
}
