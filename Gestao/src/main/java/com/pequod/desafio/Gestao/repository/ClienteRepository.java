package com.pequod.desafio.Gestao.repository;

import com.pequod.desafio.Gestao.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByAssessor(String assessor);
}