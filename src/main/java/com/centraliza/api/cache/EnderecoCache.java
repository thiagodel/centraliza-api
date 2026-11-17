package com.centraliza.api.cache;


import com.centraliza.api.dto.EnderecoResponseDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class EnderecoCache {

    private final ConcurrentHashMap<String, CacheEntry> cache =
            new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, CompletableFuture<EnderecoResponseDTO>>
            requisicoesEmAndamento = new ConcurrentHashMap<>();

    private static final long TEMPO_EXPIRACAO_SEGUNDOS = 300;

    public Optional<EnderecoResponseDTO> buscar(String cep){

        CacheEntry entrada = cache.get(cep);

        if(entrada == null){
            return Optional.empty();
        }
        if (!Instant.now().isBefore(entrada.expiraEm())) {
            cache.remove(cep, entrada);
            return Optional.empty();
        }
        return Optional.of(entrada.endereco());
    }

    public void armazenar(String cep, EnderecoResponseDTO endereco){

        Instant expiracao = Instant.now()
                .plusSeconds(TEMPO_EXPIRACAO_SEGUNDOS);

        cache.put(cep, new CacheEntry(endereco, expiracao));
    }

    private record CacheEntry(
            EnderecoResponseDTO endereco,
            Instant expiraEm
    ) {}

    public EnderecoResponseDTO buscarOuConsultar(
            String cep,
            Supplier<EnderecoResponseDTO> consulta) {

        Optional<EnderecoResponseDTO> enderecoCacheado = buscar(cep);

        if (enderecoCacheado.isPresent()) {
            return enderecoCacheado.get();
        }

        CompletableFuture<EnderecoResponseDTO> novaConsulta =
                new CompletableFuture<>();

        CompletableFuture<EnderecoResponseDTO> consultaExistente =
                requisicoesEmAndamento.putIfAbsent(cep, novaConsulta);

        if (consultaExistente != null) {
            return aguardarResultado(consultaExistente);
        }

        try {
            enderecoCacheado = buscar(cep);

            if (enderecoCacheado.isPresent()) {
                novaConsulta.complete(enderecoCacheado.get());
                return enderecoCacheado.get();
            }

            EnderecoResponseDTO endereco = consulta.get();

            armazenar(cep, endereco);

            novaConsulta.complete(endereco);

            return endereco;

        } catch (RuntimeException | Error ex) {
            novaConsulta.completeExceptionally(ex);
            throw ex;

        } finally {
            requisicoesEmAndamento.remove(cep, novaConsulta);
        }
    }

    private EnderecoResponseDTO aguardarResultado(
            CompletableFuture<EnderecoResponseDTO> consulta) {

        try {
            return consulta.join();

        } catch (CompletionException ex) {
            if (ex.getCause() instanceof RuntimeException causa) {
                throw causa;
            }

            throw ex;
        }
    }

}

