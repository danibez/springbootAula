package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.PagamentoModel;

public interface PagamentoRepository extends JpaRepository<PagamentoModel, Long> {

    boolean existsByPedidoId(Long pedidoId);
}