package com.univesp.PCPView.common;

import com.univesp.PCPView.dto.product.request.ProductRequestDTO;
import com.univesp.PCPView.dto.product.response.ProductResponseDTO;
import com.univesp.PCPView.models.ProductModel;

public class ProductConstants {

    public static final ProductModel PRODUTO_VALIDO = new ProductModel("PROD-01", "SKU-123", "PARAFUSO ACO");
    public static final ProductRequestDTO PRODUTO_REQUEST_VALIDO = new ProductRequestDTO("PROD-01", "SKU-123", "PARAFUSO ACO");
    public static final ProductRequestDTO PRODUTO_REQUEST_INVALIDO = new ProductRequestDTO("", "", "");
    public static final ProductResponseDTO PRODUTO_RESPONSE_VALIDO = new ProductResponseDTO("PROD-01", "SKU-123", "PARAFUSO ACO");

    public static final String PRODUTO_ID_INEXISTENTE = "ID-INEXISTENTE";
    public static final String NOVO_NOME_PRODUTO = "PARAFUSO INOX";
    public static final ProductResponseDTO PRODUTO_RESPONSE_ATUALIZADO = new ProductResponseDTO("PROD-01", "SKU-123", "PARAFUSO INOX");
}
