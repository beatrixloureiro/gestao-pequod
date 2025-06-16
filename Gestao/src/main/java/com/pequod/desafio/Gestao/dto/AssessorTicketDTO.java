package com.pequod.desafio.Gestao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AssessorTicketDTO {
    private String assessor;
    private BigDecimal ticketMedio;
}