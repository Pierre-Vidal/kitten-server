package com.kitten.kitten_server.dto;

import java.util.Map;

import lombok.Data;

@Data
public class StateUpdateRequest {
	private String code;
	private Map<String, Object> state;
}
