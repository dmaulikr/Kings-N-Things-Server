package com.kings.http;

public class HttpResponseError {
	private ResponseError responseError;
	
	public HttpResponseError() {
		
	}
	
	public HttpResponseError(ResponseError responseError) {
		setResponseError(responseError);
	}
	
	public enum ResponseError {
		BAD_USERNAME_AND_PASSWORD,
		ALREADY_LOGGED_IN,
		ALREADY_REGISTERED,
		GENERIC_ERROR,
		NOT_LOGGED_IN,
		NO_LOBBY_AVAILABLE,
		NO_OPEN_LOBBY_FOR_USER,
		UNABLE_TO_HOST_LOBBY,
		USER_ALREADY_IN_GAME,
		WRONG_PHASE,
		NOT_YOUR_TURN,
		BAD_MOVE,
		BATTLE_DOES_NOT_EXIST,
		BATTLE_OVER,
		BATTLE_NOT_STARTED,
		WRONG_ROUND,
		WRONG_ROUND_STATE
	}

	public ResponseError getResponseError() {
		return responseError;
	}

	public void setResponseError(ResponseError responseError) {
		this.responseError = responseError;
	}
	
	
}
