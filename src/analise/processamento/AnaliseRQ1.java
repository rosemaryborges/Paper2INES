package analise.processamento;

import analise.aprendizagem.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** Q1: padroes, contagem de revisoes e percurso ate a primeira correcao. */
public final class AnaliseRQ1 {
    private AnaliseRQ1() {}
    public static void executar(List<Trajetoria> trajetorias, Path pasta) throws IOException {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(CSV.cabecalho("trajectory_id","turma","student_id","unit","question_id","quiz_id",
            "question_attempt_id","first_step_id","trajectory_pattern","revision_count",
            "revisions_to_success","trajectory_description","initial_error_category",
            "eventual_success","early_revision_behavior","success_after_first_revision",
            "eligible_for_later_success_analysis","recovery_case_pattern","time_to_success"));
        Map<String,Integer> porPadrao = new TreeMap<>();
        for (Trajetoria t:trajetorias) {
            StringBuilder descricao = new StringBuilder();
            for (Submissao s:t.getSubmissoes()) {
                if(!descricao.isEmpty()) descricao.append(" -> ");
                descricao.append(s.getErro().getCategoria());
            }
            int ate = Indicadores.revisoesAteSucesso(t);
            String comportamento = Indicadores.comportamentoInicial(t);
            boolean posterior = t.conseguiuCorrigir() && t.contarRevisoes()>1;
            boolean elegivel = t.contarRevisoes()>0 && !"CORRECAO_IMEDIATA".equals(comportamento)
                && !"INDETERMINADO".equals(comportamento);
            linhas.add(Indicadores.juntar(Indicadores.prefixo(t),new String[]{
                t.getPadrao(),""+t.contarRevisoes(),ate<0?"":""+ate,descricao.toString(),
                t.getErroInicialCategoria(),""+t.conseguiuCorrigir(),comportamento,""+posterior,
                ""+elegivel,t.getPadrao(),CSV.numero(t.tempoAteCorrecao())}));
            porPadrao.merge(t.getPadrao(),1,Integer::sum);
        }
        CSV.escrever(pasta.resolve("trajetorias.csv"),linhas);
        System.out.println("\nQ1 | Padroes (n trajetorias)");
        porPadrao.forEach((padrao,n) -> System.out.println("  "+padrao+": "+n));
        System.out.println("Q1 | Revisoes totais: " + trajetorias.stream().mapToInt(Trajetoria::contarRevisoes).sum());
        System.out.println("Q1 | Corrigidas: " + trajetorias.stream().filter(Trajetoria::conseguiuCorrigir).count());
    }
}
