import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.PhysicalMemory;
import java.util.List;

public class HardwareService {

    private SystemInfo si = new SystemInfo();

    public void capturarDados() {
        var hardware = si.getHardware();
        ComputerSystem system = hardware.getComputerSystem();
        
        // 1. Marca e Modelo
        String marca = system.getManufacturer();
        String modelo = system.getModel();

        // 2. Hostname
        String hostname = si.getOperatingSystem().getNetworkParams().getHostName();

        // 3. Processador
        CentralProcessor processor = hardware.getProcessor();
        String nomeProcessador = processor.getProcessorIdentifier().getName();

        // 4. Memória RAM (Total, Tipo e Frequência)
        GlobalMemory memory = hardware.getMemory();
        long totalRamGB = memory.getTotal() / (1024 * 1024 * 1024);
        List<PhysicalMemory> pmList = memory.getPhysicalMemory();
        
        String infoRam = "";
        for (PhysicalMemory pm : pmList) {
            infoRam = String.format("Tipo: %s | Frequência: %.2f MHz", 
                        pm.getMemoryType(), (pm.getClockSpeed() / 1000000.0));
        }

        // 5. Disco (Tipo, Tamanho e Saúde)
        List<HWDiskStore> discos = hardware.getDiskStores();
        for (HWDiskStore disk : discos) {
            long tamanhoGB = disk.getSize() / (1024 * 1024 * 1024);
            // OSHI tenta identificar se é SSD/NVME pelo nome/modelo
            String modeloDisco = disk.getModel(); 
            
            // Nota: A "Vida Útil" (Health) em % exata depende do fabricante 
            // e do suporte SMART do driver.
            System.out.println("Disco: " + modeloDisco + " | Tamanho: " + tamanhoGB + "GB");
        }

        // Print de teste no console
        System.out.println("Hostname: " + hostname);
        System.out.println("Máquina: " + marca + " " + modelo);
        System.out.println("CPU: " + nomeProcessador);
        System.out.println("RAM Total: " + totalRamGB + "GB " + infoRam);
    }
    // ... (mantenha os imports e o código anterior)

    public static void main(String[] args) {
        HardwareService service = new HardwareService();
        System.out.println("=== INICIANDO COLETA DE HARDWARE ===");
        service.capturarDados();
        System.out.println("=== COLETA FINALIZADA ===");
    }
}
