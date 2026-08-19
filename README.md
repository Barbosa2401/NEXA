NEXA Field System 🚀

NEXA é uma ferramenta de auditoria de hardware e gestão de ativos (Asset Management) de alta performance, desenvolvida para técnicos de TI em operação de campo.
Projetado para ser 100% portátil e autônomo, o NEXA roda diretamente de um pendrive, sem a necessidade de instalar dependências, baixar pacotes ou abrir terminais na máquina do cliente. Ele coleta dados críticos de hardware em segundos, permite a atribuição de usuários e TAGs de patrimônio na hora, e consolida tudo em um Dashboard interativo e relatórios Excel formatados.

✨ Funcionalidades Principais
* Coleta Automatizada: Mapeia Hostname, Modelo, Serial Number (Service Tag), Processador, Memória RAM (quantidade e frequência) e Discos Físicos (tamanho e saúde da partição).
* Atribuição em Campo: Interface nativa para preenchimento imediato do Nome do Colaborador e TAG de Patrimônio da máquina auditada.
* Zero Dependências: Utiliza um motor Java (JRE) embutido. Roda silenciosamente via arquivo .exe, burlando a necessidade de o host ter o Java instalado.
* Operação Offline (Failover): Arquitetura desenhada para campo. Os dados são salvos instantaneamente em arquivos .csv estruturados diretamente no pendrive.
* Dashboard Local Integrado: Uma interface gráfica HTML/CSS moderna que lê os arquivos CSV, fornecendo filtros por data, busca por Hostname/CPU e contagem de máquinas.
* Exportação Corporativa: Geração de planilhas Excel (.xlsx) com formatação profissional (cabeçalhos destacados, bordas e alinhamento automático) a partir do Dashboard.

📂 Estrutura do Pendrive (Produção)
Para o perfeito funcionamento em campo, a raiz do pendrive deve conter estritamente a seguinte estrutura:

/
* jre/                -> Motor Java embutido (Requisito: Java 21)
* img/                -> Recursos de imagem para o Dashboard
* NEXA.exe            -> Executável principal do Agente de Coleta
* index.html          -> Dashboard interativo de visualização
* style.css           -> Estilização do Dashboard
* icon.jpg            -> Ícone de exibição no navegador


🛠️ Tecnologias Utilizadas
Backend (Agente de Coleta):

Java 21

* Oshi (Operating System and Hardware Information): Biblioteca core para extração em baixo nível dos dados de hardware.
* Maven: Gerenciamento de dependências e build.
* Launch4j: Encapsulamento do .jar em executável nativo Windows (.exe) com interface GUI (sem console).
* Frontend (Dashboard):
* HTML5 / CSS3 / Vanilla JavaScript
* PapaParse: Leitura e conversão veloz dos arquivos CSV locais.
* ExcelJS & FileSaver.js: Geração, formatação avançada e download dos relatórios Excel direto no navegador.

🚀 Como Utilizar em Campo

* Insira o pendrive na máquina alvo.
* Dê um clique duplo em NEXA.exe.
* Preencha o Nome do Cliente/Empresa na primeira janela.
* Insira o Nome do Colaborador e a TAG de Patrimônio na segunda janela.
* O sistema fará a varredura em background e salvará os dados em uma pasta segregada por cliente (/Relatorios_Cliente).
* Quando desejar visualizar ou exportar os dados, abra o arquivo index.html e carregue o arquivo .csv recém-criado.
