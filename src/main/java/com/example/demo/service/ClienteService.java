package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.ClienteModel;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PedidoRepository pedidoRepository;

    public ClienteModel createCliente(ClienteModel cliente) throws Exception{
        if(cliente.getUsername().isBlank() || cliente.getPassword().isEmpty())
            throw new Exception("Nome ou senha invalidos");
        return clienteRepository.save(cliente);
    }

    public List<ClienteModel> getAllClients() {
        return clienteRepository.findAll();
    }

    public ClienteModel findClienteById(long id) {
        if(clienteRepository.existsById(id))
            return clienteRepository.findById(id).get();
        return null;
    }

    public HttpStatus deleteById(long id) {
        if(clienteRepository.existsById(id)){
            if(pedidoRepository.existsByClienteId(id))
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cliente possui pedidos e não pode ser excluído");
            clienteRepository.deleteById(id);
            return HttpStatus.OK;
        }
        return HttpStatus.NOT_FOUND;
    }

    

}
