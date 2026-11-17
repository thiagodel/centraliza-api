package com.centraliza.api.service;

import com.centraliza.api.cache.EnderecoCache;
import com.centraliza.api.client.ViaCepClient;
import com.centraliza.api.dto.EnderecoResponseDTO;
import com.centraliza.api.dto.ViaCepResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    private ViaCepClient viaCepClient;

    @Spy
    private EnderecoCache enderecoCache = new EnderecoCache();

    @InjectMocks
    private EnderecoService enderecoService;

    @Test
    void deveRetornarEnderecoDoCacheSemConsultarViaCep() {

        EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais"
        );

        enderecoCache.armazenar("38010000", endereco);

        EnderecoResponseDTO resultado =
                enderecoService.buscarPorCep("38010-000");

        assertEquals(endereco, resultado);

        verifyNoInteractions(viaCepClient);
    }

    @Test
    void deveConsultarViaCepEArmazenarQuandoNaoEncontrarNoCache() {

        ViaCepResponseDTO dadosExternos = new ViaCepResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais",
                false
        );

        when(viaCepClient.buscarPorCep("38010000"))
                .thenReturn(dadosExternos);

        EnderecoResponseDTO resultado =
                enderecoService.buscarPorCep("38010000");

        assertEquals("38010-000", resultado.cep());
        assertEquals("Uberaba", resultado.localidade());

        verify(viaCepClient).buscarPorCep("38010000");

        assertEquals(
                Optional.of(resultado),
                enderecoCache.buscar("38010000")
        );
    }

    @Test
    void deveLancarErroQuandoCepForInvalido() {

        ResponseStatusException excecao = assertThrows(
                ResponseStatusException.class,
                () -> enderecoService.buscarPorCep("123")
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                excecao.getStatusCode()
        );

        verifyNoInteractions(enderecoCache, viaCepClient);
    }

    @Test
    void deveLancarErroQuandoCepNaoForEncontrado() {

        ViaCepResponseDTO resposta = new ViaCepResponseDTO(
                "00000-000",
                "",
                "",
                "",
                "",
                "",
                "",
                true
        );

        when(viaCepClient.buscarPorCep("00000000"))
                .thenReturn(resposta);

        ResponseStatusException excecao = assertThrows(
                ResponseStatusException.class,
                () -> enderecoService.buscarPorCep("00000000")
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                excecao.getStatusCode()
        );

        verify(viaCepClient).buscarPorCep("00000000");

        assertTrue(enderecoCache.buscar("00000000").isEmpty());
    }


    @Test
    void deveCompartilharConsultaQuandoDuasThreadsBuscaremMesmoCep()
            throws Exception {

        ViaCepResponseDTO dadosExternos = new ViaCepResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais",
                false
        );

        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch consultaIniciada = new CountDownLatch(1);
        CountDownLatch liberarConsulta = new CountDownLatch(1);
        CountDownLatch segundaThreadIniciada = new CountDownLatch(1);

        when(viaCepClient.buscarPorCep("38010000"))
                .thenAnswer(invocation -> {

                    consultaIniciada.countDown();

                    if (!liberarConsulta.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Tempo limite da consulta excedido"
                        );
                    }

                    return dadosExternos;
                });

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {

            Future<EnderecoResponseDTO> primeira = executor.submit(() -> {
                inicio.await();
                return enderecoService.buscarPorCep("38010000");
            });

            Future<EnderecoResponseDTO> segunda = executor.submit(() -> {
                inicio.await();
                segundaThreadIniciada.countDown();
                return enderecoService.buscarPorCep("38010000");
            });

            inicio.countDown();

            assertTrue(
                    consultaIniciada.await(2, TimeUnit.SECONDS),
                    "A consulta à ViaCEP não foi iniciada"
            );

            assertTrue(
                    segundaThreadIniciada.await(2, TimeUnit.SECONDS),
                    "A segunda thread não iniciou"
            );

            liberarConsulta.countDown();

            EnderecoResponseDTO resultado1 =
                    primeira.get(3, TimeUnit.SECONDS);

            EnderecoResponseDTO resultado2 =
                    segunda.get(3, TimeUnit.SECONDS);

            assertEquals(resultado1, resultado2);

            // A ViaCEP deve ter sido chamada apenas uma vez.
            verify(viaCepClient, times(1))
                    .buscarPorCep("38010000");

        } finally {
            liberarConsulta.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void devePropagarErroQuandoViaCepEstiverIndisponivel() {

        when(viaCepClient.buscarPorCep("38010000"))
                .thenThrow(new IllegalStateException("ViaCEP indisponível"));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> enderecoService.buscarPorCep("38010000")
        );

        assertEquals("ViaCEP indisponível", excecao.getMessage());

        assertTrue(enderecoCache.buscar("38010000").isEmpty());
    }
}
