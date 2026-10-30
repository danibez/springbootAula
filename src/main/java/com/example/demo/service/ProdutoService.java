package com.example.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.ProdutoModel;
import com.example.demo.repository.PedidoRepository;
import com.example.demo.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProdutoService {
    
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;

    public ProdutoModel criarProduto(ProdutoModel produto){
        return produtoRepository.save(produto);
    }

    public List<ProdutoModel> listarProdutos(){
        return produtoRepository.findAll();
    }

    public ProdutoModel buscarProdutoPorId(long id){
        return produtoRepository.findById(id)   
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado com ID: " + id
                ));
    }

    public ProdutoModel atualizarProduto(long id, ProdutoModel produto){
        ProdutoModel antigo = buscarProdutoPorId(id);

        antigo.setNome(produto.getNome());
        antigo.setPreco(produto.getPreco());
        antigo.setQuantidade(produto.getQuantidade());

        return produtoRepository.save(antigo);
    }

    public void excluirProduto(long id){
        if(pedidoRepository.existsByProdutosId(id))
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Produto está em pedidos e não pode ser excluído");
        produtoRepository.deleteById(id);
    }

}
