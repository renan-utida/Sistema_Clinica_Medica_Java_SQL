package com.example.clinic.ui.console;

import com.example.clinic.dao.jdbc.*;
import com.example.clinic.domain.*;
import com.example.clinic.service.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConsoleMain {

    private static final DateTimeFormatter PADRAO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

    public static void main(String[] args) {
        runConsole();
    }

    private static void runConsole() {
        var consultaRepo = new ConsultaJdbcRepository();
        var medicoDao = new MedicoDao();
        var pacienteDao = new PacienteDao();

        var agendaService = new AgendaService(consultaRepo);
        var medicoService = new MedicoService(medicoDao);
        var pacienteService = new PacienteService(pacienteDao);

        try (Scanner in = new Scanner(System.in)) {
            while (true) {
                exibirMenuConsole();
                System.out.print("Escolha uma opção: ");

                try {
                    int opcao = Integer.parseInt(in.nextLine().trim());

                    switch (opcao) {
                        case 1 -> agendarConsultaConsole(in, agendaService);
                        case 2 -> cadastrarPacienteConsole(in, pacienteService);
                        case 3 -> cadastrarMedicoConsole(in, medicoService);
                        case 4 -> listarPacientesConsole(pacienteService);
                        case 5 -> listarMedicosConsole(medicoService);
                        case 0 -> {
                            System.out.println("Saindo...");
                            return;
                        }
                        default -> System.out.println("Opção inválida!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Digite um número válido!");
                } catch (Exception e) {
                    System.err.println("Erro: " + e.getMessage());
                }

                System.out.println("\nPressione Enter para continuar...");
                in.nextLine();
            }
        }
    }

    private static void exibirMenuConsole() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("        SISTEMA DE CLÍNICA");
        System.out.println("=".repeat(40));
        System.out.println("1 - Agendar Consulta");
        System.out.println("2 - Cadastrar Paciente");
        System.out.println("3 - Cadastrar Médico");
        System.out.println("4 - Listar Pacientes");
        System.out.println("5 - Listar Médicos");
        System.out.println("0 - Sair");
        System.out.println("=".repeat(40));
    }

    private static void agendarConsultaConsole(Scanner in, AgendaService service) {
        System.out.println("\n--- AGENDAR CONSULTA ---");
        System.out.print("ID do paciente: ");
        long pacienteId = Long.parseLong(in.nextLine().trim());

        System.out.print("ID do médico: ");
        long medicoId = Long.parseLong(in.nextLine().trim());

        System.out.print("Início da consulta (dd/MM/yyyy HH:mm): ");
        LocalDateTime inicio = LocalDateTime.parse(in.nextLine().trim(), PADRAO);

        System.out.print("Duração em minutos: ");
        int duracaoMin = Integer.parseInt(in.nextLine().trim());
        LocalDateTime fim = inicio.plusMinutes(duracaoMin);

        var consulta = new Consulta(null, pacienteId, medicoId, inicio, fim);
        Long id = service.agendar(consulta);
        System.out.println("Consulta agendada com sucesso! ID = " + id);
    }

    private static void cadastrarPacienteConsole(Scanner in, PacienteService service) {
        System.out.println("\n--- CADASTRAR PACIENTE ---");
        System.out.print("Nome do paciente: ");
        String nome = in.nextLine().trim();

        System.out.print("Email do paciente: ");
        String email = in.nextLine().trim();

        Long id = service.cadastrar(nome, email);
        System.out.println("Paciente cadastrado com sucesso! ID = " + id);
    }

    private static void cadastrarMedicoConsole(Scanner in, MedicoService service) {
        System.out.println("\n--- CADASTRAR MÉDICO ---");
        System.out.print("Nome do médico: ");
        String nome = in.nextLine().trim();

        System.out.print("CRM do médico (ex: CRM12345): ");
        String crm = in.nextLine().trim();

        Long id = service.cadastrar(nome, crm);
        System.out.println("Médico cadastrado com sucesso! ID = " + id);
    }

    private static void listarPacientesConsole(PacienteService service) {
        System.out.println("\n--- LISTA DE PACIENTES ---");
        List<Paciente> pacientes = service.listarTodos();
        if (pacientes.isEmpty()) {
            System.out.println("Nenhum paciente cadastrado.");
        } else {
            System.out.printf("%-5s %-30s %-30s%n", "ID", "Nome", "Email");
            System.out.println("-".repeat(70));
            for (Paciente p : pacientes) {
                System.out.printf("%-5d %-30s %-30s%n", p.getId(), p.getNome(), p.getEmail());
            }
        }
    }

    private static void listarMedicosConsole(MedicoService service) {
        System.out.println("\n--- LISTA DE MÉDICOS ---");
        List<Medico> medicos = service.listarTodos();
        if (medicos.isEmpty()) {
            System.out.println("Nenhum médico cadastrado.");
        } else {
            System.out.printf("%-5s %-30s %-15s%n", "ID", "Nome", "CRM");
            System.out.println("-".repeat(55));
            for (Medico m : medicos) {
                System.out.printf("%-5d %-30s %-15s%n", m.getId(), m.getNome(), m.getCrm());
            }
        }
    }
}
