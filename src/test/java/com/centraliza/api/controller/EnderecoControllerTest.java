package com.centraliza.api.controller;

import com.centraliza.api.service.EnderecoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.centraliza.api.dto.EnderecoResponseDTO;
import org.springframework.web.server.ResponseStatusException;


@WebMvcTest(EnderecoController.class)
   public class EnderecoControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private EnderecoService enderecoService;



    @Test
    void deveRetornarEnderecoQuandoCepForEncontrado() throws Exception {

        EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais"
        );

        when(enderecoService.buscarPorCep("38010000"))
                .thenReturn(endereco);

        mockMvc.perform(get("/api/enderecos/38010000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("38010-000"))
                .andExpect(jsonPath("$.localidade").value("Uberaba"))
                .andExpect(jsonPath("$.uf").value("MG"))
                .andExpect(jsonPath("$.estado").value("Minas Gerais"));

        verify(enderecoService).buscarPorCep("38010000");
    }


    @Test
    void deveRetornar404QuandoCepNaoForEncontrado() throws Exception {

        when(enderecoService.buscarPorCep("00000000"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CEP não encontrado"
                ));

        mockMvc.perform(get("/api/enderecos/00000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("CEP não encontrado"));

        verify(enderecoService).buscarPorCep("00000000");
    }

    @Test
    void deveRetornar400QuandoCepForInvalido() throws Exception {

        when(enderecoService.buscarPorCep("123"))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "CEP deve conter 8 dígitos"
                ));

        mockMvc.perform(get("/api/enderecos/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Bad Request"))
                .andExpect(jsonPath("$.mensagem")
                        .value("CEP deve conter 8 dígitos"));

        verify(enderecoService).buscarPorCep("123");
    }




}

