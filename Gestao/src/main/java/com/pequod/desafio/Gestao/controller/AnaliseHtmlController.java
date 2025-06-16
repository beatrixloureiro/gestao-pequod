package com.pequod.desafio.Gestao.controller;

import com.pequod.desafio.Gestao.model.ClasseAtivo;
import com.pequod.desafio.Gestao.model.Cliente;
import com.pequod.desafio.Gestao.service.AnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;

@Controller
@RequestMapping("/analise")
public class AnaliseHtmlController {

    @Autowired
    private AnaliseService analiseService;

    /**
     * Página inicial da análise — mostra os 3 assessores com maior ticket médio por cliente.
     */
    @GetMapping
    public String mostrarAnalise(Model model) {
        model.addAttribute("analises", analiseService.analisarAssessores());
        return "analise"; // HTML: analise.html
    }

    /**
     * Gráfico de evolução da receita por classe de ativo (últimos 24 meses).
     */
    @GetMapping("/grafico-receita")
    public String mostrarGraficoReceita(Model model) {
        Map<ClasseAtivo, Map<YearMonth, BigDecimal>> dados = analiseService.receitaMensalPorClasseAtivo24Meses();

        List<String> meses = dados.values().stream()
                .flatMap(map -> map.keySet().stream())
                .distinct()
                .sorted()
                .map(YearMonth::toString)
                .toList();

        List<BigDecimal> rendaFixa = meses.stream()
                .map(m -> dados.getOrDefault(ClasseAtivo.RENDA_FIXA, Collections.emptyMap())
                        .getOrDefault(YearMonth.parse(m), BigDecimal.ZERO))
                .toList();

        List<BigDecimal> rendaVariavel = meses.stream()
                .map(m -> dados.getOrDefault(ClasseAtivo.RENDA_VARIAVEL, Collections.emptyMap())
                        .getOrDefault(YearMonth.parse(m), BigDecimal.ZERO))
                .toList();

        // Cálculo de crescimento percentual para exibir no gráfico
        BigDecimal crescimentoFixa = calcularCrescimentoPercentual(rendaFixa);
        BigDecimal crescimentoVariavel = calcularCrescimentoPercentual(rendaVariavel);

        model.addAttribute("meses", meses);
        model.addAttribute("rendaFixa", rendaFixa);
        model.addAttribute("rendaVariavel", rendaVariavel);
        model.addAttribute("crescimentoFixa", crescimentoFixa);
        model.addAttribute("crescimentoVariavel", crescimentoVariavel);

        return "grafico-receita"; // HTML: grafico-receita.html
    }

    // Método auxiliar para calcular o crescimento percentual
    private BigDecimal calcularCrescimentoPercentual(List<BigDecimal> valores) {
        if (valores.isEmpty()) return BigDecimal.ZERO;
        BigDecimal inicio = valores.get(0);
        BigDecimal fim = valores.get(valores.size() - 1);
        return (inicio.compareTo(BigDecimal.ZERO) == 0) ? BigDecimal.ZERO :
                fim.subtract(inicio)
                        .divide(inicio, 2, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Página com dados dos clientes Private (volume >= R$ 3 milhões).
     */
    @GetMapping("/clientes-private")
    public String mostrarClientesPrivate(Model model) {
        List<Cliente> clientesPrivate = analiseService.clientesPrivate();
        Map<String, Long> mapa = analiseService.privatePorAssessor();

        String topAssessor = mapa.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum assessor");

        model.addAttribute("quantidade", clientesPrivate.size());
        model.addAttribute("topAssessor", topAssessor);
        model.addAttribute("receitaMedia", analiseService.receitaMediaPrivate());

        return "clientes-private"; // HTML: clientes-private.html
    }

    /**
     * Página com clientes cuja receita caiu nos últimos 3 meses consecutivos.
     */
    @GetMapping("/clientes-queda")
    public String mostrarClientesComQueda(Model model) {
        model.addAttribute("clientesComQueda", analiseService.clientesComQuedaNos3UltimosMeses());
        return "clientes-queda"; // HTML: clientes-queda.html
    }

    /**
     * Dashboard Final com:
     * - Receita total mensal (últimos 12 meses)
     * - Top 5 clientes por receita
     * - Receita por classe de ativo
     * - Receita por assessor
     */
    @GetMapping("/dashboard")
    public String mostrarDashboardFinal(Model model) {
        model.addAttribute("receitaMensal", analiseService.receitaTotalMensalUltimos12Meses());
        model.addAttribute("top5Clientes", analiseService.top5ClientesPorReceita());
        model.addAttribute("receitaPorClasse", analiseService.receitaPorClasseDeAtivo());
        model.addAttribute("receitaPorAssessor", analiseService.receitaPorAssessor());
        return "dashboard-final"; // HTML: dashboard-final.html
    }
}