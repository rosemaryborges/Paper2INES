package analise.processamento;

import analise.aprendizagem.*;
import java.util.*;

public class AuditoriaPeriodo {
	// Auditoria: apenas metadados, sem codigo nem credenciais do CodeRunner.
	public static void exportarForaPeriodo(List<Map<String, String>> excluidos) throws java.io.IOException {
		java.nio.file.Path pasta = java.nio.file.Path.of("resultados");
		java.nio.file.Files.createDirectories(pasta);
		java.nio.file.Path destino = pasta.resolve("submissoes_fora_periodo.csv");
		List<String> linhas = new ArrayList<>();
		linhas.add("turma,aluno_id,quiz_attempt_id,step_id,submission_time,step_state");
		String[] colunas = { "turma", "aluno_id", "quiz_attempt_id", "step_id", "submission_time", "step_state" };
		for (Map<String, String> registo : excluidos) {
			List<String> valores = new ArrayList<>();
			for (String coluna : colunas) {
				String v = registo.getOrDefault(coluna, "");
				valores.add("\"" + (v == null ? "" : v).replace("\"", "\"\"") + "\"");
			}
			linhas.add(String.join(",", valores));
		}
		java.nio.file.Files.write(destino, linhas, java.nio.charset.StandardCharsets.UTF_8);
		System.out.println("Auditoria temporal: " + destino);
	}


}
