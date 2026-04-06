package com.univesp.PCPView.controllers;

import com.univesp.PCPView.dto.product.request.ProductRequestDTO;
import com.univesp.PCPView.dto.product.request.UpdateProductNameDTO;
import com.univesp.PCPView.dto.product.response.ProductResponseDTO;
import com.univesp.PCPView.infra.security.WebSecurityConfig;
import com.univesp.PCPView.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@Tag(name = "Product Controller", description = "Operações relacionadas aos produtos.")
@SecurityRequirement(name = WebSecurityConfig.SECURITY)
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo produto")
    @ApiResponse(responseCode = "201",description = "Produto cadastrado com sucesso!")
    @ApiResponse(responseCode = "400", description = "Requisição inválida.")
    public ResponseEntity<ProductResponseDTO> registrarProduto(@RequestBody @Valid ProductRequestDTO body){
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.salvarProduto(body));
    }

    @GetMapping
    @Operation(summary = "Procura todos os produtos")
    @ApiResponse(responseCode = "200",description = "Produtos encontrados com sucesso!")
    public ResponseEntity<List<ProductResponseDTO>> buscarTodosProdutos(){
        return ResponseEntity.status(HttpStatus.OK).body(productService.buscarTodosProdutos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Procura um produto pelo seu ID")
    @ApiResponse(responseCode = "200",description = "Produto encontrado com sucesso!")
    @ApiResponse(responseCode = "404",description = "O produto não existe.")
    public ResponseEntity<ProductResponseDTO> buscarProdutoPorID(@PathVariable(value = "id") String id){
        return ResponseEntity.status(HttpStatus.OK).body(productService.buscarProdutoPorID(id));
    }

    @PatchMapping("/updateName/{id}")
    @Operation(summary = "Atualiza o nome de um produto")
    @ApiResponse(responseCode = "200",description = "Produto atualizado com sucesso!")
    @ApiResponse(responseCode = "404",description = "O produto não existe.")
    public ResponseEntity<ProductResponseDTO> atualizarNomeProduto(@PathVariable(value = "id") String id, @RequestBody @Valid UpdateProductNameDTO body){
        return ResponseEntity.status(HttpStatus.OK).body(productService.atualizarNomeProduto(id, body));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deleta um produto pelo seu ID")
    @ApiResponse(responseCode = "204",description = "Produto deletado com sucesso!")
    @ApiResponse(responseCode = "404",description = "O produto não existe.")
    public ResponseEntity<?> deletarProdutoPorID (@PathVariable(value = "id") String id) {
        productService.deletarProduto(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
