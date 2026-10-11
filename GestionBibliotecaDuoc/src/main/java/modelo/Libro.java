package modelo;

public record Libro(int id, String titulo, String autor, String isbn, String editorial,
                    int stock, int idCategoria, String categoria) {
    @Override public String toString() { return titulo + " (stock: " + stock + ")"; }
}