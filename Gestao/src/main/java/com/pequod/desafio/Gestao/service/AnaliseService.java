package com.pequod.desafio.Gestao.service;

import com.pequod.desafio.Gestao.dto.AnaliseAssessorDTO;
import com.pequod.desafio.Gestao.model.*;
import com.pequod.desafio.Gestao.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnaliseService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private CustodiaRepository custodiaRepository;

    /**
     * Receita total dos últimos 12 meses agrupada por assessor.
     */
    public Map<String, BigDecimal> receitaTotalUltimos12MesesPorAssessor() {
        LocalDate limite = LocalDate.now().minusMonths(12);
        List<Receita> receitas = receitaRepository.findByMesAfter(limite);

        return receitas.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCliente().getAssessor(),
                        Collectors.mapping(Receita::getReceita,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    /**
     * Número de clientes ativos por assessor.
     */
    public Map<String, Long> numeroClientesAtivosPorAssessor() {
        return clienteRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Cliente::getAssessor,
                        Collectors.counting()
                ));
    }

    /**
     * Calcula o ticket médio por assessor com base no último mês de custódia.
     */
    public Map<String, BigDecimal> ticketMedioPorAssessor() {
        LocalDate ultimoMes = custodiaRepository.findAll().stream()
                .map(Custodia::getMes)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        Map<Long, BigDecimal> volumePorCliente = custodiaRepository.findByMes(ultimoMes).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCliente().getId(),
                        Collectors.mapping(Custodia::getVolumeAlocado,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        Map<String, List<BigDecimal>> volumesPorAssessor = new HashMap<>();

        clienteRepository.findAll().forEach(cliente -> {
            BigDecimal volume = volumePorCliente.getOrDefault(cliente.getId(), BigDecimal.ZERO);
            volumesPorAssessor.computeIfAbsent(cliente.getAssessor(), a -> new ArrayList<>()).add(volume);
        });

        Map<String, BigDecimal> ticketMedio = new HashMap<>();
        volumesPorAssessor.forEach((assessor, volumes) -> {
            BigDecimal soma = volumes.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            ticketMedio.put(assessor, soma.divide(BigDecimal.valueOf(volumes.size()), 2, BigDecimal.ROUND_HALF_UP));
        });

        return ticketMedio;
    }

    /**
     * Top 3 assessores com maior ticket médio.
     */
    public List<Map.Entry<String, BigDecimal>> top3AssessoresPorTicketMedio() {
        return ticketMedioPorAssessor().entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .toList();
    }

    /**
     * Lista de clientes com volume >= R$ 3.000.000 (clientes Private).
     */
    public List<Cliente> clientesPrivate() {
        LocalDate ultimoMes = custodiaRepository.findAll().stream()
                .map(Custodia::getMes)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        Map<Long, BigDecimal> volumePorCliente = custodiaRepository.findByMes(ultimoMes).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCliente().getId(),
                        Collectors.mapping(Custodia::getVolumeAlocado,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return clienteRepository.findAll().stream()
                .filter(c -> volumePorCliente.getOrDefault(c.getId(), BigDecimal.ZERO)
                        .compareTo(new BigDecimal("3000000")) >= 0)
                .toList();
    }

    /**
     * Quantidade de clientes Private por assessor.
     */
    public Map<String, Long> privatePorAssessor() {
        return clientesPrivate().stream()
                .collect(Collectors.groupingBy(
                        Cliente::getAssessor,
                        Collectors.counting()
                ));
    }

    /**
     * Receita média dos clientes Private nos últimos 12 meses.
     */
    public BigDecimal receitaMediaPrivate() {
        List<Cliente> privateList = clientesPrivate();
        if (privateList.isEmpty()) return BigDecimal.ZERO;

        LocalDate limite = LocalDate.now().minusMonths(12);
        List<Receita> receitas = receitaRepository.findByMesAfter(limite);

        BigDecimal total = receitas.stream()
                .filter(r -> privateList.contains(r.getCliente()))
                .map(Receita::getReceita)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(privateList.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Receita mensal por classe de ativo nos últimos 24 meses.
     */
    public Map<ClasseAtivo, Map<YearMonth, BigDecimal>> receitaMensalPorClasseAtivo24Meses() {
        LocalDate inicio = LocalDate.now().minusMonths(24);
        List<Receita> receitas = receitaRepository.findByMesAfter(inicio);

        return receitas.stream().collect(Collectors.groupingBy(
                Receita::getClasseAtivo,
                Collectors.groupingBy(
                        r -> YearMonth.from(r.getMes()),
                        Collectors.mapping(Receita::getReceita,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                )
        ));
    }

    /**
     * Clientes cuja receita caiu nos últimos 3 meses consecutivos.
     */
    public List<Map<String, Object>> clientesComQuedaNos3UltimosMeses() {
        List<Map<String, Object>> resultado = new ArrayList<>();
        LocalDate hoje = LocalDate.now();
        List<YearMonth> ultimos4Meses = List.of(
                YearMonth.from(hoje.minusMonths(3)),
                YearMonth.from(hoje.minusMonths(2)),
                YearMonth.from(hoje.minusMonths(1)),
                YearMonth.from(hoje)
        );

        for (Cliente cliente : clienteRepository.findAll()) {
            List<BigDecimal> valores = ultimos4Meses.stream()
                    .map(m -> receitaRepository.findByClienteAndMesBetween(
                            cliente,
                            m.atDay(1),
                            m.atEndOfMonth()
                    ).stream().map(Receita::getReceita)
                            .reduce(BigDecimal.ZERO, BigDecimal::add))
                    .toList();

            if (valores.size() == 4 &&
                valores.get(0).compareTo(valores.get(1)) > 0 &&
                valores.get(1).compareTo(valores.get(2)) > 0 &&
                valores.get(2).compareTo(valores.get(3)) > 0) {

                Map<String, Object> linha = new LinkedHashMap<>();
                linha.put("cliente", cliente.getId());
                linha.put("assessor", cliente.getAssessor());
                linha.put("mes1", valores.get(0));
                linha.put("mes2", valores.get(1));
                linha.put("mes3", valores.get(2));
                linha.put("mes4", valores.get(3));
                resultado.add(linha);
            }
        }

        return resultado;
    }

    /**
     * Retorna os 3 assessores com maior ticket médio (DTO).
     */
    public List<AnaliseAssessorDTO> analisarAssessores() {
        LocalDate dataLimite = LocalDate.now().minusMonths(12);
        List<Cliente> clientes = clienteRepository.findAll();
        List<Receita> receitas = receitaRepository.findAll().stream()
                .filter(r -> r.getMes().isAfter(dataLimite))
                .collect(Collectors.toList());

        List<Custodia> custodias = custodiaRepository.findAll();

        Map<String, List<Cliente>> clientesPorAssessor = clientes.stream()
                .collect(Collectors.groupingBy(Cliente::getAssessor));

        List<AnaliseAssessorDTO> analises = new ArrayList<>();

        for (Map.Entry<String, List<Cliente>> entry : clientesPorAssessor.entrySet()) {
            String assessor = entry.getKey();
            List<Cliente> listaClientes = entry.getValue();

            BigDecimal totalReceita = receitas.stream()
                    .filter(r -> listaClientes.contains(r.getCliente()))
                    .map(Receita::getReceita)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal volumeTotal = custodias.stream()
                    .filter(c -> listaClientes.contains(c.getCliente()))
                    .map(Custodia::getVolumeAlocado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Long qtdClientes = (long) listaClientes.size();
            BigDecimal ticketMedio = qtdClientes == 0 ? BigDecimal.ZERO :
                    volumeTotal.divide(BigDecimal.valueOf(qtdClientes), 2, BigDecimal.ROUND_HALF_UP);

            analises.add(new AnaliseAssessorDTO(assessor, totalReceita, qtdClientes, ticketMedio));
        }

        return analises.stream()
                .sorted(Comparator.comparing(AnaliseAssessorDTO::getTicketMedio).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    // ---------------------
    // MÉTODOS PARA DASHBOARD FINAL
    // ---------------------

    /**
     * Receita total mensal nos últimos 12 meses.
     */
    public Map<String, BigDecimal> receitaTotalMensalUltimos12Meses() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusMonths(12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        return receitaRepository.findByMesAfter(inicio).stream()
                .collect(Collectors.groupingBy(
                        r -> r.getMes().format(formatter),
                        Collectors.mapping(Receita::getReceita,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    /**
     * Top 5 clientes por receita nos últimos 12 meses.
     */
    public List<Object[]> top5ClientesPorReceita() {
        LocalDate limite = LocalDate.now().minusMonths(12);

        return receitaRepository.findByMesAfter(limite).stream()
                .collect(Collectors.groupingBy(
                        Receita::getCliente,
                        Collectors.mapping(Receita::getReceita,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                )).entrySet().stream()
                .sorted(Map.Entry.<Cliente, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .map(e -> new Object[]{e.getKey().getId(), e.getValue()})
                .toList();
    }

    /**
     * Receita por classe de ativo nos últimos 12 meses.
     */
    public Map<String, BigDecimal> receitaPorClasseDeAtivo() {
        LocalDate limite = LocalDate.now().minusMonths(12);
        return receitaRepository.findByMesAfter(limite).stream()
                .collect(Collectors.groupingBy(
                        r -> r.getClasseAtivo().name(),
                        Collectors.mapping(Receita::getReceita,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    /**
     * Receita por assessor nos últimos 12 meses.
     */
    public Map<String, BigDecimal> receitaPorAssessor() {
        LocalDate limite = LocalDate.now().minusMonths(12);
        return receitaRepository.findByMesAfter(limite).stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCliente().getAssessor(),
                        Collectors.mapping(Receita::getReceita,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }
}