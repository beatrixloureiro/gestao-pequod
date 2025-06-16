package com.pequod.desafio.Gestao.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "cliente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    private Long id;

    @Column(nullable = false)
    private String assessor;

    @Column(name = "inicio_relacionamento", nullable = false)
    private LocalDate inicioRelacionamento;
}