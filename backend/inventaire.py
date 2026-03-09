import json

# Remplace "inventaire_complet.json" par le chemin vers ton fichier JSON
with open("inventaire.json", "r", encoding="utf-8") as f:
    data = json.load(f)

with open("insert_inventaire_1_98.sql", "w", encoding="utf-8") as out:
    for item in data["inventaire"]:
        idp     = item["id_produit"]
        nom     = item["designation"].replace("'", "''")
        pu      = item["prix_unitaire"]
        qte     = item.get("quantite", "NULL")
        code     = "NULL" if item.get("code") is None else f"'{item['code']}'"
        dp      = "NULL" if item.get("dernier_prix") is None else item["dernier_prix"]
        stmt = (
            f"INSERT INTO produit "
            f"(id_produit, designation, prix_unitaire, quantite, code, dernier_prix)\n"
            f"VALUES ({idp}, '{nom}', {pu}, {qte}, {code}, {dp});\n"
        )
        out.write(stmt)

print("Génération terminée : insert_inventaire_1_98.sql")
