package fr.coding.tickets.dao;

import java.util.List;

import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Organisateur;
import fr.coding.tickets.model.Utilisateur;

public interface UtilisateurDao {

    /**
     * Trouve un utilisateur par email et mot de passe (authentification)
     */
    Utilisateur findByEmailAndPassword(String email, String password) throws DaoException;

    /**
     * Trouve un utilisateur par son ID
     */
    Utilisateur findById(long id) throws DaoException;

    /**
     * Liste tous les clients
     */
    List<Client> findAllClients() throws DaoException;

    /**
     * Liste tous les organisateurs
     */
    List<Organisateur> findAllOrganisateurs() throws DaoException;

    /**
     * Crée un nouvel utilisateur
     */
    void create(Utilisateur utilisateur) throws DaoException;

    /**
     * Met à jour un utilisateur
     */
    void update(Utilisateur utilisateur) throws DaoException;
}