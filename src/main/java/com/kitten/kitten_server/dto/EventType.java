package com.kitten.kitten_server.dto;

/**
 * Types d'événements envoyés aux clients via WebSocket
 * Le SDK utilise ces types pour savoir quoi faire avec chaque message
 */
public enum EventType {
	/** Une room vient d'être créée */
	ROOM_CREATED,
	/** Un nouveau joueur a rejoint */
	PLAYER_JOINED,
	/** Un joueur a quitté ou a été déconnecté */
	PLAYER_LEFT,
	/** L'hôte a changé suite à un départ */
	HOST_CHANGED,
	/** L'état partagé du jeu a été mis à jour */
	STATE_UPDATED,
	/** Le statut de la room a changé (WAITING / IN_GAME) */
	STATUS_CHANGED,
	/** Une erreur s'est produite, voir ErrorEvent pour les détails */
	ERROR
}
