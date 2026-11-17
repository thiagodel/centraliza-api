package com.centraliza.api.dto;

public record ViaCepResponseDTO(

        String cep,
        String logradouro,
        String complemento,
        String bairro,
        String localidade,
        String uf,
        String estado,
        Boolean erro

) {
}
