package fr.coding.tickets.dao;

import java.util.List;

import fr.coding.tickets.model.Evenement;

public interface EvenementDao {

    /**
     * Liste tous les événements
     */
    List<Evenement> findAll() throws DaoException;

    /**
     * Liste les événements disponibles (avec places restantes)
     */
    List<Evenement> findAllDisponibles() throws DaoException;

    /**
     * Trouve un événement par son ID
     */
    Evenement findById(long id) throws DaoException;

    /**
     * Liste les événements d'un organisateur
     */
    List<Evenement> findByOrganisateur(long organisateurId) throws DaoException;

    /**
     * Crée un nouvel événement
     */
    void create(Evenement evenement) throws DaoException;

    /**
     * Met à jour un événement
     */
    void update(Evenement evenement) throws DaoException;

    /**
     * Supprime un événement
     */
    void delete(long id) throws DaoException;
}