package com.example.clinic;

import com.example.clinic.domain.model.*;
import com.example.clinic.domain.service.*;
import com.example.clinic.infra.dao.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {

    private static final DateTimeFormatter PADRAO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

    public static void main(String[] args) {
        String ui = parseArg(args, "--ui");

        if ("console".equalsIgnoreCase(ui)) {
            runConsole();
        } else if ("swing".equalsIgnoreCase(ui)) {
            runSwing();
        } else {
            System.out.println("""
                Uso:
                  java ... com.example.clinic.App --ui=console   # entrada via Scanner
                  java ... com.example.clinic.App --ui=swing     # entrada via Swing

                Formato de data/hora: dd/MM/yyyy HH:mm  (ex.: 21/08/2025 10:00)
                """);
        }
    }

    private static String parseArg(String[] args, String key) {
        if (args == null) return null;
        String prefix = key + "=";
        for (String a : args) {
            if (a != null && a.startsWith(prefix)) {
                return a.substring(prefix.length());
            }
        }
        return null;
    }

    /* =========================
       MODO CONSOLE (Scanner)
       ========================= */
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

    /* =========================
       MODO SWING (JPanel)
       ========================= */
    private static void runSwing() {
        // Look & Feel Nimbus (opcional)
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            var consultaRepo = new ConsultaJdbcRepository();
            var medicoDao = new MedicoDao();
            var pacienteDao = new PacienteDao();

            var agendaService = new AgendaService(consultaRepo);
            var medicoService = new MedicoService(medicoDao);
            var pacienteService = new PacienteService(pacienteDao);

            JFrame frame = new JFrame("Sistema de Clínica");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);

            // Painel principal com CardLayout para alternar telas
            JPanel mainPanel = new JPanel(new CardLayout());

            // Menu principal
            JPanel menuPanel = criarMenuSwing(mainPanel, agendaService, medicoService, pacienteService);
            mainPanel.add(menuPanel, "MENU");

            // Telas específicas
            mainPanel.add(criarTelaAgendarConsulta(mainPanel, agendaService), "AGENDAR");
            mainPanel.add(criarTelaCadastrarPaciente(mainPanel, pacienteService), "PACIENTE");
            mainPanel.add(criarTelaCadastrarMedico(mainPanel, medicoService), "MEDICO");

            frame.setContentPane(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static JPanel criarMenuSwing(JPanel mainPanel, AgendaService agendaService,
                                         MedicoService medicoService, PacienteService pacienteService) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel titulo = new JLabel("Sistema de Clínica", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 24));
        c.gridx = 0; c.gridy = 0;
        panel.add(titulo, c);

        JButton btnAgendar = new JButton("Agendar Consulta");
        btnAgendar.addActionListener(e -> ((CardLayout) mainPanel.getLayout()).show(mainPanel, "AGENDAR"));
        c.gridy++;
        panel.add(btnAgendar, c);

        JButton btnPaciente = new JButton("Cadastrar Paciente");
        btnPaciente.addActionListener(e -> ((CardLayout) mainPanel.getLayout()).show(mainPanel, "PACIENTE"));
        c.gridy++;
        panel.add(btnPaciente, c);

        JButton btnMedico = new JButton("Cadastrar Médico");
        btnMedico.addActionListener(e -> ((CardLayout) mainPanel.getLayout()).show(mainPanel, "MEDICO"));
        c.gridy++;
        panel.add(btnMedico, c);

        JButton btnSair = new JButton("Sair");
        btnSair.addActionListener(e -> System.exit(0));
        c.gridy++;
        panel.add(btnSair, c);

        return panel;
    }

    private static JPanel criarTelaAgendarConsulta(JPanel mainPanel, AgendaService service) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtPaciente = new JTextField(15);
        txtPaciente.setToolTipText("Ex.: 1");
        JTextField txtMedico = new JTextField(15);
        txtMedico.setToolTipText("Ex.: 10");
        JTextField txtInicio = new JTextField(15);
        txtInicio.setToolTipText("Formato: dd/MM/yyyy HH:mm");
        JTextField txtDuracao = new JTextField(15);
        txtDuracao.setToolTipText("Duração em minutos");
        JButton btnAgendar = new JButton("Agendar");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        int row = 0;

        // Título
        JLabel titulo = new JLabel("Agendar Consulta", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        panel.add(titulo, c);

        c.gridwidth = 1;

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("ID Paciente:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtPaciente, c);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("ID Médico:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtMedico, c);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("Início:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtInicio, c);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("Duração (min):"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtDuracao, c);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        panel.add(btnAgendar, c);

        c.gridy++;
        panel.add(btnVoltar, c);

        btnAgendar.addActionListener(e -> {
            try {
                long pacienteId = Long.parseLong(txtPaciente.getText().trim());
                long medicoId = Long.parseLong(txtMedico.getText().trim());
                LocalDateTime inicio = LocalDateTime.parse(txtInicio.getText().trim(), PADRAO);
                int duracaoMin = Integer.parseInt(txtDuracao.getText().trim());
                LocalDateTime fim = inicio.plusMinutes(duracaoMin);

                var consulta = new Consulta(null, pacienteId, medicoId, inicio, fim);
                Long id = service.agendar(consulta);

                JOptionPane.showMessageDialog(panel, "Consulta agendada com sucesso!\nID = " + id,
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // Limpa campos
                txtPaciente.setText("");
                txtMedico.setText("");
                txtInicio.setText("");
                txtDuracao.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnVoltar.addActionListener(e -> ((CardLayout) mainPanel.getLayout()).show(mainPanel, "MENU"));

        return panel;
    }

    private static JPanel criarTelaCadastrarPaciente(JPanel mainPanel, PacienteService service) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        txtNome.setToolTipText("Nome completo do paciente");
        JTextField txtEmail = new JTextField(20);
        txtEmail.setToolTipText("Email válido do paciente");
        JButton btnCadastrar = new JButton("Cadastrar Paciente");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        int row = 0;

        // Título
        JLabel titulo = new JLabel("Cadastrar Paciente", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        panel.add(titulo, c);

        c.gridwidth = 1;

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtNome, c);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("Email:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtEmail, c);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        panel.add(btnCadastrar, c);

        c.gridy++;
        panel.add(btnVoltar, c);

        btnCadastrar.addActionListener(e -> {
            try {
                String nome = txtNome.getText().trim();
                String email = txtEmail.getText().trim();

                Long id = service.cadastrar(nome, email);

                JOptionPane.showMessageDialog(panel, "Paciente cadastrado com sucesso!\nID = " + id,
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // Limpa campos
                txtNome.setText("");
                txtEmail.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnVoltar.addActionListener(e -> ((CardLayout) mainPanel.getLayout()).show(mainPanel, "MENU"));

        return panel;
    }

    private static JPanel criarTelaCadastrarMedico(JPanel mainPanel, MedicoService service) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtNome = new JTextField(20);
        txtNome.setToolTipText("Nome completo do médico");
        JTextField txtCrm = new JTextField(20);
        txtCrm.setToolTipText("CRM no formato CRM12345");
        JButton btnCadastrar = new JButton("Cadastrar Médico");
        JButton btnVoltar = new JButton("Voltar ao Menu");

        int row = 0;

        // Título
        JLabel titulo = new JLabel("Cadastrar Médico", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        c.gridx = 0; c.gridy = row++; c.gridwidth = 2;
        panel.add(titulo, c);

        c.gridwidth = 1;

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("Nome:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtNome, c);

        c.gridx = 0; c.gridy = row; c.weightx = 0;
        panel.add(new JLabel("CRM:"), c);
        c.gridx = 1; c.gridy = row++; c.weightx = 1.0;
        panel.add(txtCrm, c);

        c.gridx = 0; c.gridy = row; c.gridwidth = 2;
        panel.add(btnCadastrar, c);

        c.gridy++;
        panel.add(btnVoltar, c);

        btnCadastrar.addActionListener(e -> {
            try {
                String nome = txtNome.getText().trim();
                String crm = txtCrm.getText().trim();

                Long id = service.cadastrar(nome, crm);

                JOptionPane.showMessageDialog(panel, "Médico cadastrado com sucesso!\nID = " + id,
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                // Limpa campos
                txtNome.setText("");
                txtCrm.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnVoltar.addActionListener(e -> ((CardLayout) mainPanel.getLayout()).show(mainPanel, "MENU"));

        return panel;
    }
}