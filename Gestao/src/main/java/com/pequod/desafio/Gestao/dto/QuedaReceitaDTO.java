package com.pequod.desafio.Gestao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class QuedaReceitaDTO {
    private Long clienteId;
    private String assessor;
    private BigDecimal mes1;
    private BigDecimal mes2;
    private BigDecimal mes3;
    private BigDecimal mes4;
}
