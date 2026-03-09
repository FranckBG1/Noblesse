-- Migration pour ajouter les champs avance et reste à la table factures

ALTER TABLE factures ADD COLUMN avance DOUBLE DEFAULT 0.0;
ALTER TABLE factures ADD COLUMN reste DOUBLE DEFAULT 0.0;

-- Mettre à jour les factures existantes pour calculer le reste
UPDATE factures f
SET reste = (
    SELECT COALESCE(SUM(v.montant_total), 0)
    FROM ventes v
    WHERE v.id_facture = f.id_facture
) - COALESCE(f.avance, 0);
