package com.pequod.desafio.Gestao.repository;

import com.pequod.desafio.Gestao.model.Custodia;
import com.pequod.desafio.Gestao.model.Cliente;
import com.pequod.desafio.Gestao.model.ClasseAtivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CustodiaRepository extends JpaRepository<Custodia, Long> {

    List<Custodia> findByCliente(Cliente cliente);

    List<Custodia> findByMes(LocalDate mes);

    List<Custodia> findByClienteAndMes(Cliente cliente, LocalDate mes);

    List<Custodia> findByClasseAtivoAndMesBetween(ClasseAtivo classeAtivo, LocalDate inicio, LocalDate fim);
}