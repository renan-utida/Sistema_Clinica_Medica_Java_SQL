package com.example.clinic.service;

import com.example.clinic.domain.Paciente;
import com.example.clinic.dao.jdbc.PacienteDao;
import java.util.List;
import java.util.regex.Pattern;

public class PacienteService {

    private final PacienteDao pacienteDao;

    // Pattern para validação de email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public PacienteService(PacienteDao pacienteDao) {
        this.pacienteDao = pacienteDao;
    }

    public Long cadastrar(String nome, String email) {
        // Validações de negócio
        validarNome(nome);
        validarEmail(email);

        Paciente paciente = new Paciente(null, nome.trim(), email.trim().toLowerCase());
        return pacienteDao.salvar(paciente);
    }

    public List<Paciente> listarTodos() {
        return pacienteDao.listarTodos();
    }

    public Paciente buscarPorId(long id) {
        return pacienteDao.buscarPorId(id);
    }

    public void atualizar(Paciente paciente) {
        validarNome(paciente.getNome());
        validarEmail(paciente.getEmail());
        pacienteDao.atualizar(paciente);
    }

    public void deletar(long id) {
        pacienteDao.deletar(id);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do paciente é obrigatório");
        }
        if (nome.trim().length() < 2) {
            throw new IllegalArgumentException("Nome do paciente deve ter pelo menos 2 caracteres");
        }
        if (nome.trim().length() > 120) {
            throw new IllegalArgumentException("Nome do paciente não pode exceder 120 caracteres");
        }
    }

    private void validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }

        String emailLimpo = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(emailLimpo).matches()) {
            throw new IllegalArgumentException("Email deve ter um formato válido (ex: usuario@dominio.com)");
        }

        if (emailLimpo.length() > 120) {
            throw new IllegalArgumentException("Email não pode exceder 120 caracteres");
        }
    }
}