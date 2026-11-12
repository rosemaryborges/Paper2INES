package analise.processamento;

import analise.aprendizagem.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/** Q2: uma linha por par consecutivo; colunas de apoio para Q1, Q3 e Q4. */
public final class AnaliseRQ2 {
    private AnaliseRQ2() {}
    public static void executar(List<Trajetoria> trajetorias, Path pasta) throws IOException {
        List<String[]> linhas = new ArrayList<>();
        linhas.add(CSV.cabecalho("trajectory_id","turma","student_id","unit","question_id","quiz_id",
            "question_attempt_id","first_step_id","revision_number","step_before","step_after",
            "previous_error_category","error_category","error_transition","error_change",
            "revision_interval","score_before","score_after","score_delta","revision_outcome",
            "tests_comparable","resolved_tests","persistent_failed_tests","new_failed_tests",
            "code_change_type","code_change_magnitude","requires_manual_review","code_before","code_after"));
        Map<String,Integer> matriz = new TreeMap<>();
        for(Trajetoria t:trajetorias) {
            int numero=0;
            for(Revisao r:t.getRevisoes()) {
                numero++;
                String anterior=r.getAnterior().getErro().getCategoria(), atual=r.getAtual().getErro().getCategoria();
                String transicao=anterior+" -> "+atual;
                String mudanca="CORRETO".equals(atual)?"ERRO_RESOLVIDO":anterior.equals(atual)?"TIPO_MANTIDO":"TIPO_ALTERADO";
                boolean comparavel=r.testesComparaveis();
                linhas.add(Indicadores.juntar(Indicadores.prefixo(t),new String[]{""+numero,
                    ""+r.getAnterior().getIdPasso(),""+r.getAtual().getIdPasso(),
                    anterior,atual,transicao,mudanca,CSV.numero(r.getIntervaloMinutos()),
                    CSV.numero(r.getAnterior().getPontuacao()),CSV.numero(r.getAtual().getPontuacao()),
                    CSV.numero(r.getDiferencaPontuacao()),r.getResultadoRevisao(),""+comparavel,
                    comparavel?""+r.contar(Revisao.MudancaTeste.ERRO_RESOLVIDO):"",
                    comparavel?""+r.contar(Revisao.MudancaTeste.ERRO_PERSISTENTE):"",
                    comparavel?""+r.contar(Revisao.MudancaTeste.NOVO_ERRO):"",
                    r.getTipoRevisao(),CSV.numero(r.getMagnitudeRevisao()),
                    ""+(!comparavel || r.requerRevisaoManual()),
                    r.getAnterior().getCodigo(),r.getAtual().getCodigo()}));
                matriz.merge(transicao,1,Integer::sum);
            }
        }
        CSV.escrever(pasta.resolve("revisoes.csv"),linhas);
        System.out.println("\nQ2 | Transicoes entre categorias (n pares)");
        matriz.forEach((transicao,n)->System.out.println("  "+transicao+": "+n));
    }
}
