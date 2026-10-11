package controladores;

import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import java.util.List;

public interface BibliotecaActions {
    void recargar();
    void guardarLibro(Libro libro, boolean nuevo);
    void eliminarLibro(int id);
    void guardarEstudiante(Estudiante estudiante, String rutAnterior, String claveInicial, boolean nuevo);
    void eliminarEstudiante(Estudiante estudiante);
    void solicitarPrestamo(int idEstudiante, int idLibro);
    void registrarDevolucion(int idPrestamo, int idEstudiante);
    void generarReporte(int tipo);
    void crearCategoria(String nombre);
    void actualizarCategoria(int id, String nombre);
    void eliminarCategoria(int id);
    void mostrarMensaje(String mensaje, boolean error);
    void datosCargados(List<Libro> libros, List<Estudiante> estudiantes, List<Categoria> categorias,
                       List<Prestamo> prestamosActivos);
    void mostrarReporte(String reporte);
}