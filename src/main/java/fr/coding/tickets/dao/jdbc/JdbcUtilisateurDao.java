package fr.coding.tickets.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.coding.tickets.dao.DaoException;
import fr.coding.tickets.dao.UtilisateurDao;
import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Organisateur;
import fr.coding.tickets.model.Role;
import fr.coding.tickets.model.Utilisateur;
import fr.coding.tickets.util.PasswordHasher;

/**
 * DAO JDBC pour les utilisateurs avec hashing sécurisé des mots de passe
 */
public class JdbcUtilisateurDao implements UtilisateurDao {

    @Override
    public Utilisateur findByEmailAndPassword(String email, String plainPassword) throws DaoException {
        // ÉTAPE 1 : Récupérer l'utilisateur par email uniquement
        String sql = "SELECT * FROM UTILISATEUR WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("mot_de_passe");
                    
                    // ÉTAPE 2 : Vérifier le mot de passe avec le hash stocké
                    if (PasswordHasher.verifyPassword(plainPassword, storedHash)) {
                        // Mot de passe correct, retourner l'utilisateur
                        return mapResultSetToUtilisateur(rs);
                    } else {
                        // Mot de passe incorrect
                        return null;
                    }
                }
                // Email non trouvé
                return null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de l'authentification", e);
        }
    }

    @Override
    public Utilisateur findById(long id) throws DaoException {
        String sql = "SELECT * FROM UTILISATEUR WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche de l'utilisateur", e);
        }
    }

    @Override
    public List<Client> findAllClients() throws DaoException {
        String sql = "SELECT * FROM UTILISATEUR WHERE role = 'CLIENT'";
        List<Client> clients = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clients.add((Client) mapResultSetToUtilisateur(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des clients", e);
        }

        return clients;
    }

    @Override
    public List<Organisateur> findAllOrganisateurs() throws DaoException {
        String sql = "SELECT * FROM UTILISATEUR WHERE role = 'ORGANISATEUR'";
        List<Organisateur> organisateurs = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                organisateurs.add((Organisateur) mapResultSetToUtilisateur(rs));
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des organisateurs", e);
        }

        return organisateurs;
    }

    @Override
    public void create(Utilisateur utilisateur) throws DaoException {
        String sql = "INSERT INTO UTILISATEUR (nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getEmail());
            
            // IMPORTANT : Hash le mot de passe avant insertion
            String hashedPassword = PasswordHasher.hashPassword(utilisateur.getMotDePasse());
            stmt.setString(3, hashedPassword);
            
            stmt.setString(4, utilisateur.getRole().name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    utilisateur.setId(rs.getLong("id"));
                    System.out.println("✓ Utilisateur créé avec mot de passe hashé - ID: " + utilisateur.getId());
                } else {
                    throw new DaoException("Échec de la création de l'utilisateur");
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création de l'utilisateur", e);
        }
    }

    @Override
    public void update(Utilisateur utilisateur) throws DaoException {
        String sql = "UPDATE UTILISATEUR SET nom = ?, email = ?, mot_de_passe = ?, role = ? WHERE id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, utilisateur.getNom());
            stmt.setString(2, utilisateur.getEmail());
            
            // IMPORTANT : Hash le mot de passe si nécessaire
            String password = utilisateur.getMotDePasse();
            if (!PasswordHasher.isHashed(password)) {
                password = PasswordHasher.hashPassword(password);
            }
            stmt.setString(3, password);
            
            stmt.setObject(4, utilisateur.getRole().name(), java.sql.Types.OTHER);
            stmt.setLong(5, utilisateur.getId());

            int rows = stmt.executeUpdate();
            System.out.println("✓ Utilisateur mis à jour - Lignes affectées : " + rows);

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL update utilisateur : " + e.getMessage());
            throw new DaoException("Erreur lors de la mise à jour de l'utilisateur", e);
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
     * Mappe un ResultSet vers un objet Utilisateur (Client ou Organisateur)
     */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nom = rs.getString("nom");
        String email = rs.getString("email");
        String motDePasse = rs.getString("mot_de_passe");
        String roleStr = rs.getString("role");
        Role role = Role.valueOf(roleStr);

        Utilisateur utilisateur;
        if (role == Role.CLIENT) {
            utilisateur = new Client(nom, email, motDePasse);
        } else {
            utilisateur = new Organisateur(nom, email, motDePasse);
        }

        utilisateur.setId(id);

        return utilisateur;
    }

    /**
     * Méthode utilitaire pour vérifier si un email existe déjà
     */
    public boolean emailExists(String email) throws DaoException {
        String sql = "SELECT COUNT(*) FROM UTILISATEUR WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la vérification de l'email", e);
        }
    }
}