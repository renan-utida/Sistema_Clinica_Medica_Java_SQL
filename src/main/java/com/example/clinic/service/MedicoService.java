package com.example.clinic.service;

import com.example.clinic.domain.Medico;
import com.example.clinic.dao.jdbc.MedicoDao;
import java.util.List;

public class MedicoService {

    private final MedicoDao medicoDao;

    public MedicoService(MedicoDao medicoDao) {
        this.medicoDao = medicoDao;
    }

    public Long cadastrar(String nome, String crm) {
        // Validações de negócio
        validarNome(nome);
        validarCrm(crm);

        Medico medico = new Medico(null, nome.trim(), crm.trim().toUpperCase());
        return medicoDao.salvar(medico);
    }

    public List<Medico> listarTodos() {
        return medicoDao.listarTodos();
    }

    public Medico buscarPorId(long id) {
        return medicoDao.buscarPorId(id);
    }

    public void atualizar(Medico medico) {
        validarNome(medico.getNome());
        validarCrm(medico.getCrm());
        medicoDao.atualizar(medico);
    }

    public void deletar(long id) {
        medicoDao.deletar(id);
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do médico é obrigatório");
        }
        if (nome.trim().length() < 2) {
            throw new IllegalArgumentException("Nome do médico deve ter pelo menos 2 caracteres");
        }
        if (nome.trim().length() > 120) {
            throw new IllegalArgumentException("Nome do médico não pode exceder 120 caracteres");
        }
    }

    private void validarCrm(String crm) {
        if (crm == null || crm.trim().isEmpty()) {
            throw new IllegalArgumentException("CRM é obrigatório");
        }

        String crmLimpo = crm.trim().toUpperCase();

        // Validar formato básico do CRM (CRM seguido de números)
        if (!crmLimpo.matches("CRM\\d{4,6}")) {
            throw new IllegalArgumentException("CRM deve estar no formato CRM seguido de 4 a 6 dígitos (ex: CRM12345)");
        }

        if (crmLimpo.length() > 20) {
            throw new IllegalArgumentException("CRM não pode exceder 20 caracteres");
        }
    }
}