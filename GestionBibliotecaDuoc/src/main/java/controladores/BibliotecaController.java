package controladores;

import DAO.CategoriaDAO;
import DAO.EstudianteDAO;
import DAO.LibroDAO;
import DAO.PrestamoDAO;
import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;
import servicios.PrestamoService;
import UI.BibliotecaFrame;

import javax.swing.SwingWorker;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class BibliotecaController implements BibliotecaActions {
    private final Usuario usuario;
    private final BibliotecaFrame view;
    private final LibroDAO libros;
    private final EstudianteDAO estudiantes;
    private final CategoriaDAO categorias;
    private final PrestamoDAO prestamos;
    private final PrestamoService prestamoService;

    public BibliotecaController(Usuario usuario, BibliotecaFrame view, LibroDAO libros,
                                EstudianteDAO estudiantes, CategoriaDAO categorias, PrestamoDAO prestamos) {
        this.usuario = usuario;
        this.view = view;
        this.libros = libros;
        this.estudiantes = estudiantes;
        this.categorias = categorias;
        this.prestamos = prestamos;
        this.prestamoService = new PrestamoService(prestamos);
        view.setActions(this);
    }

    public void iniciar() {
        view.setVisible(true);
        recargar();
    }

    @Override public void recargar() {
        view.setBusy(true);
        new SwingWorker<ScreenData, Void>() {
            @Override protected ScreenData doInBackground() throws Exception {
                return new ScreenData(libros.listar(), usuario.esBibliotecario() ? estudiantes.listar() : List.of(),
                        categorias.listar(), prestamos.listarActivos(usuario.esBibliotecario() ? null : usuario.idEstudiante()));
            }

            @Override protected void done() {
                view.setBusy(false);
                try {
                    ScreenData data = get();
                    view.datosCargados(data.libros(), data.estudiantes(), data.categorias(), data.prestamos());
                } catch (Exception e) {
                    mostrarMensaje(errorText(e), true);
                }
            }
        }.execute();
    }

    @Override public void guardarLibro(Libro libro, boolean nuevo) {
        run(nuevo ? "Libro guardado." : "Libro actualizado.",
                () -> { if (nuevo) libros.crear(libro); else libros.actualizar(libro); return null; }, ignored -> recargar());
    }

    @Override public void eliminarLibro(int id) {
        run("Libro eliminado.", () -> { libros.eliminar(id); return null; }, ignored -> recargar());
    }

    @Override public void guardarEstudiante(Estudiante estudiante, String rutAnterior, String claveInicial, boolean nuevo) {
        run(nuevo ? "Estudiante y cuenta de acceso creados." : "Datos del estudiante actualizados.", () -> {
            if (nuevo) estudiantes.crear(estudiante, claveInicial);
            else estudiantes.actualizar(estudiante, rutAnterior);
            return null;
        }, ignored -> recargar());
    }

    @Override public void eliminarEstudiante(Estudiante estudiante) {
        run("Estudiante eliminado.", () -> { estudiantes.eliminar(estudiante.id(), estudiante.rut()); return null; }, ignored -> recargar());
    }

    @Override public void solicitarPrestamo(int idEstudiante, int idLibro) {
        run("Préstamo registrado. La devolución vence en 14 días.",
                () -> prestamoService.prestar(idEstudiante, idLibro), ignored -> recargar());
    }

    @Override public void registrarDevolucion(int idPrestamo, int idEstudiante) {
        run("Devolución registrada y stock actualizado.", () -> {
            prestamoService.devolver(idPrestamo, idEstudiante);
            return null;
        }, ignored -> recargar());
    }

    @Override public void generarReporte(int tipo) {
        run("Reporte generado.", () -> switch (tipo) {
            case 0 -> prestamos.reporteLibrosMasPrestados();
            case 1 -> prestamos.reporteHistorialEstudiante();
            case 2 -> prestamos.reportePrestamosActivos();
            default -> throw new IllegalArgumentException("Selecciona un reporte válido.");
        }, view::mostrarReporte);
    }

    @Override public void crearCategoria(String nombre) {
        run("Categoría creada.", () -> { categorias.crear(nombre); return null; }, ignored -> recargar());
    }

    @Override public void actualizarCategoria(int id, String nombre) {
        run("Categoría actualizada.", () -> { categorias.actualizar(id, nombre); return null; }, ignored -> recargar());
    }

    @Override public void eliminarCategoria(int id) {
        run("Categoría eliminada.", () -> { categorias.eliminar(id); return null; }, ignored -> recargar());
    }

    @Override public void mostrarMensaje(String mensaje, boolean error) {
        view.mostrarMensaje(mensaje, error);
    }

    @Override public void datosCargados(List<Libro> libros, List<Estudiante> estudiantes,
                                        List<Categoria> categorias, List<Prestamo> prestamosActivos) {
        view.datosCargados(libros, estudiantes, categorias, prestamosActivos);
    }

    @Override public void mostrarReporte(String reporte) {
        view.mostrarReporte(reporte);
    }

    private <T> void run(String successMessage, Operation<T> operation, Consumer<T> onSuccess) {
        view.setBusy(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception {
                return operation.run();
            }

            @Override protected void done() {
                view.setBusy(false);
                try {
                    T result = get();
                    if (successMessage != null && !successMessage.isBlank()) mostrarMensaje(successMessage, false);
                    if (onSuccess != null) onSuccess.accept(result);
                } catch (Exception e) {
                    mostrarMensaje(errorText(e), true);
                }
            }
        }.execute();
    }

    private static String errorText(Exception error) {
        Throwable cause = error;
        if (error instanceof ExecutionException && error.getCause() != null) cause = error.getCause();
        String message = cause.getMessage();
        return message == null || message.isBlank() ? "Ocurrió un error al ejecutar la operación." : message;
    }

    @FunctionalInterface
    private interface Operation<T> {
        T run() throws Exception;
    }

    private record ScreenData(List<Libro> libros, List<Estudiante> estudiantes,
                              List<Categoria> categorias, List<Prestamo> prestamos) { }
}
