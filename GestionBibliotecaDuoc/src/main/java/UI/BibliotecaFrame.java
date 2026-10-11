package UI;

import controladores.BibliotecaActions;
import modelo.Categoria;
import modelo.Estudiante;
import modelo.Libro;
import modelo.Prestamo;
import modelo.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

public final class BibliotecaFrame extends JFrame {
    private final Usuario usuario;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel estado = new JLabel("Listo");
    private BibliotecaActions actions;

    private final DefaultTableModel booksModel = model("ID", "Título", "Autor", "ISBN", "Editorial", "Stock", "Categoría");
    private final JTable booksTable = new JTable(booksModel);
    private final JTextField titleField = new JTextField();
    private final JTextField authorField = new JTextField();
    private final JTextField isbnField = new JTextField();
    private final JTextField publisherField = new JTextField();
    private final JTextField stockField = new JTextField("1");
    private final JComboBox<Categoria> categoryBox = new JComboBox<>();
    private List<Libro> books = List.of();
    private List<Categoria> categories = List.of();
    private Libro selectedBook;

    private final DefaultTableModel studentsModel = model("ID", "Nombre", "RUT", "Curso", "Correo");
    private final JTable studentsTable = new JTable(studentsModel);
    private final JTextField studentNameField = new JTextField();
    private final JTextField studentRutField = new JTextField();
    private final JTextField studentCourseField = new JTextField();
    private final JTextField studentEmailField = new JTextField();
    private final JPasswordField studentPasswordField = new JPasswordField();
    private List<Estudiante> students = List.of();
    private Estudiante selectedStudent;
    private String oldStudentRut;

    private final JComboBox<Estudiante> studentLoanBox = new JComboBox<>();
    private final JComboBox<Libro> loanBookBox = new JComboBox<>();
    private final DefaultTableModel loansModel = model("ID", "Estudiante", "Libro", "Fecha préstamo", "Vence", "Estado");
    private final JTable loansTable = new JTable(loansModel);
    private List<Prestamo> loans = List.of();
    private final JTextArea reportArea = new JTextArea();

    public BibliotecaFrame(Usuario usuario) {
        super("Biblioteca Escolar | " + usuario.nombre());
        this.usuario = usuario;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 720);
        setMinimumSize(new java.awt.Dimension(920, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));
        add(buildHeader(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(buildStatus(), BorderLayout.SOUTH);
        tabs.addTab("Catálogo de libros", buildBooksPanel());
        if (usuario.esBibliotecario()) tabs.addTab("Estudiantes", buildStudentsPanel());
        tabs.addTab("Préstamos y devoluciones", buildLoansPanel());
        if (usuario.esBibliotecario()) tabs.addTab("Reportes", buildReportsPanel());
    }

    public void setActions(BibliotecaActions actions) {
        this.actions = actions;
    }

    public void setBusy(boolean busy) {
        estado.setText(busy ? "Procesando…" : "Listo");
        setCursor(busy ? java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR)
                : java.awt.Cursor.getDefaultCursor());
    }

    public void datosCargados(List<Libro> libros, List<Estudiante> estudiantes,
                              List<Categoria> categorias, List<Prestamo> prestamosActivos) {
        this.books = List.copyOf(libros);
        this.students = List.copyOf(estudiantes);
        this.categories = List.copyOf(categorias);
        this.loans = List.copyOf(prestamosActivos);

        booksModel.setRowCount(0);
        for (Libro book : books) {
            booksModel.addRow(new Object[]{book.id(), book.titulo(), book.autor(), book.isbn(),
                    book.editorial(), book.stock(), book.categoria()});
        }
        studentsModel.setRowCount(0);
        for (Estudiante student : students) {
            studentsModel.addRow(new Object[]{student.id(), student.nombre(), student.rut(), student.curso(), student.correo()});
        }
        loansModel.setRowCount(0);
        for (Prestamo loan : loans) {
            loansModel.addRow(new Object[]{loan.id(), loan.estudiante(), loan.libro(), loan.fechaPrestamo(),
                    loan.fechaDevolucion(), loan.estaAtrasado() ? "Atrasado" : "Activo"});
        }

        categoryBox.removeAllItems();
        for (Categoria category : categories) categoryBox.addItem(category);
        loanBookBox.removeAllItems();
        for (Libro book : books) if (book.stock() > 0) loanBookBox.addItem(book);
        studentLoanBox.removeAllItems();
        for (Estudiante student : students) studentLoanBox.addItem(student);
        if (selectedBook != null) selectBook(selectedBook.id());
        if (selectedStudent != null) selectStudent(selectedStudent.id());
    }

    public void mostrarMensaje(String mensaje, boolean error) {
        estado.setText(mensaje);
        if (error) JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarReporte(String reporte) {
        reportArea.setText(reporte);
        reportArea.setCaretPosition(0);
        if (usuario.esBibliotecario()) tabs.setSelectedIndex(tabs.getTabCount() - 1);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 4, 14));
        JLabel title = new JLabel("Biblioteca Escolar");
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 20f));
        panel.add(title, BorderLayout.WEST);
        String role = usuario.esBibliotecario() ? "Bibliotecario" : "Estudiante";
        panel.add(new JLabel(usuario.nombre() + "  |  " + role), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildStatus() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));
        panel.add(estado, BorderLayout.CENTER);
        JButton refresh = new JButton("Actualizar");
        refresh.addActionListener(event -> actions.recargar());
        panel.add(refresh, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        configureTable(booksTable);
        booksTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) selectBookFromTable();
        });
        panel.add(new JScrollPane(booksTable), BorderLayout.CENTER);
        if (usuario.esBibliotecario()) {
            JPanel editor = new JPanel(new BorderLayout(6, 6));
            JPanel fields = new JPanel(new GridLayout(2, 6, 6, 4));
            fields.add(new JLabel("Título")); fields.add(new JLabel("Autor")); fields.add(new JLabel("ISBN"));
            fields.add(new JLabel("Editorial")); fields.add(new JLabel("Stock")); fields.add(new JLabel("Categoría"));
            fields.add(titleField); fields.add(authorField); fields.add(isbnField);
            fields.add(publisherField); fields.add(stockField); fields.add(categoryBox);
            editor.add(fields, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton save = new JButton("Guardar libro");
            JButton clear = new JButton("Nuevo / limpiar");
            JButton delete = new JButton("Eliminar libro");
            JButton addCategory = new JButton("Nueva categoría");
            JButton editCategory = new JButton("Editar categoría");
            JButton deleteCategory = new JButton("Eliminar categoría");
            save.addActionListener(event -> saveBook());
            clear.addActionListener(event -> clearBookForm());
            delete.addActionListener(event -> deleteBook());
            addCategory.addActionListener(event -> createCategory());
            editCategory.addActionListener(event -> editCategory());
            deleteCategory.addActionListener(event -> deleteCategory());
            buttons.add(save); buttons.add(clear); buttons.add(delete);
            buttons.add(addCategory); buttons.add(editCategory); buttons.add(deleteCategory);
            editor.add(buttons, BorderLayout.SOUTH);
            panel.add(editor, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel buildStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        configureTable(studentsTable);
        studentsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) selectStudentFromTable();
        });
        panel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);

        JPanel editor = new JPanel(new BorderLayout(6, 6));
        JPanel fields = new JPanel(new GridLayout(2, 5, 6, 4));
        fields.add(new JLabel("Nombre")); fields.add(new JLabel("RUT")); fields.add(new JLabel("Curso"));
        fields.add(new JLabel("Correo")); fields.add(new JLabel("Clave inicial (solo al crear)"));
        fields.add(studentNameField); fields.add(studentRutField); fields.add(studentCourseField);
        fields.add(studentEmailField); fields.add(studentPasswordField);
        editor.add(fields, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton save = new JButton("Guardar estudiante");
        JButton clear = new JButton("Nuevo / limpiar");
        JButton delete = new JButton("Eliminar estudiante");
        save.addActionListener(event -> saveStudent());
        clear.addActionListener(event -> clearStudentForm());
        delete.addActionListener(event -> deleteStudent());
        buttons.add(save); buttons.add(clear); buttons.add(delete);
        editor.add(buttons, BorderLayout.SOUTH);
        panel.add(editor, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildLoansPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        configureTable(loansTable);
        panel.add(new JScrollPane(loansTable), BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if (usuario.esBibliotecario()) actionsPanel.add(new JLabel("Estudiante:"));
        if (usuario.esBibliotecario()) actionsPanel.add(studentLoanBox);
        actionsPanel.add(new JLabel("Libro:"));
        actionsPanel.add(loanBookBox);
        JButton borrow = new JButton("Registrar préstamo");
        JButton returnBook = new JButton("Registrar devolución del seleccionado");
        borrow.addActionListener(event -> requestLoan());
        returnBook.addActionListener(event -> returnLoan());
        actionsPanel.add(borrow);
        actionsPanel.add(returnBook);
        panel.add(actionsPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> reportType = new JComboBox<>(new String[]{
                "Libros más prestados", "Historial de préstamos", "Préstamos activos"});
        JButton generate = new JButton("Generar reporte");
        generate.addActionListener(event -> actions.generarReporte(reportType.getSelectedIndex()));
        controls.add(reportType);
        controls.add(generate);
        reportArea.setEditable(false);
        reportArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        return panel;
    }

    private void saveBook() {
        try {
            Categoria category = (Categoria) categoryBox.getSelectedItem();
            if (category == null) throw new IllegalArgumentException("Crea o selecciona una categoría.");
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
            Libro book = new Libro(selectedBook == null ? 0 : selectedBook.id(), titleField.getText().trim(),
                    authorField.getText().trim(), isbnField.getText().trim(), publisherField.getText().trim(),
                    stock, category.id(), category.nombre());
            if (book.titulo().isBlank() || book.autor().isBlank())
                throw new IllegalArgumentException("Completa el título y el autor.");
            actions.guardarLibro(book, selectedBook == null);
        } catch (NumberFormatException error) {
            mostrarMensaje("El stock debe ser un número entero.", true);
        } catch (IllegalArgumentException error) {
            mostrarMensaje(error.getMessage(), true);
        }
    }

    private void deleteBook() {
        if (selectedBook == null) { mostrarMensaje("Selecciona un libro primero.", true); return; }
        if (confirm("¿Eliminar el libro «" + selectedBook.titulo() + "»?")) actions.eliminarLibro(selectedBook.id());
    }

    private void clearBookForm() {
        selectedBook = null;
        booksTable.clearSelection();
        titleField.setText(""); authorField.setText(""); isbnField.setText("");
        publisherField.setText(""); stockField.setText("1");
    }

    private void selectBookFromTable() {
        int row = booksTable.getSelectedRow();
        if (row < 0 || row >= books.size()) return;
        selectedBook = books.get(row);
        titleField.setText(selectedBook.titulo()); authorField.setText(selectedBook.autor());
        isbnField.setText(selectedBook.isbn()); publisherField.setText(selectedBook.editorial());
        stockField.setText(Integer.toString(selectedBook.stock()));
        selectCategory(selectedBook.idCategoria());
    }

    private void selectBook(int id) {
        for (int i = 0; i < books.size(); i++) if (books.get(i).id() == id) {
            booksTable.setRowSelectionInterval(i, i);
            return;
        }
    }

    private void createCategory() {
        String name = JOptionPane.showInputDialog(this, "Nombre de la categoría:", "Nueva categoría", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.isBlank()) actions.crearCategoria(name.trim());
    }

    private void editCategory() {
        Categoria category = (Categoria) categoryBox.getSelectedItem();
        if (category == null) { mostrarMensaje("Selecciona una categoría primero.", true); return; }
        String name = JOptionPane.showInputDialog(this, "Nombre de la categoría:", category.nombre());
        if (name != null && !name.isBlank()) actions.actualizarCategoria(category.id(), name.trim());
    }

    private void deleteCategory() {
        Categoria category = (Categoria) categoryBox.getSelectedItem();
        if (category == null) { mostrarMensaje("Selecciona una categoría primero.", true); return; }
        if (confirm("¿Eliminar la categoría «" + category.nombre() + "»?")) actions.eliminarCategoria(category.id());
    }

    private void saveStudent() {
        String password = new String(studentPasswordField.getPassword());
        if (selectedStudent == null && password.isBlank()) {
            mostrarMensaje("Ingresa una clave inicial para la cuenta del estudiante.", true);
            return;
        }
        Estudiante student = new Estudiante(selectedStudent == null ? 0 : selectedStudent.id(),
                studentNameField.getText().trim(), studentRutField.getText().trim(),
                studentCourseField.getText().trim(), studentEmailField.getText().trim());
        if (student.nombre().isBlank() || student.rut().isBlank() || student.correo().isBlank()) {
            mostrarMensaje("Completa nombre, RUT y correo.", true);
            return;
        }
        actions.guardarEstudiante(student, oldStudentRut, password, selectedStudent == null);
        studentPasswordField.setText("");
    }

    private void deleteStudent() {
        if (selectedStudent == null) { mostrarMensaje("Selecciona un estudiante primero.", true); return; }
        if (confirm("¿Eliminar a «" + selectedStudent.nombre() + "» y su cuenta?")) actions.eliminarEstudiante(selectedStudent);
    }

    private void clearStudentForm() {
        selectedStudent = null;
        oldStudentRut = null;
        studentsTable.clearSelection();
        studentNameField.setText(""); studentRutField.setText(""); studentCourseField.setText("");
        studentEmailField.setText(""); studentPasswordField.setText("");
    }

    private void selectStudentFromTable() {
        int row = studentsTable.getSelectedRow();
        if (row < 0 || row >= students.size()) return;
        selectedStudent = students.get(row);
        oldStudentRut = selectedStudent.rut();
        studentNameField.setText(selectedStudent.nombre()); studentRutField.setText(selectedStudent.rut());
        studentCourseField.setText(selectedStudent.curso()); studentEmailField.setText(selectedStudent.correo());
        studentPasswordField.setText("");
    }

    private void selectStudent(int id) {
        for (int i = 0; i < students.size(); i++) if (students.get(i).id() == id) {
            studentsTable.setRowSelectionInterval(i, i);
            return;
        }
    }

    private void requestLoan() {
        Libro book = (Libro) loanBookBox.getSelectedItem();
        if (book == null) { mostrarMensaje("No hay libros con stock disponible.", true); return; }
        Integer studentId;
        if (usuario.esBibliotecario()) {
            Estudiante student = (Estudiante) studentLoanBox.getSelectedItem();
            studentId = student == null ? null : student.id();
        } else {
            studentId = usuario.idEstudiante();
        }
        if (studentId == null) { mostrarMensaje("La cuenta no está asociada a un estudiante.", true); return; }
        actions.solicitarPrestamo(studentId, book.id());
    }

    private void returnLoan() {
        int row = loansTable.getSelectedRow();
        if (row < 0 || row >= loans.size()) { mostrarMensaje("Selecciona un préstamo activo.", true); return; }
        Prestamo loan = loans.get(row);
        Integer studentId;
        if (usuario.esBibliotecario()) studentId = loan.idEstudiante();
        else studentId = usuario.idEstudiante();
        if (studentId == null) { mostrarMensaje("La cuenta no está asociada a un estudiante.", true); return; }
        actions.registrarDevolucion(loan.id(), studentId);
    }

    private void selectCategory(int id) {
        for (int i = 0; i < categories.size(); i++) if (categories.get(i).id() == id) {
            categoryBox.setSelectedIndex(i);
            return;
        }
    }

    private boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void configureTable(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        table.setFillsViewportHeight(true);
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}
