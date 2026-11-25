package es.tracker.repository;

import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Entrevista;
import es.tracker.model.EntrevistaVista;
import es.tracker.model.ResultadoEntrevista;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de entrevistas sobre JDBC.
 */
public class JdbcEntrevistaRepository implements EntrevistaRepository {

    private static final String COLUMNAS = "id, id_oferta, fecha, tipo, notas, resultado";

    private final Database database;

    public JdbcEntrevistaRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<EntrevistaVista> findAllVistas() {
        String sql = "SELECT n.id, n.id_oferta, n.fecha, n.tipo, n.notas, n.resultado, "
                + "o.titulo AS titulo_oferta, e.nombre AS empresa_nombre "
                + "FROM entrevista n JOIN oferta o ON o.id = n.id_oferta JOIN empresa e ON e.id = o.id_empresa "
                + "ORDER BY n.fecha DESC, n.id DESC";
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<EntrevistaVista> vistas = new ArrayList<>();
            while (rs.next()) {
                vistas.add(new EntrevistaVista(mapear(rs), rs.getString("titulo_oferta"), rs.getString("empresa_nombre")));
            }
            return vistas;
        } catch (SQLException e) {
            throw Jdbc.error("listar las entrevistas", e);
        }
    }

    @Override
    public List<Entrevista> findByOferta(int idOferta) {
        String sql = "SELECT " + COLUMNAS + " FROM entrevista WHERE id_oferta = ? ORDER BY fecha DESC, id DESC";
        try (Database.Conexion c = database.abrir(); PreparedStatement ps = c.connection().prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                List<Entrevista> entrevistas = new ArrayList<>();
                while (rs.next()) {
                    entrevistas.add(mapear(rs));
                }
                return entrevistas;
            }
        } catch (SQLException e) {
            throw Jdbc.error("listar las entrevistas de la oferta", e);
        }
    }

    @Override
    public Optional<Entrevista> findById(int id) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement("SELECT " + COLUMNAS + " FROM entrevista WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Jdbc.error("buscar la entrevista", e);
        }
    }

    @Override
    public Entrevista save(Entrevista entrevista) {
        try (Database.Conexion c = database.abrir()) {
            return entrevista.id() == null ? insertar(c, entrevista) : actualizar(c, entrevista);
        } catch (SQLException e) {
            if (Jdbc.esClaveForanea(e)) {
                throw new CandidaturaException("La oferta indicada no existe", e);
            }
            throw Jdbc.error("guardar la entrevista", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        return borrar("DELETE FROM entrevista WHERE id = ?", id) > 0;
    }

    @Override
    public int deleteByOferta(int idOferta) {
        return borrar("DELETE FROM entrevista WHERE id_oferta = ?", idOferta);
    }

    @Override
    public int deleteByEmpresa(int idEmpresa) {
        return borrar("DELETE FROM entrevista WHERE id_oferta IN (SELECT id FROM oferta WHERE id_empresa = ?)", idEmpresa);
    }

    private int borrar(String sql, int id) {
        try (Database.Conexion c = database.abrir(); PreparedStatement ps = c.connection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw Jdbc.error("eliminar entrevistas", e);
        }
    }

    private Entrevista insertar(Database.Conexion c, Entrevista entrevista) throws SQLException {
        String sql = "INSERT INTO entrevista (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, entrevista);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return findById(claves.getInt(1)).orElseThrow();
            }
        }
    }

    private Entrevista actualizar(Database.Conexion c, Entrevista entrevista) throws SQLException {
        String sql = "UPDATE entrevista SET id_oferta = ?, fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (PreparedStatement ps = c.connection().prepareStatement(sql)) {
            asignar(ps, entrevista);
            ps.setInt(6, entrevista.id());
            if (ps.executeUpdate() == 0) {
                throw new CandidaturaException("No existe ninguna entrevista con ID " + entrevista.id());
            }
            return findById(entrevista.id()).orElseThrow();
        }
    }

    private static void asignar(PreparedStatement ps, Entrevista entrevista) throws SQLException {
        ps.setInt(1, entrevista.idOferta());
        ps.setObject(2, entrevista.fecha());
        ps.setString(3, entrevista.tipo().trim());
        ps.setString(4, Jdbc.vacioANulo(entrevista.notas()));
        ps.setString(5, entrevista.resultado().getTexto());
    }

    private static Entrevista mapear(ResultSet rs) throws SQLException {
        return new Entrevista(rs.getInt("id"), rs.getInt("id_oferta"), rs.getObject("fecha", LocalDate.class),
                rs.getString("tipo"), Jdbc.noNulo(rs.getString("notas")),
                ResultadoEntrevista.desdeTexto(rs.getString("resultado")));
    }
}
