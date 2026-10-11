package servicios;

import DAO.PrestamoDAO;
import modelo.Prestamo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrestamoServiceTest {
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void simultaneousLoansCannotBorrowTheLastCopyTwice() throws Exception {
        var dao = new FakePrestamoDAO(1);
        var service = new PrestamoService(dao);
        int attempts = 16;
        var ready = new CountDownLatch(attempts);
        var start = new CountDownLatch(1);
        var successful = new AtomicInteger();
        var pool = Executors.newFixedThreadPool(attempts);
        try {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<?>>();
            for (int i = 0; i < attempts; i++) {
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        service.prestar(1, 1);
                        successful.incrementAndGet();
                    } catch (SQLException expectedWhenOutOfStock) {
                        // Las solicitudes posteriores a la última copia deben rechazarse.
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS), "Todas las solicitudes deben quedar listas");
            start.countDown();
            for (var future : futures) future.get();
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, successful.get());
        assertEquals(0, dao.stock);
        assertEquals(14, dao.diasPrestamo);
    }

    private static final class FakePrestamoDAO implements PrestamoDAO {
        private int stock;
        private int diasPrestamo;
        private int nextId = 1;

        private FakePrestamoDAO(int stock) { this.stock = stock; }

        @Override public int crearConDescuentoStock(int idEstudiante, int idLibro, LocalDate inicio,
                                                    LocalDate vencimiento) throws SQLException {
            if (stock <= 0) throw new SQLException("Sin stock");
            try { Thread.sleep(3); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            stock--;
            diasPrestamo = (int) ChronoUnit.DAYS.between(inicio, vencimiento);
            return nextId++;
        }
        @Override public void devolverConAumentoStock(int idPrestamo, int idEstudiante) { }
        @Override public List<Prestamo> listarActivos(Integer idEstudiante) { return List.of(); }
        @Override public List<Prestamo> historialEstudiante(int idEstudiante) { return List.of(); }
        @Override public String reporteLibrosMasPrestados() { return ""; }
        @Override public String reporteHistorialEstudiante() { return ""; }
        @Override public String reportePrestamosActivos() { return ""; }
    }
}