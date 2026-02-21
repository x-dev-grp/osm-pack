package com.abiooc.inventory_service.Enum;

public enum StatutBonCommande {
    BROUILLON,      // Création manuelle non finalisée
    EN_ATTENTE,     // En attente de validation admin (⭐ automatique)
    VALIDE,         // Validé par admin, commande envoyée
    RECU,           // Reçu en totalité
    PARTIELLEMENT_RECU, // Reçu partiellement
    ANNULE          // Annulé par admin
}
