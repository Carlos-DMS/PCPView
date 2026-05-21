package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.product.request.ProductRequestDTO;
import com.univesp.PCPView.dto.product.response.ProductResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentProductException;
import com.univesp.PCPView.models.ProductModel;
import com.univesp.PCPView.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponseDTO registrarProduto(ProductRequestDTO body) {
        ProductModel produto = new ProductModel(body.id(), body.sku(), body.nome());

        productRepository.save(produto);

        return converterProdutoParaResponseDTO(produto);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO buscarProdutoPorID (String id) {
        ProductModel produto = productRepository.findById(id).orElseThrow(NonExistentProductException::new);

        return converterProdutoParaResponseDTO(produto);
    }

    public List<ProductResponseDTO> buscarTodosProdutos () {
        return productRepository.findAll().stream().map(this::converterProdutoParaResponseDTO).toList();
    }

    @Transactional
    public ProductResponseDTO atualizarNomeProduto (String id, String nome) {
        ProductModel produto = productRepository.findById(id).orElseThrow(NonExistentProductException::new);

        produto.setNome(nome);

        productRepository.save(produto);

        return converterProdutoParaResponseDTO(produto);
    }

    @Transactional
    public void deletarProduto (String id) {
        ProductModel produto = productRepository.findById(id).orElseThrow(NonExistentProductException::new);

        productRepository.delete(produto);
    }

    private ProductResponseDTO converterProdutoParaResponseDTO (ProductModel produto) {
        return new ProductResponseDTO(produto.getId(), produto.getSku(), produto.getNome());
    }
}