package com.example.clinic.ui.swing;

import com.example.clinic.dao.jdbc.*;
import com.example.clinic.domain.Consulta;
import com.example.clinic.service.*;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SwingMain {

    private static final DateTimeFormatter PADRAO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

    public static void main(String[] args) {
        runSwing();
    }

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
