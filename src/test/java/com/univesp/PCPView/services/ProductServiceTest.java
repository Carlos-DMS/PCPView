package com.univesp.PCPView.services;

import com.univesp.PCPView.common.ProductConstants;
import com.univesp.PCPView.dto.product.response.ProductResponseDTO;
import com.univesp.PCPView.exceptions.NonExistentProductException;
import com.univesp.PCPView.models.ProductModel;
import com.univesp.PCPView.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    public void registrarProduto_comDadosValidos_retornaProductResponseDTO() {
        // Arrange
        when(productRepository.save(any(ProductModel.class))).thenReturn(ProductConstants.PRODUTO_VALIDO);

        // Act
        ProductResponseDTO sut = productService.registrarProduto(ProductConstants.PRODUTO_REQUEST_VALIDO);

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(ProductConstants.PRODUTO_RESPONSE_VALIDO);
    }

    @Test
    public void registrarProduto_comDadosInvalidos_lancaExcecao() {
        // Arrange
        when(productRepository.save(any(ProductModel.class))).thenThrow(RuntimeException.class);

        // Act e Assert
        assertThatThrownBy(() -> productService.registrarProduto(ProductConstants.PRODUTO_REQUEST_INVALIDO))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void buscarProdutoPorID_comIdExistente_retornaProductResponseDTO() {
        // Arrange
        when(productRepository.findById(ProductConstants.PRODUTO_VALIDO.getId()))
                .thenReturn(Optional.of(ProductConstants.PRODUTO_VALIDO));

        // Act
        ProductResponseDTO sut = productService.buscarProdutoPorID(ProductConstants.PRODUTO_VALIDO.getId());

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(ProductConstants.PRODUTO_RESPONSE_VALIDO);
    }

    @Test
    public void buscarProdutoPorID_comIdInexistente_lancaNonExistentProductException() {
        // Arrange
        when(productRepository.findById(ProductConstants.PRODUTO_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> productService.buscarProdutoPorID(ProductConstants.PRODUTO_ID_INEXISTENTE))
                .isInstanceOf(NonExistentProductException.class);
    }

    @Test
    public void buscarTodosProdutos_quandoExistiremProdutos_retornaListaDeProductResponseDTO() {
        // Arrange
        when(productRepository.findAll()).thenReturn(List.of(ProductConstants.PRODUTO_VALIDO));

        // Act
        List<ProductResponseDTO> sut = productService.buscarTodosProdutos();

        // Assert
        assertThat(sut).isNotEmpty();
        assertThat(sut).hasSize(1);
        assertThat(sut.getFirst()).usingRecursiveComparison().isEqualTo(ProductConstants.PRODUTO_RESPONSE_VALIDO);
    }

    @Test
    public void buscarTodosProdutos_quandoNaoExistiremProdutos_retornaListaVazia() {
        // Arrange
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ProductResponseDTO> sut = productService.buscarTodosProdutos();

        // Assert
        assertThat(sut).isEmpty();
    }

    @Test
    public void atualizarNomeProduto_comIdExistente_retornaProductResponseDTOAtualizado() {
        // Arrange
        ProductModel produtoEmMemoria = new ProductModel(
                ProductConstants.PRODUTO_VALIDO.getId(),
                ProductConstants.PRODUTO_VALIDO.getSku(),
                ProductConstants.PRODUTO_VALIDO.getNome()
        );

        when(productRepository.findById(produtoEmMemoria.getId())).thenReturn(Optional.of(produtoEmMemoria));
        when(productRepository.save(any(ProductModel.class))).thenReturn(produtoEmMemoria);

        // Act
        ProductResponseDTO sut = productService.atualizarNomeProduto(produtoEmMemoria.getId(), ProductConstants.NOVO_NOME_PRODUTO);

        // Assert
        assertThat(sut).usingRecursiveComparison().isEqualTo(ProductConstants.PRODUTO_RESPONSE_ATUALIZADO);
    }

    @Test
    public void atualizarNomeProduto_comIdInexistente_lancaNonExistentProductException() {
        // Arrange
        when(productRepository.findById(ProductConstants.PRODUTO_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> productService.atualizarNomeProduto(ProductConstants.PRODUTO_ID_INEXISTENTE, ProductConstants.NOVO_NOME_PRODUTO))
                .isInstanceOf(NonExistentProductException.class);
    }

    @Test
    public void deletarProduto_comIdExistente_naoLancaExcecao() {
        // Arrange
        when(productRepository.findById(ProductConstants.PRODUTO_VALIDO.getId()))
                .thenReturn(Optional.of(ProductConstants.PRODUTO_VALIDO));

        // Act e Assert
        assertThatCode(() -> productService.deletarProduto(ProductConstants.PRODUTO_VALIDO.getId()))
                .doesNotThrowAnyException();

        verify(productRepository).delete(ProductConstants.PRODUTO_VALIDO);
    }

    @Test
    public void deletarProduto_comIdInexistente_lancaNonExistentProductException() {
        // Arrange
        when(productRepository.findById(ProductConstants.PRODUTO_ID_INEXISTENTE))
                .thenReturn(Optional.empty());

        // Act e Assert
        assertThatThrownBy(() -> productService.deletarProduto(ProductConstants.PRODUTO_ID_INEXISTENTE))
                .isInstanceOf(NonExistentProductException.class);

        verify(productRepository, never()).delete(any());
    }
}