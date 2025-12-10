INSERT INTO UTILISATEUR (nom, email, mot_de_passe, role) VALUES
(
    'Jean Dupont', 
    'jean.dupont@email.fr',
    -- password123
    '"75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8="',
    'CLIENT'
),
(
    'Marie Martin', 
    'marie.martin@email.fr',
    -- password456
    '"xrqRuQ2SLhWYk/RsOH5dwbPcXBAaWkUi8DuYcXeiSpE="',
    'CLIENT'
),
(
    'Pierre Durand', 
    'pierre.durand@email.fr',
    -- password789
    '"XvwrAX2k93NtGSp03eWJE2ngaF1NOPKkVbb82rKC35w="',
    'CLIENT'
);

-- =====================================================
-- INSERTION DES UTILISATEURS (ORGANISATEURS)
-- =====================================================

INSERT INTO UTILISATEUR (nom, email, mot_de_passe, role) VALUES
(
    'Sophie Legrand', 
    'sophie.legrand@events.fr',
    -- org123
    '"0Ia4QWOm3j8x1Ghtvsoxq0hMb4MuLHCrXyFxzO8M/s8="',
    'ORGANISATEUR'
),
(
    'Thomas Bernard', 
    'thomas.bernard@events.fr',
    -- org456
    '"ZHF/RZcfWxLaV+IUg0KDPOnQreJ7y59gw1Jc2Su3ZKU="',
    'ORGANISATEUR'
);

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

INSERT INTO RESERVATION (client_id, evenement_id, nb_places, montant_total, statut, date_reservation) VALUES
(
    (SELECT id FROM UTILISATEUR WHERE email = 'jean.dupont@email.fr'),
    (SELECT id FROM EVENEMENT WHERE titre LIKE 'Concert Rock%'),
    3,
    136.50,
    'CONFIRMEE',
    CURRENT_TIMESTAMP
);

UPDATE EVENEMENT 
SET nb_places_restantes = nb_places_restantes - 3 
WHERE titre LIKE 'Concert Rock%';

INSERT INTO RESERVATION (client_id, evenement_id, nb_places, montant_total, statut, date_reservation) VALUES
(
    (SELECT id FROM UTILISATEUR WHERE email = 'marie.martin@email.fr'),
    (SELECT id FROM EVENEMENT WHERE titre LIKE 'Théâtre%'),
    2,
    70.00,
    'EN_ATTENTE',
    CURRENT_TIMESTAMP
);

UPDATE EVENEMENT 
SET nb_places_restantes = nb_places_restantes - 2 
WHERE titre LIKE 'Théâtre%';
