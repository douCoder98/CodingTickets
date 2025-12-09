// ==================== JdbcReservationDao.java ====================
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
import fr.coding.tickets.dao.ReservationDao;
import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.Reservation;
import fr.coding.tickets.model.StatutReservation;

public class JdbcReservationDao implements ReservationDao {

    private JdbcUtilisateurDao utilisateurDao = new JdbcUtilisateurDao();
    private JdbcEvenementDao evenementDao = new JdbcEvenementDao();

    @Override
    public Reservation findById(long id) throws DaoException {
        String sql = "SELECT * FROM RESERVATION WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de la réservation", e);
        }
    }

    @Override
    public List<Reservation> findByClient(Client client) throws DaoException {
        String sql = "SELECT * FROM RESERVATION WHERE client_id = ? ORDER BY date_reservation DESC";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, client.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des réservations du client", e);
        }

        return reservations;
    }

    @Override
    public List<Reservation> findByEvenement(long evenementId) throws DaoException {
        String sql = "SELECT * FROM RESERVATION WHERE evenement_id = ?";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, evenementId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des réservations de l'événement", e);
        }

        return reservations;
    }

    @Override
    public void create(Reservation reservation) throws DaoException {
        // Approche alternative : sans RETURNING, récupérer l'ID après
        String sqlInsert = "INSERT INTO RESERVATION (date_reservation, nb_places, montant_total, statut, client_id, evenement_id) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlGetId = "SELECT currval('reservation_id_seq')";

        Connection conn = null;
        PreparedStatement stmtInsert = null;
        PreparedStatement stmtGetId = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Insertion
            stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setTimestamp(1, Timestamp.valueOf(reservation.getDateReservation()));
            stmtInsert.setInt(2, reservation.getNbPlaces());
            stmtInsert.setDouble(3, reservation.getMontantTotal());
            stmtInsert.setObject(4, reservation.getStatut().name(), java.sql.Types.OTHER);
            stmtInsert.setLong(5, reservation.getClient().getId());
            stmtInsert.setLong(6, reservation.getEvenement().getId());

            int rows = stmtInsert.executeUpdate();
            System.out.println("DEBUG - Lignes insérées : " + rows);

            if (rows == 0) {
                throw new DaoException("Échec de l'insertion - aucune ligne affectée");
            }

            // Récupérer l'ID généré
            stmtGetId = conn.prepareStatement(sqlGetId);
            rs = stmtGetId.executeQuery();

            if (rs.next()) {
                long id = rs.getLong(1);
                reservation.setId(id);
                System.out.println("✓ Réservation créée avec ID : " + id);
            } else {
                throw new DaoException("Impossible de récupérer l'ID généré");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL : " + e.getMessage());
            System.err.println("SQLState : " + e.getSQLState());
            e.printStackTrace();
            throw new DaoException("Erreur lors de la création de la réservation", e);
        } finally {
            try {
                if (rs != null) {
					rs.close();
				}
                if (stmtGetId != null) {
					stmtGetId.close();
				}
                if (stmtInsert != null) {
					stmtInsert.close();
				}
                if (conn != null) {
					conn.close();
				}
            } catch (SQLException e) {
                System.err.println("Erreur fermeture : " + e.getMessage());
            }
        }
    }

    @Override
    public void update(Reservation reservation) throws DaoException {
        String sql = "UPDATE RESERVATION SET nb_places = ?, montant_total = ?, statut = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, reservation.getNbPlaces());
            stmt.setDouble(2, reservation.getMontantTotal());
            stmt.setObject(3, reservation.getStatut().name(), java.sql.Types.OTHER);
            stmt.setLong(4, reservation.getId());

            int rows = stmt.executeUpdate();
            System.out.println("DEBUG - Réservation mise à jour, lignes affectées : " + rows);

            if (rows == 0) {
                System.err.println("⚠️ Aucune réservation mise à jour pour ID : " + reservation.getId());
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL update réservation : " + e.getMessage());
            e.printStackTrace();
            throw new DaoException("Erreur lors de la mise à jour de la réservation", e);
        } finally {
            try {
                if (stmt != null) {
					stmt.close();
				}
                if (conn != null) {
					conn.close();
				}
            } catch (SQLException e) {
                System.err.println("Erreur fermeture : " + e.getMessage());
            }
        }
    }

    /**
     * Mappe un ResultSet vers un objet Reservation
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException, DaoException {
        long id = rs.getLong("id");
        LocalDateTime dateReservation = rs.getTimestamp("date_reservation").toLocalDateTime();
        int nbPlaces = rs.getInt("nb_places");
        double montantTotal = rs.getDouble("montant_total");
        String statutStr = rs.getString("statut");
        StatutReservation statut = StatutReservation.valueOf(statutStr);
        long clientId = rs.getLong("client_id");
        long evenementId = rs.getLong("evenement_id");

        // Récupérer le client et l'événement
        Client client = (Client) utilisateurDao.findById(clientId);
        Evenement evenement = evenementDao.findById(evenementId);

        // Créer la réservation sans passer par le constructeur qui réserve les places
        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setDateReservation(dateReservation);
        reservation.setNbPlaces(nbPlaces);
        reservation.setMontantTotal(montantTotal);
        reservation.setStatut(statut);
        reservation.setClient(client);
        reservation.setEvenement(evenement);

        return reservation;
    }
}