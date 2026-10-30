package com.example.demo.dto;

import com.example.demo.model.ClienteModel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
@AllArgsConstructor 
public class ClienteResponse {
    private long id;
    private String username;

    public static ClienteResponse from(ClienteModel cliente){
        return new ClienteResponse(
                    cliente.getId(),
                    cliente.getUsername());
    }
}
