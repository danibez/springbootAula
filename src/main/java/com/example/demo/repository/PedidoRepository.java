package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.PedidoModel;

public interface PedidoRepository extends JpaRepository<PedidoModel, Long> {

    boolean existsByClienteId(long clienteId);

    boolean existsByProdutosId(long produtoId);
}