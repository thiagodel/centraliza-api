package com.centraliza.api.controller;


import com.centraliza.api.dto.EnderecoResponseDTO;
import com.centraliza.api.service.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/enderecos")
@Tag(name = "Endereços", description = "Consultas de endereços por CEP")
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @Operation(
            summary = "Consultar endereço por CEP",
            description = "Busca informações de endereço utilizando a API ViaCEP."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Endereço encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "CEP inválido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "CEP não encontrado"
            )
    })
    @GetMapping("/{cep}")
    public EnderecoResponseDTO buscarPorCep(
            @Parameter(
             description = "CEP com 8 dígitos",
                    example = "38010000"
            )
            @PathVariable String cep) {

        return enderecoService.buscarPorCep(cep);
    }
}

