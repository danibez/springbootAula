package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.PagamentoModel;
import com.example.demo.model.PedidoModel;
import com.example.demo.repository.PagamentoRepository;
import com.example.demo.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public PagamentoModel criarPagamento(
            Long pedidoId,
            BigDecimal valor,
            String metodo) {

        PedidoModel pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pedido não encontrado: " + pedidoId
            ));

        if (pagamentoRepository.existsByPedidoId(pedidoId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Este pedido já possui um pagamento"
            );
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O valor do pagamento deve ser maior que zero"
            );
        }

        if (metodo == null || metodo.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O método de pagamento é obrigatório"
            );
        }

        PagamentoModel pagamento = new PagamentoModel();
        pagamento.setPedido(pedido);
        pagamento.setValor(valor);
        pagamento.setMetodo(metodo);
        pagamento.setStatus("PENDENTE");

        return pagamentoRepository.save(pagamento);
    }

    public List<PagamentoModel> listarPagamentos() {
        return pagamentoRepository.findAll();
    }

    public PagamentoModel buscarPagamentoPorId(Long id) {
        return pagamentoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pagamento não encontrado: " + id
            ));
    }

    public PagamentoModel atualizarStatus(Long id, String status) {
        PagamentoModel pagamento = buscarPagamentoPorId(id);

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "O status é obrigatório"
            );
        }

        pagamento.setStatus(status.toUpperCase());

        return pagamentoRepository.save(pagamento);
    }

    public void excluirPagamento(Long id) {
        PagamentoModel pagamento = buscarPagamentoPorId(id);
        pagamentoRepository.delete(pagamento);
    }
}