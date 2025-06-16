package com.pequod.desafio.Gestao.service;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.pequod.desafio.Gestao.model.Cliente;
import com.pequod.desafio.Gestao.model.Receita;
import com.pequod.desafio.Gestao.model.Custodia;
import com.pequod.desafio.Gestao.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class RelatorioPdfService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private CustodiaRepository custodiaRepository;

    private final String outputDir = "relatorios";

    public void gerarRelatoriosClientesZip() throws Exception {
        // Cria pasta se não existir
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        // Gera um PDF por cliente
        for (Cliente cliente : clienteRepository.findAll()) {
            gerarRelatorioIndividual(cliente);
        }

        // Compactar tudo em um .zip
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream("relatorios.zip"))) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        ZipEntry entry = new ZipEntry(f.getName());
                        zipOut.putNextEntry(entry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zipOut.write(buffer, 0, length);
                        }
                        zipOut.closeEntry();
                    }
                }
            }
        }
    }

    public void gerarRelatorioIndividual(Cliente cliente) throws Exception {
        String filePath = outputDir + "/Relatorio_" + cliente.getId() + ".pdf";

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");

        doc.add(new Paragraph("Relatório do Cliente")
                .setBold()
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Cliente ID: " + cliente.getId()));
        doc.add(new Paragraph("Assessor: " + cliente.getAssessor()));
        doc.add(new Paragraph(" "));

        Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 3, 3}))
                .useAllAvailableWidth();
        table.addHeaderCell("Mês");
        table.addHeaderCell("Classe de Ativo");
        table.addHeaderCell("Volume Alocado");
        table.addHeaderCell("Receita");

        java.util.List<Custodia> custodias = custodiaRepository.findByCliente(cliente);
        java.util.List<Receita> receitas = receitaRepository.findByCliente(cliente);

        // Indexa receitas por mês + classe para facilitar busca
        Map<String, BigDecimal> receitaPorMesAtivo = new HashMap<>();
        for (Receita r : receitas) {
            String chave = r.getMes().format(fmt) + "-" + r.getClasseAtivo();
            receitaPorMesAtivo.merge(chave, r.getReceita(), BigDecimal::add);
        }

        for (Custodia c : custodias) {
            String mesFormatado = c.getMes().format(fmt);
            String chave = mesFormatado + "-" + c.getClasseAtivo();
            BigDecimal receita = receitaPorMesAtivo.getOrDefault(chave, BigDecimal.ZERO);

            table.addCell(mesFormatado);
            table.addCell(c.getClasseAtivo().name());
            table.addCell("R$ " + c.getVolumeAlocado().setScale(2));
            table.addCell("R$ " + receita.setScale(2));
        }

        doc.add(table);
        doc.close();
    }
}