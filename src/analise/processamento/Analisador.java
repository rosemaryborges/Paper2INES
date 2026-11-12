
package analise.processamento;

import analise.aprendizagem.*;
import analise.contexto.*;

import java.util.*;
import java.util.regex.Pattern;

public class Analisador {

    private final Map<String, Aluno> alunos = new LinkedHashMap<>();
    private final Map<String, Questao> questoes = new LinkedHashMap<>();

    private final LeitorTestOutcome leitorResultado =
            new LeitorTestOutcome();

    private final List<String[]> auditoria = new ArrayList<>();

    private final Pattern excecao = Pattern.compile(
        "(?i)(exception in thread|arithmeticexception|" +
        "inputmismatchexception|nullpointerexception|" +
        "arrayindexoutofboundsexception|numberformatexception|" +
        "stackoverflowerror|outofmemoryerror|time limit|timeout)"
    );

    // ---------------------------------------------------------
    // 1. CRIAR SUBMISSOES
    // ---------------------------------------------------------

    public List<Submissao> criarSubmissoes(
            List<Map<String, String>> linhas) {

        List<Submissao> lista = new ArrayList<>();
        auditoria.clear();
        alunos.clear();
        questoes.clear();
        Set<String> passosVistos = new HashSet<>();

        for (Map<String, String> linha : linhas) {

            String motivo = verificar(linha);

            if (!motivo.equals("INCLUIDO")) {
                auditoria.add(new String[]{
                    texto(linha, "step_id"), motivo
                });
                continue;
            }

            try {
                AvaliacaoCodeRunner avaliacao =
                    leitorResultado.ler(
                        texto(linha, "testoutcome")
                    );

                if (avaliacao == null ||
                        !avaliacao.avaliacaoDoEstudanteValida()) {

                    auditoria.add(new String[]{
                        texto(linha, "step_id"),
                        "AVALIACAO_NAO_UTILIZAVEL"
                    });
                    continue;
                }

                String codigo = textoOriginal(linha, "answer");

                if (codigo.isBlank()) {
                    codigo = textoOriginal(linha, "submittedcode");
                }

                int turma = inteiro(linha, "turma");
                int aluno = inteiro(linha, "aluno_id");

                int original =
                    inteiro(linha, "question_id_original");

                int questao =
                    inteiro(linha, "question_id");


                // NOVO: classificacao completa do erro
                Erro erro = classificar(avaliacao);

                Submissao submissao = new Submissao(
                    turma,
                    aluno,
                    questao,
                    original,
                    inteiro(linha, "step_id"),
                    longo(linha, "unix_time"),
                    codigo,
                    texto(linha, "step_state"),
                    numero(linha, "fraction"),
                    avaliacao,
                    erro,
                    inteiro(linha, "question_attempt_id"),
                    inteiro(linha, "quiz_attempt_id"),
                    inteiro(linha, "quiz_id"),
                    inteiro(linha, "sequence_number"),
                    texto(linha, "questao_analitica"),
                    texto(linha, "tipo_atividade"),
                    inteiro(linha, "unidade")
                );

                String chavePasso = turma + ":" + aluno + ":" + submissao.getIdPasso();
                if (!passosVistos.add(chavePasso)) {
                    auditoria.add(new String[]{texto(linha,"step_id"), "PASSO_DUPLICADO"});
                    continue;
                }
                lista.add(submissao);

                alunos.putIfAbsent(
                    turma + ":" + aluno,
                    new Aluno(aluno, turma)
                );

                String chaveQuestao = turma + ":" + questao + ":" + inteiro(linha, "quiz_id");
                questoes.putIfAbsent(chaveQuestao,
                    new Questao(questao, texto(linha, "question_name"),
                        (texto(linha, "unidade").isBlank() ? identificarUnidade(texto(linha, "question_name")) : inteiro(linha, "unidade")),
                        "java_program", inteiro(linha, "quiz_id"),
                        texto(linha, "questao_analitica"), texto(linha, "tipo_atividade")));

            } catch (RuntimeException e) {

                auditoria.add(new String[]{
                    texto(linha, "step_id"),
                    "ERRO_LEITURA_TESTOUTCOME"
                });
            }
        }

        lista.sort(
            Comparator.comparingInt(Submissao::getTurma)
                .thenComparingInt(Submissao::getAlunoId)
                .thenComparingInt(Submissao::getQuizId)
                .thenComparingInt(Submissao::getQuestionAttemptId)
                .thenComparingInt(Submissao::getQuestaoId)
                .thenComparingLong(Submissao::getMomento)
                .thenComparingInt(Submissao::getIdPasso)
        );

        return lista;
    }

    // ---------------------------------------------------------
    // 2. VERIFICAR SE O REGISTO DEVE SER INCLUIDO
    // ---------------------------------------------------------

    private String verificar(Map<String, String> linha) {

        if (!texto(linha, "coderunner_type")
                .equalsIgnoreCase("java_program")) {
            return "OUTRO_TIPO";
        }

        String estado = texto(linha, "step_state");

        if (!estado.equals("todo") &&
                !estado.equals("complete")) {
            return "PASSO_ADMINISTRATIVO";
        }

        if (textoOriginal(linha, "answer").isBlank() &&
                textoOriginal(linha, "submittedcode").isBlank()) {
            return "SEM_CODIGO";
        }

        if (texto(linha, "testoutcome").isBlank()) {
            return "SEM_TESTOUTCOME";
        }

        return "INCLUIDO";
    }

    // ---------------------------------------------------------
    // 3. CLASSIFICAR A SUBMISSAO
    // ---------------------------------------------------------

    private Erro classificar(AvaliacaoCodeRunner avaliacao) {

        String mensagem = seguro(
            avaliacao.getMensagemErro()
        );

        StatusCodeRunner status = avaliacao.getStatus();

        // 3.1 - ERRO DE COMPILACAO
        if (status == StatusCodeRunner.ERRO_SINTAXE) {

            String subcategoria =
                identificarErroCompilacao(mensagem);

            return new Erro(
                "COMPILACAO",
                subcategoria,
                mensagem
            );
        }

        // 3.2 - OUTROS ESTADOS DO CODERUNNER
        if (status != StatusCodeRunner.VALIDO) {

            return new Erro(
                "SISTEMA_INDETERMINADO",
                status.name(),
                mensagem
            );
        }

        // 3.3 - PROGRAMA CORRETO
        if (avaliacao.foiAceite()) {

            return new Erro(
                "CORRETO",
                "NAO_SE_APLICA",
                ""
            );
        }

        // 3.4 - ERRO DE EXECUCAO
        String evidencia = procurarExcecao(mensagem);

        if (evidencia.isEmpty()) {

            for (ResultadoTeste teste :
                    avaliacao.getTestes()) {

                evidencia = procurarExcecao(
                    teste.getStderr()
                );

                if (evidencia.isEmpty()) {
                    evidencia = procurarExcecao(
                        teste.getObtido()
                    );
                }

                if (!evidencia.isEmpty()) {
                    break;
                }
            }
        }

        if (!evidencia.isEmpty()) {

            return new Erro(
                "EXECUCAO",
                identificarErroExecucao(evidencia),
                evidencia
            );
        }

        // 3.5 - FALHA NOS TESTES
        for (ResultadoTeste teste :
                avaliacao.getTestes()) {

            if (teste.isCorreto()) {
                continue;
            }

            String esperado = seguro(
                teste.getEsperado()
            );

            String obtido = seguro(
                teste.getObtido()
            );

            // Os textos diferem apenas no espacamento?
            if (!esperado.isEmpty() &&
                    !esperado.equals(obtido) &&
                    removerEspacos(esperado)
                        .equals(removerEspacos(obtido))) {

                return new Erro(
                    "FALHA_TESTES",
                    "DIFERENCA_ESPACAMENTO",
                    evidenciaTeste(teste)
                );
            }
        }

        // 3.6 - FALHA AINDA NAO CLASSIFICADA
        return new Erro(
            "FALHA_TESTES",
            "NAO_CLASSIFICADO",
            evidenciaPrimeiroTesteFalhado(avaliacao)
        );
    }

    // ---------------------------------------------------------
    // 4. SUBCATEGORIAS DE COMPILACAO
    // ---------------------------------------------------------

    private String identificarErroCompilacao(
            String mensagem) {

        String texto = mensagem.toLowerCase(Locale.ROOT);

        if (texto.contains("cannot find symbol")) {
            return "SIMBOLO_NAO_ENCONTRADO";
        }

        if (texto.contains("';' expected")) {
            return "PONTO_E_VIRGULA";
        }

        if (texto.contains(
                "reached end of file while parsing")) {
            return "FIM_ARQUIVO_INESPERADO";
        }

        if (texto.contains("unclosed string literal")) {
            return "STRING_NAO_FECHADA";
        }

        if (texto.contains("illegal start of expression")) {
            return "EXPRESSAO_INVALIDA";
        }

        if (texto.contains("incompatible types")) {
            return "TIPOS_INCOMPATIVEIS";
        }

        return "NAO_CLASSIFICADO";
    }

    // ---------------------------------------------------------
    // 5. SUBCATEGORIAS DE EXECUCAO
    // ---------------------------------------------------------

    private String identificarErroExecucao(
            String mensagem) {

        if (mensagem.contains("ArithmeticException") &&
                mensagem.contains("/ by zero")) {
            return "DIVISAO_POR_ZERO";
        }

        if (mensagem.contains("InputMismatchException")) {
            return "ENTRADA_INVALIDA";
        }

        if (mensagem.contains(
                "ArrayIndexOutOfBoundsException")) {
            return "INDICE_INVALIDO";
        }

        if (mensagem.contains("NullPointerException")) {
            return "REFERENCIA_NULA";
        }

        if (mensagem.contains("NumberFormatException")) {
            return "CONVERSAO_NUMERICA";
        }

        if (mensagem.toLowerCase(Locale.ROOT)
                .contains("time limit") ||
            mensagem.toLowerCase(Locale.ROOT)
                .contains("timeout")) {
            return "TEMPO_EXCEDIDO";
        }

        return "OUTRA_EXCECAO";
    }

    // ---------------------------------------------------------
    // 6. PROCURAR EXCECOES NA SAIDA
    // ---------------------------------------------------------

    private String procurarExcecao(String texto) {

        String mensagem = seguro(texto);

        if (excecao.matcher(mensagem).find()) {
            return mensagem;
        }

        Pattern padrao = Pattern.compile(
            "(?:[\\w$]+\\.)*[\\w$]+(?:Exception|Error)\\b"
        );

        if (padrao.matcher(mensagem).find()) {
            return mensagem;
        }

        return "";
    }

    // ---------------------------------------------------------
    // 7. GUARDAR EVIDENCIAS DE FALHA NOS TESTES
    // ---------------------------------------------------------

    private String evidenciaPrimeiroTesteFalhado(
            AvaliacaoCodeRunner avaliacao) {

        for (ResultadoTeste teste :
                avaliacao.getTestes()) {

            if (!teste.isCorreto()) {
                return evidenciaTeste(teste);
            }
        }

        return seguro(avaliacao.getMensagemErro());
    }

    private String evidenciaTeste(ResultadoTeste teste) {

        return "Teste: " + teste.getNome()
            + " | Entrada: " + teste.getEntrada()
            + " | Esperado: " + teste.getEsperado()
            + " | Obtido: " + teste.getObtido();
    }

    private String removerEspacos(String texto) {
        return texto.replaceAll("\\s+", "");
    }

    private String seguro(String texto) {
        return texto == null ? "" : texto;
    }

    // ---------------------------------------------------------
    // 8. CRIAR TRAJETORIAS DOS ESTUDANTES
    // ---------------------------------------------------------

    public List<Trajetoria> criarTrajetorias(
            List<Submissao> submissoes) {
        for (Aluno aluno : alunos.values()) aluno.getTrajetorias().clear();

        Map<String, List<Submissao>> grupos =
            new LinkedHashMap<>();

        for (Submissao submissao : submissoes) {

            String chave =
                submissao.getTurma() + ":" +
                submissao.getAlunoId() + ":" +
                submissao.getQuizId() + ":" +
                submissao.getQuestaoId() + ":" +
                submissao.getQuestionAttemptId();

            grupos.computeIfAbsent(
                chave,
                k -> new ArrayList<>()
            ).add(submissao);
        }

        List<Trajetoria> trajetorias =
            new ArrayList<>();

        for (List<Submissao> grupo : grupos.values()) {
            grupo.sort(Comparator.comparingLong(Submissao::getMomento)
                .thenComparingInt(Submissao::getSequenceNumber)
                .thenComparingInt(Submissao::getIdPasso));

            Submissao primeiro = grupo.get(0);

            Aluno aluno = alunos.get(
                primeiro.getTurma() + ":" +
                primeiro.getAlunoId()
            );

            Questao questao = questoes.get(
                primeiro.getTurma() + ":" + primeiro.getQuestaoId() + ":" + primeiro.getQuizId()
            );

            Trajetoria trajetoria =
                new Trajetoria(aluno, questao);

            boolean iniciou = false;

            for (Submissao submissao : grupo) {

                if (!iniciou && !submissao.estaCorreta()) {
                    iniciou = true;
                }

                if (iniciou) {

                    trajetoria.adicionarSubmissao(
                        submissao
                    );

                    // Parar na primeira correcao
                    if (submissao.estaCorreta()) {
                        break;
                    }
                }
            }

            if (iniciou) {

                trajetorias.add(trajetoria);

                aluno.adicionarTrajetoria(
                    trajetoria
                );
            }
        }

        return trajetorias;
    }

    // ---------------------------------------------------------
    // 9. METODOS AUXILIARES
    // ---------------------------------------------------------

    // -1 indica que a unidade nao pode ser inferida de forma segura.
    private static int identificarUnidade(String nome) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "(?i)(?:unidade|unid|uni|\\bu)\\s*[_\\-\\s]*0*([1-9][0-9]?)"
        ).matcher(nome == null ? "" : nome);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    public List<String[]> getAuditoria() {
        return auditoria;
    }

    // Preserve exact source code, including indentation, trailing spaces and line endings.
    private String textoOriginal(Map<String, String> linha, String campo) {
        return linha.getOrDefault(campo, "");
    }

    private String texto(
            Map<String, String> linha,
            String campo) {

        String valor = linha.get(campo);
        return valor == null ? "" : valor.trim();
    }

    private int inteiro(
            Map<String, String> linha,
            String campo) {

        return (int) longo(linha, campo);
    }

    private long longo(
            Map<String, String> linha,
            String campo) {

        try {
            return Long.parseLong(texto(linha, campo));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double numero(
            Map<String, String> linha,
            String campo) {

        try {
            return Double.parseDouble(texto(linha, campo));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
