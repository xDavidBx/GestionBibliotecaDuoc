package modelo;

import java.time.LocalDate;

public record Prestamo(int id, int idEstudiante, String estudiante, int idLibro, String libro,
                       LocalDate fechaPrestamo, LocalDate fechaDevolucion,
                       LocalDate fechaRetorno, boolean devuelto) {
    public boolean estaAtrasado() {
        return !devuelto && fechaDevolucion.isBefore(LocalDate.now());
    }
}