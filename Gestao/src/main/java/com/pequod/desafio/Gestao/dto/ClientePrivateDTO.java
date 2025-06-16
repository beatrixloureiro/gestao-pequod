package com.pequod.desafio.Gestao.dto;

import java.math.BigDecimal;

public class ClientePrivateDTO {
    private Long clienteId;
    private String assessor;
    private BigDecimal receita;

    public ClientePrivateDTO(Long clienteId, String assessor, BigDecimal receita) {
        this.clienteId = clienteId;
        this.assessor = assessor;
        this.receita = receita;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getAssessor() {
        return assessor;
    }

    public BigDecimal getReceita() {
        return receita;
    }

    public String getReceitaFormatada() {
        return receita != null
            ? "R$ " + String.format("%,.2f", receita).replace(",", "X").replace(".", ",").replace("X", ".")
            : "R$ 0,00";
    }
}