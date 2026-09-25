package br.com.fiap.autospec_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "veiculo")
@Getter
@Setter
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marca;

    private String modelo;

    private String versao;

    @OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Especificacao> especificacoes;
}