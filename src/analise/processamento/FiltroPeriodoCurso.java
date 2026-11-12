package analise.processamento;


import java.time.LocalDate;
import java.util.*;

public class FiltroPeriodoCurso {

    // Datas oficiais de encerramento
    private static final Map<Integer, LocalDate> ENCERRAMENTOS =
            Map.of(
                3, LocalDate.of(2026, 4, 13),
                5, LocalDate.of(2026, 6, 10)
            );

    private final List<Map<String, String>> excluidos =
            new ArrayList<>();

    public List<Map<String, String>> filtrar(
            List<Map<String, String>> linhas) {

        excluidos.clear();

        List<Map<String, String>> incluidos =
                new ArrayList<>();

        for (Map<String, String> linha : linhas) {

            int turma = Integer.parseInt(
                linha.get("turma").trim()
            );

            LocalDate encerramento =
                    ENCERRAMENTOS.get(turma);

            if (encerramento == null) {
                throw new IllegalArgumentException(
                    "Turma sem data de encerramento: " + turma
                );
            }

            // submission_time: 2026-04-12 18:30:00
            String dataHora = linha.get("submission_time");

            if (dataHora == null ||
                    dataHora.length() < 10) {

                throw new IllegalArgumentException(
                    "Data invalida no step_id "
                    + linha.get("step_id")
                );
            }

            LocalDate dataSubmissao =
                    LocalDate.parse(
                        dataHora.substring(0, 10)
                    );

            // Incluir ate ao ultimo dia oficial
            if (!dataSubmissao.isAfter(encerramento)) {

                incluidos.add(linha);

            } else {

                excluidos.add(linha);
            }
        }

        return incluidos;
    }

    public List<Map<String, String>> getExcluidos() {
        return Collections.unmodifiableList(excluidos);
    }

    public LocalDate getEncerramento(int turma) {
        return ENCERRAMENTOS.get(turma);
    }
}
