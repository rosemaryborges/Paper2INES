package analise.processamento;

import analise.aprendizagem.*;
import java.nio.file.Path;
import java.util.*;

// java -cp build analise.processamento.Main dados/coderunner_us.csv resultados
public final class Main {
    private Main() {}
    public static void main(String[] args) throws Exception {
        String entrada = args.length > 0 ? args[0] : "dados/coderunner_us.csv";
        Path pasta = Path.of(args.length > 1 ? args[1] : "resultados");
        List<Map<String,String>> originais = new LeitorCSV().ler(entrada);
        FiltroPeriodoCurso filtro = new FiltroPeriodoCurso();
        List<Map<String,String>> noPeriodo = filtro.filtrar(originais);
        Analisador analisador = new Analisador();
        List<Submissao> submissoes = analisador.criarSubmissoes(noPeriodo);
        List<Trajetoria> trajetorias = Indicadores.selecionarUnidades(analisador.criarTrajetorias(submissoes));

        System.out.printf("Registos originais: %d | no periodo: %d | excluidos por data: %d%n",
            originais.size(), noPeriodo.size(), filtro.getExcluidos().size());
        System.out.printf("Submissoes Java Program utilizaveis: %d | trajetorias U2/U4/U5: %d%n",
            submissoes.size(), trajetorias.size());
        System.out.printf("Registos rejeitados na leitura/avaliacao: %d%n", analisador.getAuditoria().size());

        AnaliseRQ1.executar(trajetorias, pasta);
        AnaliseRQ2.executar(trajetorias, pasta);
        AnaliseRQ3.executar(trajetorias);
        AnaliseRQ4.executar(trajetorias);
        System.out.println("Ficheiros gerados: " + pasta.resolve("trajetorias.csv") + " e " + pasta.resolve("revisoes.csv"));
    }
}
