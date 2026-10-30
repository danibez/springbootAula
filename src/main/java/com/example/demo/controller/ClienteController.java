package com.example.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ClienteRequest;
import com.example.demo.dto.ClienteResponse;
import com.example.demo.model.ClienteModel;
import com.example.demo.service.ClienteService;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping("/api/v1/cliente")
@RequiredArgsConstructor 
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping("/novoCliente")
    public ClienteResponse novoCliente(@RequestBody ClienteRequest cliente) throws Exception {  
        ClienteModel clienteNovo = new ClienteModel();
        clienteNovo.setPassword(cliente.getPassword());
        clienteNovo.setUsername(cliente.getUsername());

        ClienteModel clienteSalvo = clienteService.createCliente(clienteNovo);

        return ClienteResponse.from(clienteSalvo);
    }

    @GetMapping("/all")
    public List<ClienteResponse> getAllClients() {
        return clienteService.getAllClients().stream().
                        map(ClienteResponse::from).toList();
    }
    

    @GetMapping("/cliente/{id}")
    public ClienteResponse findById(@PathVariable long id) {
        ClienteModel cliente = clienteService.findClienteById(id);
        return ClienteResponse.from(cliente);
    }
    
    @DeleteMapping("/cliente/{id}")
    public HttpStatus deleteById(@PathVariable long id){
        return clienteService.deleteById(id);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!!!";
    }
    
    
}
