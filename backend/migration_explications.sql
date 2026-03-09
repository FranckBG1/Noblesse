-- Migration pour ajouter le champ explications aux factures
-- Date: 2024

-- Ajouter la colonne explications
ALTER TABLE factures ADD COLUMN explications TEXT;

-- Commentaire sur la colonne
COMMENT ON COLUMN factures.explications IS 'Explications ou notes sur la facture ou les modifications effectuées';
