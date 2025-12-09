-- ========================================
-- SCRIPT D'INITIALISATION DES DONNÉES
-- ========================================

-- Vider les tables (pour réinitialisation)
TRUNCATE TABLE RESERVATION CASCADE;
TRUNCATE TABLE EVENEMENT CASCADE;
TRUNCATE TABLE UTILISATEUR CASCADE;

-- Réinitialiser les séquences
ALTER SEQUENCE utilisateur_id_seq RESTART WITH 1;
ALTER SEQUENCE evenement_id_seq RESTART WITH 1;
ALTER SEQUENCE reservation_id_seq RESTART WITH 1;

-- ========================================
-- INSERTION DES UTILISATEURS
-- ========================================

-- CLIENTS (3)
INSERT INTO UTILISATEUR (nom, email, mot_de_passe, role) VALUES
('Jean Dupont', 'jean.dupont@email.fr', 'password123', 'CLIENT'),
('Marie Martin', 'marie.martin@email.fr', 'password456', 'CLIENT'),
('Pierre Durand', 'pierre.durand@email.fr', 'password789', 'CLIENT');

-- ORGANISATEURS (2)
INSERT INTO UTILISATEUR (nom, email, mot_de_passe, role) VALUES
('Sophie Legrand', 'sophie.legrand@events.fr', 'org123', 'ORGANISATEUR'),
('Thomas Bernard', 'thomas.bernard@events.fr', 'org456', 'ORGANISATEUR');

-- ========================================
-- INSERTION DES ÉVÉNEMENTS
-- ========================================

-- Événements (4)
INSERT INTO EVENEMENT (titre, description, lieu, date_evenement, nb_places_totales, nb_places_restantes, prix_base, organisateur_id) VALUES
(
    'Concert Rock - The Beatles Tribute',
    'Une soirée inoubliable avec le meilleur tribute band des Beatles',
    'Zénith de Paris',
    CURRENT_TIMESTAMP + INTERVAL '15 days',
    150,
    150,
    45.50,
    (SELECT id FROM UTILISATEUR WHERE email = 'sophie.legrand@events.fr')
),
(
    'Festival Jazz d''été',
    '3 jours de jazz avec les plus grands artistes internationaux',
    'Parc de la Villette',
    CURRENT_TIMESTAMP + INTERVAL '30 days',
    500,
    500,
    75.00,
    (SELECT id FROM UTILISATEUR WHERE email = 'sophie.legrand@events.fr')
),
(
    'Théâtre - Le Misanthrope',
    'La célèbre pièce de Molière revisitée',
    'Comédie Française',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    80,
    80,
    35.00,
    (SELECT id FROM UTILISATEUR WHERE email = 'thomas.bernard@events.fr')
),
(
    'Stand-up Comedy Night',
    'Une soirée d''humour avec les meilleurs humoristes français',
    'Olympia',
    CURRENT_TIMESTAMP + INTERVAL '20 days',
    200,
    200,
    28.00,
    (SELECT id FROM UTILISATEUR WHERE email = 'thomas.bernard@events.fr')
);

-- ========================================
-- INSERTION DE RÉSERVATIONS DE TEST
-- ========================================

-- Réservation 1 : Jean réserve 3 places pour le concert
INSERT INTO RESERVATION (client_id, evenement_id, nb_places, montant_total, statut, date_reservation) VALUES
(
    (SELECT id FROM UTILISATEUR WHERE email = 'jean.dupont@email.fr'),
    (SELECT id FROM EVENEMENT WHERE titre LIKE 'Concert Rock%'),
    3,
    136.50,
    'CONFIRMEE',
    CURRENT_TIMESTAMP
);

-- Mettre à jour les places restantes
UPDATE EVENEMENT 
SET nb_places_restantes = nb_places_restantes - 3 
WHERE titre LIKE 'Concert Rock%';

-- Réservation 2 : Marie réserve 2 places pour le théâtre
INSERT INTO RESERVATION (client_id, evenement_id, nb_places, montant_total, statut, date_reservation) VALUES
(
    (SELECT id FROM UTILISATEUR WHERE email = 'marie.martin@email.fr'),
    (SELECT id FROM EVENEMENT WHERE titre LIKE 'Théâtre%'),
    2,
    70.00,
    'EN_ATTENTE',
    CURRENT_TIMESTAMP
);

-- Mettre à jour les places restantes
UPDATE EVENEMENT 
SET nb_places_restantes = nb_places_restantes - 2 
WHERE titre LIKE 'Théâtre%';

-- ========================================
-- VÉRIFICATION DES DONNÉES
-- ========================================

-- Afficher le résumé
SELECT 'UTILISATEURS' AS table_name, COUNT(*) AS count FROM UTILISATEUR
UNION ALL
SELECT 'EVENEMENTS', COUNT(*) FROM EVENEMENT
UNION ALL
SELECT 'RESERVATIONS', COUNT(*) FROM RESERVATION;