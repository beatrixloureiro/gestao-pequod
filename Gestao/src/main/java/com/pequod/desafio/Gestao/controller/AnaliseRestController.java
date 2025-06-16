package com.pequod.desafio.Gestao.controller;

import com.pequod.desafio.Gestao.dto.AnaliseAssessorDTO;
import com.pequod.desafio.Gestao.service.AnaliseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analise")
public class AnaliseRestController {

    @Autowired
    private AnaliseService analiseService;

    @GetMapping("/top-assessores")
    public List<AnaliseAssessorDTO> getTopAssessores() {
        return analiseService.analisarAssessores();
    }
}