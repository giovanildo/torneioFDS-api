package com.giovanildo.torneiofds.service;

import com.giovanildo.torneiofds.model.EAtleta;
import com.giovanildo.torneiofds.repository.EAtletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EAtletaService {

    private final EAtletaRepository eAtletaRepository;
    private final PasswordEncoder passwordEncoder;

    public List<EAtleta> listarTodos() {
        return eAtletaRepository.findAll();
    }

    public EAtleta buscarPorId(Long id) {
        return eAtletaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("EAtleta nao encontrado"));
    }

    public EAtleta buscarPorLogin(String login) {
        return eAtletaRepository.findByLogin(login)
                .orElseThrow(() -> new NoSuchElementException("EAtleta nao encontrado: " + login));
    }

    @Transactional
    public EAtleta registrar(String nome, String login, String senha) {
        if (eAtletaRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Login '" + login + "' ja esta em uso");
        }
        EAtleta eAtleta = new EAtleta(nome, login, passwordEncoder.encode(senha));
        return eAtletaRepository.save(eAtleta);
    }
}
