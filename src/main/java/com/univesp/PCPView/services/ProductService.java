package com.univesp.PCPView.services;

import com.univesp.PCPView.dto.product.request.ProductRequestDTO;
import com.univesp.PCPView.dto.product.request.UpdateProductNameDTO;
import com.univesp.PCPView.dto.product.response.ProductResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentProductException;
import com.univesp.PCPView.models.ProductModel;
import com.univesp.PCPView.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponseDTO salvarProduto (ProductRequestDTO body) {
        ProductModel produto = new ProductModel(body.id(), body.sku(), body.nome());

        productRepository.save(produto);

        return converterProdutoParaResponseDTO(produto);
    }

    public ProductResponseDTO buscarProdutoPorID (String id) {
        ProductModel produto = productRepository.findById(id).orElseThrow(NonExistentProductException::new);

        return converterProdutoParaResponseDTO(produto);
    }

    public List<ProductResponseDTO> buscarTodosProdutos () {
        return productRepository.findAll().stream().map(this::converterProdutoParaResponseDTO).toList();
    }

    @Transactional
    public ProductResponseDTO atualizarNomeProduto (String id, UpdateProductNameDTO body) {
        ProductModel produto = productRepository.findById(id).orElseThrow(NonExistentProductException::new);

        produto.setNome(body.nome());

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
