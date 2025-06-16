package com.pequod.desafio.Gestao.dto;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class AnaliseAssessorDTO {
    private String assessor;
    private BigDecimal receitaTotal;
    private Long clientesAtivos;
    private BigDecimal ticketMedio;

    public AnaliseAssessorDTO(String assessor, BigDecimal receitaTotal, Long clientesAtivos, BigDecimal ticketMedio) {
        this.assessor = assessor;
        this.receitaTotal = receitaTotal;
        this.clientesAtivos = clientesAtivos;
        this.ticketMedio = ticketMedio;
    }

    public String getAssessor() {
        return assessor;
    }

    public BigDecimal getReceitaTotal() {
        return receitaTotal;
    }

    public Long getClientesAtivos() {
        return clientesAtivos;
    }

    public BigDecimal getTicketMedio() {
        return ticketMedio;
    }

    // Método para retornar a receita formatada
    public String getReceitaTotalFormatada() {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            return receitaTotal != null ? nf.format(receitaTotal) : "0,00";
    }

    public String getTicketMedioFormatado() {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            return ticketMedio != null ? nf.format(ticketMedio) : "0,00";
    }

}