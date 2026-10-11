package modelo;

public record Categoria(int id, String nombre) {
    @Override public String toString() { return nombre; }
}