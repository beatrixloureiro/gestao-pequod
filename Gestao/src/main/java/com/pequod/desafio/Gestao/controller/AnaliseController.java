package com.pequod.desafio.Gestao.controller;

import com.pequod.desafio.Gestao.model.ClasseAtivo;
import com.pequod.desafio.Gestao.model.Cliente;
import com.pequod.desafio.Gestao.service.AnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analise")
public class AnaliseController {

    @Autowired
    private AnaliseService analiseService;

    // 1. Receita total dos últimos 12 meses por assessor
    @GetMapping("/receita-total")
    public Map<String, BigDecimal> receitaTotalUltimos12Meses() {
        return analiseService.receitaTotalUltimos12MesesPorAssessor();
    }

    // 2. Número de clientes ativos por assessor
    @GetMapping("/clientes-ativos")
    public Map<String, Long> clientesAtivosPorAssessor() {
        return analiseService.numeroClientesAtivosPorAssessor();
    }

    // 3. Ticket médio por assessor
    @GetMapping("/ticket-medio")
    public Map<String, BigDecimal> ticketMedioPorAssessor() {
        return analiseService.ticketMedioPorAssessor();
    }

    // 4. Top 3 assessores com maior ticket médio
    @GetMapping("/top3-ticket")
    public List<Map.Entry<String, BigDecimal>> top3PorTicketMedio() {
        return analiseService.top3AssessoresPorTicketMedio();
    }

    // 5a. Lista de clientes Private (volume alocado >= 3 milhões)
    @GetMapping("/clientes-private")
    public List<Cliente> clientesPrivate() {
        return analiseService.clientesPrivate();
    }

    // 5b. Quantidade total de clientes Private
    @GetMapping("/clientes-private/quantidade")
    public Long quantidadeClientesPrivate() {
        return (long) analiseService.clientesPrivate().size();
    }

    // 5c. Assessor com mais clientes Private
    @GetMapping("/clientes-private/top-assessor")
    public String assessorComMaisPrivate() {
        return analiseService.privatePorAssessor().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum assessor encontrado");
    }

    // 5d. Receita média gerada pelos clientes Private
    @GetMapping("/clientes-private/receita-media")
    public BigDecimal receitaMediaClientesPrivate() {
        return analiseService.receitaMediaPrivate();
    }

    // 6. Receita mensal por classe de ativo nos últimos 24 meses
    @GetMapping("/receita-mensal-classe-ativo")
    public Map<ClasseAtivo, Map<YearMonth, BigDecimal>> receitaMensalPorClasse() {
        return analiseService.receitaMensalPorClasseAtivo24Meses();
    }

    // 7. Clientes com queda de receita nos últimos 3 meses consecutivos
    @GetMapping("/queda-receita")
    public List<Map<String, Object>> clientesComQuedaSequencial() {
        return analiseService.clientesComQuedaNos3UltimosMeses();
    }
}