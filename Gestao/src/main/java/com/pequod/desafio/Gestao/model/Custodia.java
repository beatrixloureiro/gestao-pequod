package com.pequod.desafio.Gestao.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "custodia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Custodia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate mes;

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_ativo", nullable = false)
    private ClasseAtivo classeAtivo;

    @Column(name = "volume_alocado", nullable = false, precision = 18, scale = 2)
    private BigDecimal volumeAlocado;
}