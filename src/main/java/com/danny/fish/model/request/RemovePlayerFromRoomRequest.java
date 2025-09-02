package com.danny.fish.model.request;

public class RemovePlayerFromRoomRequest {
	private long playerId;
	private long roomId;

	public long getPlayerId() {
		return playerId;
	}

	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	public long getRoomId() {
		return roomId;
	}

	public void setRoomId(long roomId) {
		this.roomId = roomId;
	}
}
