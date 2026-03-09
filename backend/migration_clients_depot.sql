-- Migration pour ajouter le support des clients et factures de dépôt

-- Ajouter la colonne type_facture à la table factures
ALTER TABLE factures ADD COLUMN type_facture VARCHAR(20) DEFAULT 'CLASSIQUE';

-- Ajouter la colonne id_client à la table factures
ALTER TABLE factures ADD COLUMN id_client INTEGER;

-- Créer un index sur id_client pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_factures_id_client ON factures(id_client);

-- Mettre à jour les factures existantes pour avoir le type CLASSIQUE
UPDATE factures SET type_facture = 'CLASSIQUE' WHERE type_facture IS NULL;

-- Migrer les noms de clients existants vers la table clients
INSERT INTO clients (nom, date_creation)
SELECT DISTINCT nom_client, datetime('now')
FROM factures
WHERE nom_client IS NOT NULL 
  AND nom_client NOT IN (SELECT nom FROM clients)
ORDER BY nom_client;

-- Lier les factures existantes aux clients créés
UPDATE factures 
SET id_client = (
  SELECT idClient 
  FROM clients 
  WHERE clients.nom = factures.nom_client
)
WHERE nom_client IS NOT NULL;

-- Supprimer la colonne nom_client devenue redondante
CREATE TABLE factures_new (
  idFacture INTEGER PRIMARY KEY AUTOINCREMENT,
  status VARCHAR(20),
  date_creation DATETIME,
  derniere_modif DATETIME,
  avance REAL DEFAULT 0.0,
  reste REAL DEFAULT 0.0,
  type_facture VARCHAR(20) DEFAULT 'CLASSIQUE',
  id_client INTEGER,
  id_utilisateur INTEGER,
  FOREIGN KEY (id_client) REFERENCES clients(idClient),
  FOREIGN KEY (id_utilisateur) REFERENCES users(idUtilisateur)
);

INSERT INTO factures_new SELECT idFacture, status, date_creation, derniere_modif, avance, reste, type_facture, id_client, id_utilisateur FROM factures;

DROP TABLE factures;

ALTER TABLE factures_new RENAME TO factures;

CREATE INDEX IF NOT EXISTS idx_factures_id_client ON factures(id_client);

-- Afficher un résumé de la migration
SELECT 
  'Migration terminée' as status,
  (SELECT COUNT(*) FROM clients) as total_clients,
  (SELECT COUNT(*) FROM factures WHERE id_client IS NOT NULL) as factures_liees,
  (SELECT COUNT(*) FROM factures WHERE type_facture = 'CLASSIQUE') as factures_classiques,
  (SELECT COUNT(*) FROM factures WHERE type_facture = 'DEPOT') as factures_depot;
