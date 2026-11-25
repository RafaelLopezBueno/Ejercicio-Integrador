package es.tracker.repository;

import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Empresa;
import es.tracker.model.EmpresaResumen;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de empresas sobre JDBC.
 */
public class JdbcEmpresaRepository implements EmpresaRepository {

    private static final String COLUMNAS = "id, nombre, sector, web, fecha_registro";

    private final Database database;

    public JdbcEmpresaRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<Empresa> findAll() {
        String sql = "SELECT " + COLUMNAS + " FROM empresa ORDER BY nombre";
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Empresa> empresas = new ArrayList<>();
            while (rs.next()) {
                empresas.add(mapear(rs));
            }
            return empresas;
        } catch (SQLException e) {
            throw Jdbc.error("listar las empresas", e);
        }
    }

    @Override
    public List<EmpresaResumen> findAllConTotales() {
        String sql = "SELECT e.id, e.nombre, e.sector, e.web, e.fecha_registro, COUNT(o.id) AS total "
                + "FROM empresa e LEFT JOIN oferta o ON o.id_empresa = e.id "
                + "GROUP BY e.id, e.nombre, e.sector, e.web, e.fecha_registro ORDER BY e.nombre";
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<EmpresaResumen> resumenes = new ArrayList<>();
            while (rs.next()) {
                resumenes.add(new EmpresaResumen(mapear(rs), rs.getInt("total")));
            }
            return resumenes;
        } catch (SQLException e) {
            throw Jdbc.error("listar las empresas con sus ofertas", e);
        }
    }

    @Override
    public Optional<Empresa> findById(int id) {
        return buscarUna("SELECT " + COLUMNAS + " FROM empresa WHERE id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<Empresa> findByNombre(String nombre) {
        return buscarUna("SELECT " + COLUMNAS + " FROM empresa WHERE LOWER(nombre) = LOWER(?)",
                ps -> ps.setString(1, nombre.trim()));
    }

    @Override
    public Empresa save(Empresa empresa) {
        findByNombre(empresa.nombre())
                .filter(existente -> !existente.id().equals(empresa.id()))
                .ifPresent(existente -> {
                    throw new CandidaturaException("Ya existe una empresa llamada '" + existente.nombre() + "'");
                });
        try (Database.Conexion c = database.abrir()) {
            if (empresa.id() == null) {
                return insertar(c, empresa);
            }
            return actualizar(c, empresa);
        } catch (SQLException e) {
            if (Jdbc.esDuplicado(e)) {
                throw new CandidaturaException("Ya existe una empresa llamada '" + empresa.nombre() + "'", e);
            }
            throw Jdbc.error("guardar la empresa", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement("DELETE FROM empresa WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (Jdbc.esClaveForanea(e)) {
                throw new CandidaturaException("No se puede eliminar la empresa porque tiene ofertas asociadas", e);
            }
            throw Jdbc.error("eliminar la empresa", e);
        }
    }

    private Empresa insertar(Database.Conexion c, Empresa empresa) throws SQLException {
        String sql = "INSERT INTO empresa (nombre, sector, web, fecha_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, empresa.nombre().trim());
            ps.setString(2, Jdbc.vacioANulo(empresa.sector()));
            ps.setString(3, Jdbc.vacioANulo(empresa.web()));
            ps.setObject(4, empresa.fechaRegistro() != null ? empresa.fechaRegistro() : LocalDate.now());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return findById(claves.getInt(1)).orElseThrow();
            }
        }
    }

    private Empresa actualizar(Database.Conexion c, Empresa empresa) throws SQLException {
        String sql = "UPDATE empresa SET nombre = ?, sector = ?, web = ?, fecha_registro = ? WHERE id = ?";
        try (PreparedStatement ps = c.connection().prepareStatement(sql)) {
            ps.setString(1, empresa.nombre().trim());
            ps.setString(2, Jdbc.vacioANulo(empresa.sector()));
            ps.setString(3, Jdbc.vacioANulo(empresa.web()));
            ps.setObject(4, empresa.fechaRegistro());
            ps.setInt(5, empresa.id());
            if (ps.executeUpdate() == 0) {
                throw new CandidaturaException("No existe ninguna empresa con ID " + empresa.id());
            }
            return findById(empresa.id()).orElseThrow();
        }
    }

    private Optional<Empresa> buscarUna(String sql, Parametros parametros) {
        try (Database.Conexion c = database.abrir(); PreparedStatement ps = c.connection().prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Jdbc.error("buscar la empresa", e);
        }
    }

    private static Empresa mapear(ResultSet rs) throws SQLException {
        return new Empresa(rs.getInt("id"), rs.getString("nombre"), rs.getString("sector"), rs.getString("web"),
                rs.getObject("fecha_registro", LocalDate.class));
    }

    @FunctionalInterface
    private interface Parametros {
        void asignar(PreparedStatement ps) throws SQLException;
    }
}
