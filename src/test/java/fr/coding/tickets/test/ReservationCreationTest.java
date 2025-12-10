package fr.coding.tickets.test;
import fr.coding.tickets.model.Client;
import fr.coding.tickets.model.Evenement;
import fr.coding.tickets.model.Reservation;
import fr.coding.tickets.model.StatutReservation;
import fr.coding.tickets.model.PlacesInsuffisantesException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la création de réservation
 * Teste tous les scénarios : succès, échecs, validations, calculs
 */
@DisplayName("Tests de création de réservation")
class ReservationCreationTest {

    private Client client;
    private Evenement evenement;

    @BeforeEach
    void setUp() {
        client = new Client("Jean Dupont", "jean@example.com", "password123");
        evenement = new Evenement(
            "Concert Rock",
            "Un super concert",
            LocalDateTime.now().plusDays(30),
            "Paris",
            100,
            50.0
        );
    }

    // ========================================
    // Tests de succès
    // ========================================

    @Nested
    @DisplayName("✅ Tests de succès")
    class SuccessTests {

        @Test
        @DisplayName("Création réussie - Réservation basique (1 place)")
        void testCreation_Reussie_1Place() throws Exception {
            // When
            Reservation reservation = new Reservation(client, evenement, 1);

            // Then
            assertNotNull(reservation);
            assertEquals(client, reservation.getClient());
            assertEquals(evenement, reservation.getEvenement());
            assertEquals(1, reservation.getNbPlaces());
            assertEquals(50.0, reservation.getMontantTotal());
            assertEquals(StatutReservation.EN_ATTENTE, reservation.getStatut());
            assertNotNull(reservation.getDateReservation());
        }

        @Test
        @DisplayName("Création réussie - Réservation multiple places")
        void testCreation_Reussie_MultiplePlaces() throws Exception {
            // When
            Reservation reservation = new Reservation(client, evenement, 5);

            // Then
            assertEquals(5, reservation.getNbPlaces());
            assertEquals(250.0, reservation.getMontantTotal()); // 5 * 50.0
        }

        @Test
        @DisplayName("Création réussie - Toutes les places disponibles")
        void testCreation_Reussie_ToutesLesPlaces() throws Exception {
            // When
            Reservation reservation = new Reservation(client, evenement, 100);

            // Then
            assertEquals(100, reservation.getNbPlaces());
            assertEquals(5000.0, reservation.getMontantTotal()); // 100 * 50.0
            assertEquals(0, evenement.getNbPlacesRestantes());
        }

        @Test
        @DisplayName("Vérification - Les places sont déduites de l'événement")
        void testCreation_PlacesDeduites() throws Exception {
            // Given
            int placesInitiales = evenement.getNbPlacesRestantes();

            // When
            Reservation reservation = new Reservation(client, evenement, 10);

            // Then
            assertEquals(placesInitiales - 10, evenement.getNbPlacesRestantes());
            assertEquals(90, evenement.getNbPlacesRestantes());
        }

        @Test
        @DisplayName("Vérification - La réservation est ajoutée au client")
        void testCreation_AjouteeAuClient() throws Exception {
            // Given
            assertEquals(0, client.getReservations().size());

            // When
            Reservation reservation = new Reservation(client, evenement, 2);

            // Then
            assertEquals(1, client.getReservations().size());
            assertTrue(client.getReservations().contains(reservation));
        }

        @Test
        @DisplayName("Calcul correct - Montant avec différents prix")
        void testCreation_CalculMontant_DifferentsPrix() throws Exception {
            // Given - Événement à 25€
            Evenement evenementPasCher = new Evenement(
                "Cinéma",
                "Film",
                LocalDateTime.now().plusDays(5),
                "Lyon",
                50,
                25.0
            );

            // When
            Reservation reservation = new Reservation(client, evenementPasCher, 3);

            // Then
            assertEquals(75.0, reservation.getMontantTotal()); // 3 * 25.0
        }

        @Test
        @DisplayName("Date de réservation - Définie automatiquement")
        void testCreation_DateReservationAutomatique() throws Exception {
            // Given
            LocalDateTime avant = LocalDateTime.now();

            // When
            Reservation reservation = new Reservation(client, evenement, 1);

            // Then
            LocalDateTime apres = LocalDateTime.now();
            assertNotNull(reservation.getDateReservation());
            assertTrue(reservation.getDateReservation().isAfter(avant.minusSeconds(1)));
            assertTrue(reservation.getDateReservation().isBefore(apres.plusSeconds(1)));
        }

        @Test
        @DisplayName("Statut initial - EN_ATTENTE")
        void testCreation_StatutInitial() throws Exception {
            // When
            Reservation reservation = new Reservation(client, evenement, 1);

            // Then
            assertEquals(StatutReservation.EN_ATTENTE, reservation.getStatut());
        }
    }

    // ========================================
    // Tests d'échec - Places insuffisantes
    // ========================================

    @Nested
    @DisplayName("❌ Tests d'échec - Places insuffisantes")
    class PlacesInsuffisantesTests {

        @Test
        @DisplayName("Échec - Demande de plus de places que disponibles")
        void testCreation_Echec_PasAssezDePlaces() {
            // Given - Événement avec 100 places
            
            // When & Then
            PlacesInsuffisantesException exception = assertThrows(
                PlacesInsuffisantesException.class,
                () -> new Reservation(client, evenement, 101)
            );

            assertTrue(exception.getMessage().contains("Places insuffisantes"));
            assertTrue(exception.getMessage().contains("Demandé: 101"));
            assertTrue(exception.getMessage().contains("Disponible: 100"));
        }

        @Test
        @DisplayName("Échec - Événement complet (0 places)")
        void testCreation_Echec_EvenementComplet() throws Exception {
            // Given - Réserver toutes les places
            new Reservation(client, evenement, 100);
            assertEquals(0, evenement.getNbPlacesRestantes());

            // When & Then
            assertThrows(
                PlacesInsuffisantesException.class,
                () -> new Reservation(client, evenement, 1)
            );
        }

        @Test
        @DisplayName("Échec - Plus de places après plusieurs réservations")
        void testCreation_Echec_ApresPlusiersReservations() throws Exception {
            // Given - Plusieurs clients réservent
            Client client2 = new Client("Marie", "marie@example.com", "pass");
            Client client3 = new Client("Paul", "paul@example.com", "pass");

            new Reservation(client, evenement, 40);
            new Reservation(client2, evenement, 40);
            // Reste 20 places

            // When & Then
            assertThrows(
                PlacesInsuffisantesException.class,
                () -> new Reservation(client3, evenement, 21)
            );
        }

        @Test
        @DisplayName("Vérification - État inchangé après échec")
        void testCreation_EtatInchangeApresEchec() {
            // Given
            int placesAvant = evenement.getNbPlacesRestantes();
            int reservationsAvant = client.getReservations().size();

            // When & Then
            assertThrows(
                PlacesInsuffisantesException.class,
                () -> new Reservation(client, evenement, 150)
            );

            // Vérifier que rien n'a changé
            assertEquals(placesAvant, evenement.getNbPlacesRestantes());
            assertEquals(reservationsAvant, client.getReservations().size());
        }
    }

    // ========================================
    // Tests d'échec - Paramètres invalides
    // ========================================

    @Nested
    @DisplayName("❌ Tests d'échec - Paramètres invalides")
    class ParametresInvalidesTests {

        @Test
        @DisplayName("Échec - Client null")
        void testCreation_Echec_ClientNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(null, evenement, 1)
            );

            assertEquals("Client et événement requis", exception.getMessage());
        }

        @Test
        @DisplayName("Échec - Événement null")
        void testCreation_Echec_EvenementNull() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(client, null, 1)
            );

            assertEquals("Client et événement requis", exception.getMessage());
        }

        @Test
        @DisplayName("Échec - Client et événement null")
        void testCreation_Echec_ClientEtEvenementNull() {
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(null, null, 1)
            );
        }

        @Test
        @DisplayName("Échec - Nombre de places zéro")
        void testCreation_Echec_ZeroPlaces() {
            // When & Then
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(client, evenement, 0)
            );

            assertEquals("Le nombre de places doit être positif", exception.getMessage());
        }

        @Test
        @DisplayName("Échec - Nombre de places négatif")
        void testCreation_Echec_PlacesNegatives() {
            // When & Then
            assertThrows(
                IllegalArgumentException.class,
                () -> new Reservation(client, evenement, -5)
            );
        }
    }

    // ========================================
    // Tests de calcul du montant
    // ========================================

    @Nested
    @DisplayName("💰 Tests de calcul du montant")
    class CalculMontantTests {

        @Test
        @DisplayName("Calcul correct - 1 place")
        void testCalculMontant_1Place() throws Exception {
            // When
            Reservation reservation = new Reservation(client, evenement, 1);

            // Then
            assertEquals(50.0, reservation.getMontantTotal());
        }

        @Test
        @DisplayName("Calcul correct - 10 places")
        void testCalculMontant_10Places() throws Exception {
            // When
            Reservation reservation = new Reservation(client, evenement, 10);

            // Then
            assertEquals(500.0, reservation.getMontantTotal());
        }

        @Test
        @DisplayName("Calcul correct - Prix décimal")
        void testCalculMontant_PrixDecimal() throws Exception {
            // Given - Événement à 15.50€
            Evenement evenementDecimal = new Evenement(
                "Théâtre",
                "Pièce",
                LocalDateTime.now().plusDays(10),
                "Marseille",
                50,
                15.50
            );

            // When
            Reservation reservation = new Reservation(client, evenementDecimal, 3);

            // Then
            assertEquals(46.5, reservation.getMontantTotal()); // 3 * 15.50
        }

        @Test
        @DisplayName("Calcul correct - Prix très élevé")
        void testCalculMontant_PrixEleve() throws Exception {
            // Given - Événement VIP à 500€
            Evenement evenementVIP = new Evenement(
                "Gala VIP",
                "Soirée exclusive",
                LocalDateTime.now().plusDays(60),
                "Monaco",
                20,
                500.0
            );

            // When
            Reservation reservation = new Reservation(client, evenementVIP, 2);

            // Then
            assertEquals(1000.0, reservation.getMontantTotal()); // 2 * 500.0
        }

        @Test
        @DisplayName("Calcul correct - Prix minimal")
        void testCalculMontant_PrixMinimal() throws Exception {
            // Given - Événement gratuit (ou presque)
            Evenement evenementGratuit = new Evenement(
                "Conférence gratuite",
                "Avec donation suggérée",
                LocalDateTime.now().plusDays(15),
                "Toulouse",
                200,
                0.01 // 1 centime
            );

            // When
            Reservation reservation = new Reservation(client, evenementGratuit, 5);

            // Then
            assertEquals(0.05, reservation.getMontantTotal(), 0.001); // 5 * 0.01
        }
    }

    // ========================================
    // Tests de concurrence
    // ========================================

    @Nested
    @DisplayName("🔄 Tests de concurrence")
    class ConcurrenceTests {

        @Test
        @DisplayName("Plusieurs clients réservent en séquence")
        void testCreation_PlusiersClients_Sequence() throws Exception {
            // Given
            Client client1 = new Client("Alice", "alice@example.com", "pass1");
            Client client2 = new Client("Bob", "bob@example.com", "pass2");
            Client client3 = new Client("Charlie", "charlie@example.com", "pass3");

            // When
            Reservation resa1 = new Reservation(client1, evenement, 30);
            Reservation resa2 = new Reservation(client2, evenement, 30);
            Reservation resa3 = new Reservation(client3, evenement, 30);

            // Then
            assertEquals(10, evenement.getNbPlacesRestantes()); // 100 - 30 - 30 - 30
            assertEquals(30, resa1.getNbPlaces());
            assertEquals(30, resa2.getNbPlaces());
            assertEquals(30, resa3.getNbPlaces());
        }

        @Test
        @DisplayName("Dernière place disponible - Course entre 2 clients")
        void testCreation_DernierePlaceDisponible() throws Exception {
            // Given - Réserver 99 places
            new Reservation(client, evenement, 99);
            assertEquals(1, evenement.getNbPlacesRestantes());

            Client client2 = new Client("Marie", "marie@example.com", "pass");

            // When - Le premier qui arrive gagne
            Reservation resa = new Reservation(client2, evenement, 1);

            // Then
            assertNotNull(resa);
            assertEquals(0, evenement.getNbPlacesRestantes());

            // Le suivant doit échouer
            Client client3 = new Client("Paul", "paul@example.com", "pass");
            assertThrows(
                PlacesInsuffisantesException.class,
                () -> new Reservation(client3, evenement, 1)
            );
        }

        @Test
        @DisplayName("Plusieurs réservations pour le même client")
        void testCreation_MemeClient_PlusiersReservations() throws Exception {
            // When
            Reservation resa1 = new Reservation(client, evenement, 10);
            Reservation resa2 = new Reservation(client, evenement, 5);
            Reservation resa3 = new Reservation(client, evenement, 3);

            // Then
            assertEquals(3, client.getReservations().size());
            assertEquals(82, evenement.getNbPlacesRestantes()); // 100 - 10 - 5 - 3
            
            assertTrue(client.getReservations().contains(resa1));
            assertTrue(client.getReservations().contains(resa2));
            assertTrue(client.getReservations().contains(resa3));
        }
    }

    // ========================================
    // Tests d'intégration
    // ========================================

    @Nested
    @DisplayName("🔗 Tests d'intégration")
    class IntegrationTests {

        @Test
        @DisplayName("Cycle complet - Réservation → Confirmation")
        void testIntegration_ReservationEtConfirmation() throws Exception {
            // Given
            Reservation reservation = new Reservation(client, evenement, 5);
            assertEquals(StatutReservation.EN_ATTENTE, reservation.getStatut());

            // When
            reservation.confirmer();

            // Then
            assertEquals(StatutReservation.CONFIRMEE, reservation.getStatut());
            assertEquals(95, evenement.getNbPlacesRestantes());
        }

        @Test
        @DisplayName("Cycle complet - Réservation → Annulation")
        void testIntegration_ReservationEtAnnulation() throws Exception {
            // Given
            Reservation reservation = new Reservation(client, evenement, 5);
            assertEquals(95, evenement.getNbPlacesRestantes());

            // When
            reservation.annuler();

            // Then
            assertEquals(StatutReservation.ANNULEE, reservation.getStatut());
            assertEquals(100, evenement.getNbPlacesRestantes()); // Places libérées
        }

        @Test
        @DisplayName("Scénario réaliste - Plusieurs réservations et annulations")
        void testIntegration_ScenarioRealiste() throws Exception {
            // Scénario : Concert avec plusieurs clients
            Client alice = new Client("Alice", "alice@example.com", "pass1");
            Client bob = new Client("Bob", "bob@example.com", "pass2");
            Client charlie = new Client("Charlie", "charlie@example.com", "pass3");

            // Alice réserve 20 places
            Reservation resaAlice = new Reservation(alice, evenement, 20);
            assertEquals(80, evenement.getNbPlacesRestantes());

            // Bob réserve 30 places
            Reservation resaBob = new Reservation(bob, evenement, 30);
            assertEquals(50, evenement.getNbPlacesRestantes());

            // Charlie réserve 10 places
            Reservation resaCharlie = new Reservation(charlie, evenement, 10);
            assertEquals(40, evenement.getNbPlacesRestantes());

            // Bob annule sa réservation
            resaBob.annuler();
            assertEquals(70, evenement.getNbPlacesRestantes()); // 40 + 30

            // Un nouveau client peut maintenant réserver
            Client david = new Client("David", "david@example.com", "pass4");
            Reservation resaDavid = new Reservation(david, evenement, 25);
            assertEquals(45, evenement.getNbPlacesRestantes());

            // Vérifications finales
            assertEquals(StatutReservation.EN_ATTENTE, resaAlice.getStatut());
            assertEquals(StatutReservation.ANNULEE, resaBob.getStatut());
            assertEquals(StatutReservation.EN_ATTENTE, resaCharlie.getStatut());
            assertEquals(StatutReservation.EN_ATTENTE, resaDavid.getStatut());
        }
    }

    // ========================================
    // Tests de cas limites
    // ========================================

    @Nested
    @DisplayName("🎯 Tests de cas limites")
    class CasLimitesTests {

        @Test
        @DisplayName("Cas limite - Événement avec 1 seule place")
        void testCasLimite_UnePlaceUniquement() {
            // Given
            Evenement petitEvent = new Evenement(
                "Rencontre privée",
                "One-on-one",
                LocalDateTime.now().plusDays(5),
                "Paris",
                1, // Une seule place
                100.0
            );

            // When & Then - Première réservation OK
            assertDoesNotThrow(() -> new Reservation(client, petitEvent, 1));

            // Deuxième réservation échoue
            Client client2 = new Client("Marie", "marie@example.com", "pass");
            assertThrows(
                PlacesInsuffisantesException.class,
                () -> new Reservation(client2, petitEvent, 1)
            );
        }

        @Test
        @DisplayName("Cas limite - Événement avec beaucoup de places")
        void testCasLimite_BeaucoupDePlaces() throws Exception {
            // Given
            Evenement grandEvent = new Evenement(
                "Festival",
                "Grand festival",
                LocalDateTime.now().plusDays(90),
                "Stade",
                50000, // 50 000 places
                75.0
            );

            // When
            Reservation reservation = new Reservation(client, grandEvent, 1000);

            // Then
            assertNotNull(reservation);
            assertEquals(49000, grandEvent.getNbPlacesRestantes());
            assertEquals(75000.0, reservation.getMontantTotal()); // 1000 * 75
        }

        @Test
        @DisplayName("Cas limite - Réservation juste après création événement")
        void testCasLimite_ReservationImmediate() throws Exception {
            // Given - Créer événement et réserver immédiatement
            Evenement nouvelEvent = new Evenement(
                "Concert surprise",
                "Annoncé aujourd'hui",
                LocalDateTime.now().plusDays(7),
                "Lille",
                100,
                40.0
            );

            // When - Réservation immédiate
            Reservation reservation = new Reservation(client, nouvelEvent, 5);

            // Then
            assertNotNull(reservation);
            assertNotNull(reservation.getDateReservation());
            assertEquals(95, nouvelEvent.getNbPlacesRestantes());
        }
    }
}