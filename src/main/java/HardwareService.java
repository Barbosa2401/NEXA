import oshi.SystemInfo;
import oshi.hardware.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class HardwareService {

    private SystemInfo si = new SystemInfo();

    public String capturarDados() {
        var hardware = si.getHardware();
        StringBuilder relatorioConsole = new StringBuilder();
        
        // 1. Hostname e Informações do Sistema
        String hostname = si.getOperatingSystem().getNetworkParams().getHostName();
        ComputerSystem system = hardware.getComputerSystem();
        String fabricante = system.getManufacturer();
        String modeloMaquina = system.getModel();
        
        relatorioConsole.append("\n=== INICIANDO COLETA DE HARDWARE ===\n");
        
        // 2. Discos (Loop para listar todos individualmente no console)
        List<HWDiskStore> discos = hardware.getDiskStores();
        StringBuilder dadosDiscosCSV = new StringBuilder();
        
        for (HWDiskStore disk : discos) {
            long tamanhoGB = disk.getSize() / (1024 * 1024 * 1024);
            String statusSaude = verificarSaudeWmi();
            
            relatorioConsole.append(String.format("Disco: %s | Tamanho: %dGB | Saúde: %s\n", 
                    disk.getModel(), tamanhoGB, statusSaude));
            
            // Prepara string para o CSV (concatena todos os discos em uma célula)
            dadosDiscosCSV.append(disk.getModel()).append(" (").append(tamanhoGB).append("GB) ");
        }

        // 3. Hostname, Máquina e CPU
        CentralProcessor processor = hardware.getProcessor();
        String nomeCPU = processor.getProcessorIdentifier().getName();
        
        relatorioConsole.append("Hostname: ").append(hostname).append("\n");
        relatorioConsole.append("Máquina: ").append(fabricante).append(" ").append(modeloMaquina).append("\n");
        relatorioConsole.append("CPU: ").append(nomeCPU).append("\n");

        // Captura o Serial Number (Service Tag)
        String serialNumber = system.getSerialNumber();

        // Captura a data atual
        String dataColeta = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        // 4. Memória RAM (Total e Detalhes)
        GlobalMemory memory = hardware.getMemory();
        long totalRamGB = memory.getTotal() / (1024 * 1024 * 1024);
        String detalhesRam = "N/A";
        
        if (!memory.getPhysicalMemory().isEmpty()) {
            PhysicalMemory pm = memory.getPhysicalMemory().get(0);
            detalhesRam = String.format("Tipo: %s | Frequência: %.2f MHz", 
                    pm.getMemoryType(), (pm.getClockSpeed() / 1000000.0));
        }
        relatorioConsole.append("RAM Total: ").append(totalRamGB).append("GB ").append(detalhesRam).append("\n");
        relatorioConsole.append("=== COLETA FINALIZADA ===\n");

        // Exibe o relatório detalhado no terminal como solicitado
        System.out.println(relatorioConsole.toString());

        // Atualize o return para incluir esses dois novos campos
        return String.format("%s;%s %s;%s;%s;%dGB %s;%s;%s", 
        hostname, fabricante, modeloMaquina, serialNumber, nomeCPU, totalRamGB, detalhesRam, dadosDiscosCSV.toString().trim(), dataColeta);
    }

    // Método auxiliar para tentar ler o status básico do disco via WMIC
    private String verificarSaudeWmi() {
        try {
            Process process = Runtime.getRuntime().exec("wmic diskdrive get status");
            Scanner sc = new Scanner(process.getInputStream());
            while (sc.hasNext()) {
                String line = sc.next();
                if (line.equalsIgnoreCase("OK")) return "Saudável";
                if (line.equalsIgnoreCase("PredFail")) return "Atenção (Falha Iminente)";
            }
        } catch (Exception e) {
            return "Erro Coleta";
        }
        return "N/A";
    }

    public void salvarRelatorio(String cliente, String linhaCSV) {
        try {
            // Cria pasta baseada no nome do cliente
            String nomePasta = "Relatorios_" + cliente.replaceAll("[^a-zA-Z0-9]", "_");
            Files.createDirectories(Paths.get(nomePasta));

            File arquivo = new File(nomePasta + "/inventario.csv");
            boolean arquivoNovo = !arquivo.exists();

            try (FileWriter fw = new FileWriter(arquivo, true)) {
                if (arquivoNovo) {
                    // Cabeçalho do Excel/CSV
                    fw.write("Hostname;Fabricante_Modelo;Serial_Number;Processador;Memoria_RAM;Discos_Detectados;Data_Coleta\n");
                }
                fw.write(linhaCSV + "\n");
            }
            System.out.println("💾 Dados salvos em: " + arquivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Erro ao gravar arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Deixa a janela com a cara do Windows
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
            HardwareService service = new HardwareService();

            // Box de identificação do cliente
            String nomeCliente = JOptionPane.showInputDialog(null, 
                "Digite o nome do Cliente para iniciar o inventário:", 
                "NEXA FIELD SYSTEM", 
                JOptionPane.QUESTION_MESSAGE);

            if (nomeCliente == null || nomeCliente.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Operação cancelada.");
                return;
            }

            // Coleta e Salva os dados
            String dadosFormatados = service.capturarDados();
            service.salvarRelatorio(nomeCliente, dadosFormatados);

            // Pergunta sobre o Dashboard
            int resposta = JOptionPane.showConfirmDialog(null, 
                "Coleta realizada com sucesso!\n\nDeseja abrir o Dashboard agora?", 
                "NEXA FIELD SYSTEM", 
                JOptionPane.YES_NO_OPTION);

            if (resposta == JOptionPane.YES_OPTION) {
            try {
                // Pega o arquivo index.html na mesma pasta onde o .exe está rodando
                File htmlFile = new File("a.html").getAbsoluteFile();
                // USE EXATAMENTE ASSIM:
                if (htmlFile.exists()) {
                    java.awt.Desktop.getDesktop().browse(htmlFile.toURI());
                }
        
                if (htmlFile.exists()) {
                    // Usa a URI correta para evitar o erro de "file:/G://"
                    java.awt.Desktop.getDesktop().browse(htmlFile.toURI());
                } else {
                    JOptionPane.showMessageDialog(null, "Arquivo index.html não encontrado em: " + htmlFile.getAbsolutePath());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao abrir o navegador: " + e.getMessage());
            }
        }

            // Alerta final e encerramento
            JOptionPane.showMessageDialog(null, "Inventário concluído para: " + nomeCliente);
            System.exit(0);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro crítico: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    } // FIM DO MAIN
} // FIM DA CLASSE (Certifique-se de que esta chave existe!)
