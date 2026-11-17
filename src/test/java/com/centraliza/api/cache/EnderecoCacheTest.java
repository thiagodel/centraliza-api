package com.centraliza.api.cache;

import com.centraliza.api.dto.EnderecoResponseDTO;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

class EnderecoCacheTest {

    @Test
    void deveRetornarEnderecoQuandoCepEstiverNoCache() {

        EnderecoCache cache = new EnderecoCache();

        EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais"
        );

        cache.armazenar("38010000", endereco);

        Optional<EnderecoResponseDTO> resultado =
                cache.buscar("38010000");

        assertTrue(resultado.isPresent());
        assertEquals(endereco, resultado.get());
    }

    @Test
    void deveRetornarVazioQuandoCepNaoEstiverNoCache() {

        EnderecoCache cache = new EnderecoCache();

        Optional<EnderecoResponseDTO> resultado =
                cache.buscar("38010000");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveRetornarVazioQuandoCacheExpirar() throws InterruptedException {

        EnderecoCache cache = new EnderecoCache();

        EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais"
        );

        cache.armazenar("38010000", endereco);

        Thread.sleep(1100);

        Optional<EnderecoResponseDTO> resultado =
                cache.buscar("38010000");

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveCompartilharConsultaEntreDuasThreads() throws Exception {

        EnderecoCache cache = new EnderecoCache();

        EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais"
        );

        AtomicInteger quantidadeConsultas = new AtomicInteger(0);

        CountDownLatch consultaIniciada = new CountDownLatch(1);
        CountDownLatch liberarConsulta = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            java.util.function.Supplier<EnderecoResponseDTO> consulta = () -> {
                quantidadeConsultas.incrementAndGet();
                consultaIniciada.countDown();

                try {
                    if (!liberarConsulta.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException("Tempo limite excedido");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }

                return endereco;
            };

            Future<EnderecoResponseDTO> primeira = executor.submit(
                    () -> cache.buscarOuConsultar("38010000", consulta)
            );

            assertTrue(consultaIniciada.await(2, TimeUnit.SECONDS));

            Future<EnderecoResponseDTO> segunda = executor.submit(
                    () -> cache.buscarOuConsultar("38010000", consulta)
            );

            liberarConsulta.countDown();

            EnderecoResponseDTO resultado1 = primeira.get(3, TimeUnit.SECONDS);
            EnderecoResponseDTO resultado2 = segunda.get(3, TimeUnit.SECONDS);

            assertEquals(endereco, resultado1);
            assertEquals(endereco, resultado2);
            assertEquals(1, quantidadeConsultas.get());

        } finally {
            liberarConsulta.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void deveRemoverConsultaEmAndamentoQuandoOcorrerFalha() {

        EnderecoCache cache = new EnderecoCache();

        String cep = "38010000";

        java.util.function.Supplier<EnderecoResponseDTO> consulta = () -> {
            throw new IllegalStateException("Falha na API externa");
        };

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> cache.buscarOuConsultar(cep, consulta)
        );

        assertEquals("Falha na API externa", excecao.getMessage());

        // Uma nova consulta para o mesmo CEP deve ser permitida.
        EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                "38010-000",
                "Rua das Flores",
                "",
                "Centro",
                "Uberaba",
                "MG",
                "Minas Gerais"
        );

        EnderecoResponseDTO resultado = cache.buscarOuConsultar(
                cep,
                () -> endereco
        );

        assertEquals(endereco, resultado);
    }
}
