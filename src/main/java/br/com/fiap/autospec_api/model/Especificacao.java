package br.com.fiap.autospec_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "especificacao")
@Getter
@Setter
public class Especificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String atributo;

    private String valor;

    @ManyToOne
    @JoinColumn(name = "veiculo_id")
    @JsonBackReference
    private Veiculo veiculo;
}