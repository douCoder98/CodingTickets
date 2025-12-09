package fr.coding.tickets.dao;

import java.util.List;

import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Reservation;

public interface ReservationDao {

    /**
     * Trouve une réservation par son ID
     */
    Reservation findById(long id) throws DaoException;

    /**
     * Liste toutes les réservations d'un client
     */
    List<Reservation> findByClient(Client client) throws DaoException;

    /**
     * Liste toutes les réservations d'un événement
     */
    List<Reservation> findByEvenement(long evenementId) throws DaoException;

    /**
     * Crée une nouvelle réservation
     */
    void create(Reservation reservation) throws DaoException;

    /**
     * Met à jour une réservation
     */
    void update(Reservation reservation) throws DaoException;
}