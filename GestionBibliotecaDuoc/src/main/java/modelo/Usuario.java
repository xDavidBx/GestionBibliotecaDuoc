package modelo;

public record Usuario(int id, String nombre, String rut, String correo, String rol, Integer idEstudiante) {
    public boolean esBibliotecario() {
        return "BIBLIOTECARIO".equalsIgnoreCase(rol);
    }
}