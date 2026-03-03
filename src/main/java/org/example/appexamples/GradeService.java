package org.example.appexamples;

import org.example.utilities.HttpServer;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.example.utilities.HttpServer.get;
import static org.example.utilities.HttpServer.staticfiles;

/**
 * GradeService — example application built on MicroFramework Web.
 *
 * Endpoints:
 *   GET /calificar?nota=4.5          → describes a single grade (0.0–5.0 scale)
 *   GET /promedio?notas=3.5,4.0,4.8  → computes the average of several grades
 *   GET /aprobo?nota=2.8             → pass/fail check (threshold: 3.0)
 */
public class GradeService {

    public static void main(String[] args) throws IOException, URISyntaxException {

        staticfiles("/webroot/grades");

        // ── /calificar?nota=X ──────────────────────────────────────────────
        get("/calificar", (req, res) -> {
            String raw = req.getValues("nota");
            if (raw.isEmpty()) return "Falta el parámetro: <code>?nota=</code>";
            try {
                double nota = Double.parseDouble(raw);
                return "Nota: <strong>" + nota + "</strong> → " + describir(nota);
            } catch (NumberFormatException e) {
                return "Valor inválido: <code>" + raw + "</code>";
            }
        });

        // ── /promedio?notas=X,Y,Z ─────────────────────────────────────────
        get("/promedio", (req, res) -> {
            String raw = req.getValues("notas");
            if (raw.isEmpty()) return "Falta el parámetro: <code>?notas=</code> (valores separados por coma)";

            String[] parts = raw.split(",");
            double sum = 0;
            int count = 0;
            StringBuilder list = new StringBuilder();

            for (String part : parts) {
                try {
                    double n = Double.parseDouble(part.trim());
                    sum += n;
                    count++;
                    list.append(n).append("  ");
                } catch (NumberFormatException ignored) { }
            }

            if (count == 0) return "No se encontraron notas válidas.";

            double avg = Math.round((sum / count) * 100.0) / 100.0;
            return "Notas: <strong>" + list.toString().trim() + "</strong><br>"
                 + "Promedio: <strong>" + avg + "</strong> → " + describir(avg);
        });

        // ── /aprobo?nota=X ─────────────────────────────────────────────────
        get("/aprobo", (req, res) -> {
            String raw = req.getValues("nota");
            if (raw.isEmpty()) return "Falta el parámetro: <code>?nota=</code>";
            try {
                double nota = Double.parseDouble(raw);
                if (nota < 0 || nota > 5)
                    return "Nota fuera de rango (0.0 – 5.0): <code>" + nota + "</code>";

                boolean paso = nota >= 3.0;
                double margen = Math.abs(nota - 3.0);
                String detalle = paso
                    ? String.format("(%.2f por encima del mínimo)", margen)
                    : String.format("(%.2f por debajo del mínimo)", margen);

                return "Nota: <strong>" + nota + "</strong> → "
                     + (paso ? "✔ APROBÓ " : "✘ REPROBÓ ") + detalle;
            } catch (NumberFormatException e) {
                return "Valor inválido: <code>" + raw + "</code>";
            }
        });

        HttpServer.main(args);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private static String describir(double nota) {
        if (nota < 0 || nota > 5) return "fuera de rango (0.0 – 5.0)";
        if (nota >= 4.5) return "Excelente";
        if (nota >= 4.0) return "Sobresaliente";
        if (nota >= 3.5) return "Bueno";
        if (nota >= 3.0) return "Aceptable (aprobado)";
        return "Reprobado";
    }
}
