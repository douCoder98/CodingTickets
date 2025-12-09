// ==================== JdbcEvenementDao.java ====================
package fr.coding.tickets.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import fr.coding.tickets.dao.DaoException;
import fr.coding.tickets.dao.EvenementDao;
import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.Organisateur;

public class JdbcEvenementDao implements EvenementDao {

    private JdbcUtilisateurDao utilisateurDao = new JdbcUtilisateurDao();

    @Override
    public List<Evenement> findAll() throws DaoException {
        String sql = "SELECT * FROM EVENEMENT ORDER BY date_evenement";
        List<Evenement> evenements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                evenements.add(mapResultSetToEvenement(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des événements", e);
        }

        return evenements;
    }

    @Override
    public List<Evenement> findAllDisponibles() throws DaoException {
        String sql = "SELECT * FROM EVENEMENT WHERE nb_places_restantes > 0 AND date_evenement > NOW() ORDER BY date_evenement";
        List<Evenement> evenements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                evenements.add(mapResultSetToEvenement(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des événements disponibles", e);
        }

        return evenements;
    }

    @Override
    public Evenement findById(long id) throws DaoException {
        String sql = "SELECT * FROM EVENEMENT WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEvenement(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de l'événement", e);
        }
    }

    @Override
    public List<Evenement> findByOrganisateur(long organisateurId) throws DaoException {
        String sql = "SELECT * FROM EVENEMENT WHERE organisateur_id = ? ORDER BY date_evenement";
        List<Evenement> evenements = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, organisateurId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    evenements.add(mapResultSetToEvenement(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des événements de l'organisateur", e);
        }

        return evenements;
    }

    @Override
    public void create(Evenement evenement) throws DaoException {
        String sql = "INSERT INTO EVENEMENT (titre, description, lieu, date_evenement, nb_places_totales, nb_places_restantes, prix_base, organisateur_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evenement.getTitre());
            stmt.setString(2, evenement.getDescription());
            stmt.setString(3, evenement.getLieu());
            stmt.setTimestamp(4, Timestamp.valueOf(evenement.getDate()));
            stmt.setInt(5, evenement.getNbPlacesTotales());
            stmt.setInt(6, evenement.getNbPlacesRestantes());
            stmt.setDouble(7, evenement.getPrixBase());
            stmt.setLong(8, evenement.getOrganisateur().getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    evenement.setId(rs.getLong("id"));
                } else {
                    throw new DaoException("Échec de la création de l'événement");
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création de l'événement", e);
        }
    }

    @Override
    public void update(Evenement evenement) throws DaoException {
        String sql = "UPDATE EVENEMENT SET titre = ?, description = ?, lieu = ?, date_evenement = ?, nb_places_totales = ?, nb_places_restantes = ?, prix_base = ?, organisateur_id = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evenement.getTitre());
            stmt.setString(2, evenement.getDescription());
            stmt.setString(3, evenement.getLieu());
            stmt.setTimestamp(4, Timestamp.valueOf(evenement.getDate()));
            stmt.setInt(5, evenement.getNbPlacesTotales());
            stmt.setInt(6, evenement.getNbPlacesRestantes());
            stmt.setDouble(7, evenement.getPrixBase());
            stmt.setLong(8, evenement.getOrganisateur().getId());
            stmt.setLong(9, evenement.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la mise à jour de l'événement", e);
        }
    }

    @Override
    public void delete(long id) throws DaoException {
        String sql = "DELETE FROM EVENEMENT WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression de l'événement", e);
        }
    }

    /**
     * Mappe un ResultSet vers un objet Evenement
     */
    private Evenement mapResultSetToEvenement(ResultSet rs) throws SQLException, DaoException {
        long id = rs.getLong("id");
        String titre = rs.getString("titre");
        String description = rs.getString("description");
        String lieu = rs.getString("lieu");
        LocalDateTime dateEvenement = rs.getTimestamp("date_evenement").toLocalDateTime();
        int nbPlacesTotales = rs.getInt("nb_places_totales");
        int nbPlacesRestantes = rs.getInt("nb_places_restantes");
        double prixBase = rs.getDouble("prix_base");
        long organisateurId = rs.getLong("organisateur_id");

        // Récupérer l'organisateur
        Organisateur organisateur = (Organisateur) utilisateurDao.findById(organisateurId);

        Evenement evenement = new Evenement(titre, description, dateEvenement, lieu, nbPlacesTotales, prixBase);
        evenement.setId(id);
        evenement.setOrganisateur(organisateur);

        // Ajuster les places restantes (car le constructeur initialise à nbPlacesTotales)
        int placesReservees = nbPlacesTotales - nbPlacesRestantes;
        if (placesReservees > 0) {
            try {
                evenement.reserverPlaces(placesReservees);
            } catch (Exception e) {
                // Ignorer, juste pour ajuster le compteur
            }
        }

        return evenement;
    }
}