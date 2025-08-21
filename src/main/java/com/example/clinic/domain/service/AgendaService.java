package com.example.clinic.domain.service;

import com.example.clinic.domain.model.Consulta;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class AgendaService {

    public interface ConsultaRepository {
        List<Consulta> listarPorMedicoNoIntervalo(long medicoId, LocalDateTime inicio, LocalDateTime fim);
        Long salvar(Consulta c);
    }

    private final ConsultaRepository repository;

    public AgendaService(ConsultaRepository repository) {
        this.repository = repository;
    }

    // Caso de uso: agendar consulta com regras
    public Long agendar(Consulta consulta) {
        // Aplicar todas as validações
        validarDuracaoMinima(consulta);
        validarHorarioComercial(consulta);
        validarAntecedencia(consulta.getInicio());
        validarChoqueDeHorario(consulta);

        return repository.salvar(consulta);
    }

    // Regra de negócio: duração mínima de 15 minutos (movida de Consulta)
    private void validarDuracaoMinima(Consulta consulta) {
        long min = Duration.between(consulta.getInicio(), consulta.getFim()).toMinutes();
        if (min < 15) {
            throw new IllegalArgumentException("Consulta deve ter no mínimo 15 minutos");
        }
    }

    // Regra: horário comercial (08:00 - 18:00) (movida de Consulta)
    private void validarHorarioComercial(Consulta consulta) {
        int hIni = consulta.getInicio().getHour();
        int hFim = consulta.getFim().getHour();

        // Verifica se o horário de início está dentro do comercial
        if (hIni < 8 || hIni >= 18) {
            throw new IllegalArgumentException("Horário de início fora do horário comercial (08:00-18:00)");
        }

        // Verifica se o horário de fim não ultrapassa 18:00
        // Se fim é exatamente 18:00 (minuto 0), ainda é válido
        if (hFim > 18 || (hFim == 18 && consulta.getFim().getMinute() > 0)) {
            throw new IllegalArgumentException("Horário de fim fora do horário comercial (08:00-18:00)");
        }
    }

    private void validarAntecedencia(LocalDateTime inicio) {
        long minutos = Duration.between(LocalDateTime.now(), inicio).toMinutes();
        if (minutos < 60) {
            throw new IllegalArgumentException("Consulta deve ser marcada com antecedência mínima de 60 minutos");
        }
    }

    private void validarChoqueDeHorario(Consulta nova) {
        List<Consulta> existentes = repository.listarPorMedicoNoIntervalo(
                nova.getMedicoId(), nova.getInicio(), nova.getFim());
        boolean conflita = existentes.stream().anyMatch(c ->
                c.getInicio().isBefore(nova.getFim()) && nova.getInicio().isBefore(c.getFim())
        );
        if (conflita) throw new IllegalStateException("Médico já possui consulta no horário");
    }
}
