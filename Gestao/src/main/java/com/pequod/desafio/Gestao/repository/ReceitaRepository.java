package com.pequod.desafio.Gestao.repository;

import com.pequod.desafio.Gestao.model.Receita;
import com.pequod.desafio.Gestao.model.Cliente;
import com.pequod.desafio.Gestao.model.ClasseAtivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReceitaRepository extends JpaRepository<Receita, Long> {

    List<Receita> findByCliente(Cliente cliente);

    List<Receita> findByMesAfter(LocalDate data);

    List<Receita> findByClasseAtivoAndMesBetween(ClasseAtivo classeAtivo, LocalDate inicio, LocalDate fim);

    List<Receita> findByClienteAndMesBetween(Cliente cliente, LocalDate inicio, LocalDate fim);
}
