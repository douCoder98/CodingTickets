package fr.coding.tickets.test;

import fr.coding.tickets.model.AnnulationTardiveException;
import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.PlacesInsuffisantesException;
import fr.coding.tickets.model.Reservation;
import fr.coding.tickets.model.ReservationInvalideException;
import fr.coding.tickets.model.StatutReservation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'annulation de réservation
 * Teste tous les cas possibles : succès, échecs, cas limites
 */
@DisplayName("Tests d'annulation de réservation")
class ReservationAnnulationTest {

    private Client client;
    private Evenement evenement;
    private Reservation reservation;

    @BeforeEach
    void setUp() throws PlacesInsuffisantesException {
        // Créer un client
        client = new Client("Jean Dupont", "jean@example.com", "password123");

        // Créer un événement dans le futur (dans 7 jours)
        evenement = new Evenement(
            "Concert Rock",
            "Un super concert",
            LocalDateTime.now().plusDays(7),
            "Paris",
            100,
            50.0
        );

        // Créer une réservation de 2 places
        reservation = new Reservation(client, evenement, 2);
    }

    // ========== Tests de succès ==========

    @Test
    @DisplayName("✅ Annulation réussie - Réservation en attente, événement dans le futur")
    void testAnnulation_Reussie_EnAttente() throws Exception {
        // Given
        assertEquals(StatutReservation.EN_ATTENTE, reservation.getStatut());
        assertEquals(98, evenement.getNbPlacesRestantes()); // 100 - 2

        // When
        reservation.annuler();

        // Then
        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
        assertEquals(100, evenement.getNbPlacesRestantes()); // Places libérées
    }

    @Test
    @DisplayName("✅ Annulation réussie - Réservation confirmée")
    void testAnnulation_Reussie_Confirmee() throws Exception {
        // Given
        reservation.confirmer();
        assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());

        // When
        reservation.annuler();

        // Then
        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
        assertEquals(100, evenement.getNbPlacesRestantes());
    }

    @Test
    @DisplayName("✅ Annulation juste avant la limite (25 heures avant)")
    void testAnnulation_LimiteAcceptable() throws Exception {
        // Given - Événement dans 25 heures
        Evenement evenementBientot = new Evenement(
            "Concert Jazz",
            "Description",
            LocalDateTime.now().plusHours(25),
            "Lyon",
            50,
            30.0
        );
        Reservation reservationBientot = new Reservation(client, evenementBientot, 1);

        // When
        reservationBientot.annuler();

        // Then
        assertEquals(StatutReservation.ANNULEE, reservationBientot.getStatut());
        assertEquals(50, evenementBientot.getNbPlacesRestantes());
    }

    // ========== Tests d'échec - Annulation tardive ==========

    @Test
    @DisplayName("❌ Échec - Annulation moins de 24h avant l'événement")
    void testAnnulation_Echec_MoinsDe24h() throws Exception {
        // Given - Événement dans 23 heures
        Evenement evenementProche = new Evenement(
            "Théâtre",
            "Description",
            LocalDateTime.now().plusHours(23),
            "Marseille",
            100,
            25.0
        );
        Reservation reservationProche = new Reservation(client, evenementProche, 3);

        // When & Then
        AnnulationTardiveException exception = assertThrows(
            AnnulationTardiveException.class,
            () -> reservationProche.annuler()
        );

        assertTrue(exception.getMessage().contains("moins de 24h"));
        assertEquals(StatutReservation.EN_ATTENTE, reservationProche.getStatut());
        assertEquals(97, evenementProche.getNbPlacesRestantes()); // Places toujours réservées
    }

    @Test
    @DisplayName("❌ Échec - Annulation le jour même")
    void testAnnulation_Echec_JourMeme() throws Exception {
        // Given - Événement dans 12 heures
        Evenement evenementAujourdhui = new Evenement(
            "Spectacle",
            "Description",
            LocalDateTime.now().plusHours(12),
            "Lille",
            80,
            40.0
        );
        Reservation reservationAujourdhui = new Reservation(client, evenementAujourdhui, 2);

        // When & Then
        assertThrows(
            AnnulationTardiveException.class,
            () -> reservationAujourdhui.annuler()
        );
    }

    @Test
    @DisplayName("❌ Échec - Annulation après le début de l'événement")
    void testAnnulation_Echec_ApresDebut() throws Exception {
        // Given - Événement qui a déjà commencé (dans le passé)
        Evenement evenementPasse = new Evenement(
            "Film",
            "Description",
            LocalDateTime.now().minusHours(2),
            "Nice",
            50,
            15.0
        );
        Reservation reservationPassee = new Reservation(client, evenementPasse, 1);

        // When & Then
        assertThrows(
            AnnulationTardiveException.class,
            () -> reservationPassee.annuler()
        );
    }

    // ========== Tests d'échec - Réservation déjà annulée ==========

    @Test
    @DisplayName("❌ Échec - Réservation déjà annulée")
    void testAnnulation_Echec_DejaAnnulee() throws Exception {
        // Given
        reservation.annuler();
        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());

        // When & Then
        ReservationInvalideException exception = assertThrows(
            ReservationInvalideException.class,
            () -> reservation.annuler()
        );

        assertTrue(exception.getMessage().contains("déjà annulée"));
    }

    @Test
    @DisplayName("❌ Échec - Double annulation")
    void testAnnulation_Echec_DoubleAnnulation() throws Exception {
        // Given
        reservation.annuler();
        int placesApresPremiereAnnulation = evenement.getNbPlacesRestantes();

        // When & Then - Tenter une deuxième annulation
        assertThrows(
            ReservationInvalideException.class,
            () -> reservation.annuler()
        );

        // Les places ne doivent pas être libérées une deuxième fois
        assertEquals(placesApresPremiereAnnulation, evenement.getNbPlacesRestantes());
    }

    // ========== Tests des places libérées ==========

    @Test
    @DisplayName("✅ Vérification - Les places sont bien libérées après annulation")
    void testAnnulation_PlacesLiberees() throws Exception {
        // Given
        int placesAvant = evenement.getNbPlacesRestantes();
        int placesReservees = reservation.getNbPlaces();

        // When
        reservation.annuler();

        // Then
        assertEquals(placesAvant + placesReservees, evenement.getNbPlacesRestantes());
    }

    @Test
    @DisplayName("✅ Vérification - Les places ne dépassent pas le total")
    void testAnnulation_PlacesNeDépassentPasLeTotal() throws Exception {
        // Given - Créer un événement avec toutes les places disponibles
        Evenement evenementComplet = new Evenement(
            "Festival",
            "Description",
            LocalDateTime.now().plusDays(10),
            "Bordeaux",
            10,
            20.0
        );

        // Réserver 5 places
        Reservation reservation1 = new Reservation(client, evenementComplet, 5);
        assertEquals(5, evenementComplet.getNbPlacesRestantes());

        // When - Annuler la réservation
        reservation1.annuler();

        // Then - Les places reviennent à 10, pas plus
        assertEquals(10, evenementComplet.getNbPlacesRestantes());
    }

    // ========== Tests d'intégration avec Client ==========

    @Test
    @DisplayName("✅ Annulation via Client - Réservation existante")
    void testAnnulation_ViaClient_Reussie() throws Exception {
        // Given
        reservation.setId(1L);
        assertEquals(1, client.getReservations().size());

        // When
        client.annulerReservation(1L);

        // Then
        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
        assertEquals(100, evenement.getNbPlacesRestantes());
    }

    @Test
    @DisplayName("❌ Annulation via Client - Réservation inexistante")
    void testAnnulation_ViaClient_ReservationIntrouvable() {
        // Given
        reservation.setId(1L);

        // When & Then
        ReservationInvalideException exception = assertThrows(
            ReservationInvalideException.class,
            () -> client.annulerReservation(999L)
        );

        assertTrue(exception.getMessage().contains("introuvable"));
    }

    // ========== Tests de cas limites ==========

    @Test
    @DisplayName("✅ Cas limite - Événement exactement 24h dans le futur")
    void testAnnulation_Exactement24h() throws Exception {
        // Given - Événement dans exactement 24h + 1 minute
        Evenement evenement24h = new Evenement(
            "Conférence",
            "Description",
            LocalDateTime.now().plusHours(24).plusMinutes(1),
            "Toulouse",
            40,
            35.0
        );
        Reservation reservation24h = new Reservation(client, evenement24h, 1);

        // When & Then - L'annulation devrait réussir (juste après la limite)
        assertDoesNotThrow(() -> reservation24h.annuler());
        assertEquals(StatutReservation.ANNULEE, reservation24h.getStatut());
    }

    @Test
    @DisplayName("❌ Cas limite - Événement dans 23h59")
    void testAnnulation_23h59() throws Exception {
        // Given - Événement dans 23h59
        Evenement evenement23h59 = new Evenement(
            "Exposition",
            "Description",
            LocalDateTime.now().plusHours(23).plusMinutes(59),
            "Nantes",
            30,
            20.0
        );
        Reservation reservation23h59 = new Reservation(client, evenement23h59, 1);

        // When & Then - L'annulation devrait échouer (juste avant la limite)
        assertThrows(
            AnnulationTardiveException.class,
            () -> reservation23h59.annuler()
        );
    }

    @Test
    @DisplayName("✅ Réservation multiple places - Annulation libère toutes les places")
    void testAnnulation_MultiplePlaces() throws Exception {
        // Given - Réservation de 10 places
        Evenement grandEvenement = new Evenement(
            "Grand Gala",
            "Description",
            LocalDateTime.now().plusDays(5),
            "Strasbourg",
            200,
            60.0
        );
        Reservation grandeReservation = new Reservation(client, grandEvenement, 10);
        assertEquals(190, grandEvenement.getNbPlacesRestantes());

        // When
        grandeReservation.annuler();

        // Then
        assertEquals(200, grandEvenement.getNbPlacesRestantes());
    }

    // ========== Test de sécurité/cohérence ==========

    @Test
    @DisplayName("🔒 Sécurité - Le statut change bien avant la libération des places")
    void testAnnulation_OrdreOperations() throws Exception {
        // Given
        int placesAvant = evenement.getNbPlacesRestantes();

        // When
        reservation.annuler();

        // Then - Le statut doit être ANNULEE
        assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
        // Et les places doivent être libérées
        assertTrue(evenement.getNbPlacesRestantes() > placesAvant);
    }

    @Test
    @DisplayName("🔄 Concurrence - Annulation multiple du même événement")
    void testAnnulation_PlusiuersReservationsMemeEvenement() throws Exception {
        // Given - Plusieurs clients avec des réservations
        Client client2 = new Client("Marie Martin", "marie@example.com", "pass456");
        Client client3 = new Client("Paul Durand", "paul@example.com", "pass789");

        Reservation resa2 = new Reservation(client2, evenement, 3);
        Reservation resa3 = new Reservation(client3, evenement, 5);

        assertEquals(90, evenement.getNbPlacesRestantes()); // 100 - 2 - 3 - 5

        // When - Annuler toutes les réservations
        reservation.annuler();  // Libère 2 places
        resa2.annuler();        // Libère 3 places
        resa3.annuler();        // Libère 5 places

        // Then - Toutes les places doivent être libérées
        assertEquals(100, evenement.getNbPlacesRestantes());
    }
}