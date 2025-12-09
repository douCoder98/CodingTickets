-- ========================================
-- TYPES ENUM
-- ========================================

-- Type ENUM pour les rôles
CREATE TYPE role_type AS ENUM ('CLIENT', 'ORGANISATEUR');

-- Type ENUM pour les statuts de réservation
CREATE TYPE statut_reservation_type AS ENUM ('EN_ATTENTE', 'CONFIRMEE', 'ANNULEE');

-- ========================================
-- TABLE UTILISATEUR
-- ========================================
CREATE TABLE UTILISATEUR (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role role_type NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimisation
CREATE INDEX idx_utilisateur_email ON UTILISATEUR(email);
CREATE INDEX idx_utilisateur_role ON UTILISATEUR(role);

-- ========================================
-- TABLE EVENEMENT
-- ========================================
CREATE TABLE EVENEMENT (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    description TEXT,
    lieu VARCHAR(200) NOT NULL,
    date_evenement TIMESTAMP NOT NULL,
    nb_places_totales INTEGER NOT NULL,
    nb_places_restantes INTEGER NOT NULL,
    prix_base NUMERIC(10, 2) NOT NULL,
    organisateur_id BIGINT NOT NULL,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Contraintes
    CONSTRAINT fk_evenement_organisateur 
        FOREIGN KEY (organisateur_id) REFERENCES UTILISATEUR(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_places_restantes 
        CHECK (nb_places_restantes >= 0),
    CONSTRAINT chk_places_coherence 
        CHECK (nb_places_restantes <= nb_places_totales),
    CONSTRAINT chk_prix_positif 
        CHECK (prix_base > 0)
);

-- Index pour optimisation
CREATE INDEX idx_evenement_date ON EVENEMENT(date_evenement);
CREATE INDEX idx_evenement_organisateur ON EVENEMENT(organisateur_id);
CREATE INDEX idx_evenement_places ON EVENEMENT(nb_places_restantes);

-- ========================================
-- TABLE RESERVATION
-- ========================================
CREATE TABLE RESERVATION (
    id BIGSERIAL PRIMARY KEY,
    date_reservation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nb_places INTEGER NOT NULL,
    montant_total NUMERIC(10, 2) NOT NULL,
    statut statut_reservation_type NOT NULL DEFAULT 'EN_ATTENTE',
    client_id BIGINT NOT NULL,
    evenement_id BIGINT NOT NULL,
    
    -- Contraintes
    CONSTRAINT fk_reservation_client 
        FOREIGN KEY (client_id) REFERENCES UTILISATEUR(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_reservation_evenement 
        FOREIGN KEY (evenement_id) REFERENCES EVENEMENT(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_nb_places_positif 
        CHECK (nb_places > 0),
    CONSTRAINT chk_montant_positif 
        CHECK (montant_total >= 0)
);

-- Index pour optimisation
CREATE INDEX idx_reservation_client ON RESERVATION(client_id);
CREATE INDEX idx_reservation_evenement ON RESERVATION(evenement_id);
CREATE INDEX idx_reservation_statut ON RESERVATION(statut);
CREATE INDEX idx_reservation_date ON RESERVATION(date_reservation);