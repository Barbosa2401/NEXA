import oshi.SystemInfo;
import oshi.hardware.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.List;

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

        // Retorna a linha formatada para o CSV (Hostname;Maquina;CPU;RAM;Discos)
        return String.format("%s;%s %s;%s;%dGB %s;%s", 
                hostname, fabricante, modeloMaquina, nomeCPU, totalRamGB, detalhesRam, dadosDiscosCSV.toString().trim());
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
                    fw.write("Hostname;Fabricante_Modelo;Processador;Memoria_RAM;Discos_Detectados\n");
                }
                fw.write(linhaCSV + "\n");
            }
            System.out.println("💾 Dados salvos em: " + arquivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Erro ao gravar arquivo: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        HardwareService service = new HardwareService();

        System.out.println("###############################");
        System.out.println("#      NEXA FIELD SYSTEM      #");
        System.out.println("###############################");
        
        System.out.print("Informe o nome do Cliente: ");
        String nomeCliente = scanner.nextLine();

        System.out.println("\nProcessando hardware, aguarde...");
        String dadosFormatados = service.capturarDados();

        service.salvarRelatorio(nomeCliente, dadosFormatados);

        System.out.println("\n[Pressione ENTER para encerrar]");
        scanner.nextLine();
        scanner.close();
    }
}