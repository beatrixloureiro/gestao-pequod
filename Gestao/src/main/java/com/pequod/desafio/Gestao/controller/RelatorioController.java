package com.pequod.desafio.Gestao.controller;

import com.pequod.desafio.Gestao.service.RelatorioPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/relatorio")
public class RelatorioController {

    @Autowired
    private RelatorioPdfService relatorioPdfService;

    // Gera os relatórios e salva o ZIP
    @GetMapping("/gerar-pdf")
    public String gerarPdfZip() {
        try {
            relatorioPdfService.gerarRelatoriosClientesZip();
            return "Relatórios PDF gerados com sucesso! Verifique a pasta 'relatorios' e o arquivo 'relatorios.zip'.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao gerar os relatórios: " + e.getMessage();
        }
    }

    // Serve o arquivo .zip como download
    @GetMapping("/download-zip")
    public ResponseEntity<InputStreamResource> downloadRelatoriosZip() throws IOException {
        File arquivo = new File("relatorios.zip");

        if (!arquivo.exists()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(arquivo));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + arquivo.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(arquivo.length())
                .body(resource);
    }
}