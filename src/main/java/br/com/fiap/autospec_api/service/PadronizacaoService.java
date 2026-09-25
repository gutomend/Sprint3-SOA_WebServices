package br.com.fiap.autospec_api.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PadronizacaoService {

    private final Map<String, String> dicionario = new HashMap<>();

    public PadronizacaoService() {

        dicionario.put("horse power", "Potencia");
        dicionario.put("hp", "Potencia");
        dicionario.put("potência máxima", "Potencia");

        dicionario.put("engine", "Motor");
        dicionario.put("motor", "Motor");

        dicionario.put("torque máximo", "Torque");
        dicionario.put("torque", "Torque");
    }

    public String padronizar(String atributo) {

        String chave = atributo.toLowerCase().trim();

        return dicionario.getOrDefault(
                chave,
                "Nao padronizado"
        );
    }
}