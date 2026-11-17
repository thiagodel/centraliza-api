package com.centraliza.api.service;

import com.centraliza.api.client.ViaCepClient;
import com.centraliza.api.dto.EnderecoResponseDTO;
import com.centraliza.api.dto.ViaCepResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.centraliza.api.cache.EnderecoCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class EnderecoService {

    private final ViaCepClient viaCepClient;
    private final EnderecoCache enderecoCache;

    private static final Logger log =
            LoggerFactory.getLogger(EnderecoService.class);

    public EnderecoService(ViaCepClient viaCepClient,
                           EnderecoCache enderecoCache) {
        this.viaCepClient = viaCepClient;
        this.enderecoCache = enderecoCache;
    }

    public EnderecoResponseDTO buscarPorCep(String cep) {

       String cepNormalizado = normalizarCep(normalizarCep(cep));

        log.info("Solicitação recebida para o CEP {}", cepNormalizado);

        return enderecoCache.buscarOuConsultar(cepNormalizado, () -> {

            log.info("CACHE MISS - Consultando ViaCEP para o CEP {}", cepNormalizado);

            ViaCepResponseDTO dados = viaCepClient.buscarPorCep(cepNormalizado);

            if (dados == null || Boolean.TRUE.equals(dados.erro())) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "CEP não encontrado"
                );
            }

            EnderecoResponseDTO endereco = new EnderecoResponseDTO(
                    dados.cep(),
                    dados.logradouro(),
                    dados.complemento(),
                    dados.bairro(),
                    dados.localidade(),
                    dados.uf(),
                    dados.estado()
            );

            log.info("CEP {} consultado com sucesso", cepNormalizado);

            return endereco;
        });
    }

    private String normalizarCep(String cep) {

        String cepLimpo = cep.trim();

        if (!cepLimpo.matches("\\d{8}|\\d{5}-\\d{3}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "CEP deve conter 8 dígitos"
            );
        }

        return cepLimpo.replace("-", "");
    }
}

