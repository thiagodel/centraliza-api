package com.centraliza.api;

import com.centraliza.api.client.ViaCepClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ViaCepClientTest {

    @Autowired
    private ViaCepClient viaCepClient;

    @Test
    void deveConsultarCep() {

        var endereco = viaCepClient.buscarPorCep("38010000");

        System.out.println(endereco);
    }
}
