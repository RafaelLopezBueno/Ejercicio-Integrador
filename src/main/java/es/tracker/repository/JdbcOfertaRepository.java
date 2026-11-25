package es.tracker.repository;

import es.tracker.db.Database;
import es.tracker.exception.CandidaturaException;
import es.tracker.model.Estado;
import es.tracker.model.FiltroOfertas;
import es.tracker.model.Oferta;
import es.tracker.model.OfertaVista;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de ofertas sobre JDBC.
 */
public class JdbcOfertaRepository implements OfertaRepository {

    private static final String SELECT_VISTA = "SELECT o.id, o.id_empresa, o.titulo, o.descripcion, o.estado, "
            + "o.fecha_postulacion, o.enlace, o.salario, o.notas, e.nombre AS empresa_nombre "
            + "FROM oferta o JOIN empresa e ON e.id = o.id_empresa";

    private final Database database;

    public JdbcOfertaRepository(Database database) {
        this.database = database;
    }

    @Override
    public List<OfertaVista> buscar(FiltroOfertas filtro) {
        StringBuilder sql = new StringBuilder(SELECT_VISTA).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (filtro.estado() != null) {
            sql.append(" AND o.estado = ?");
            parametros.add(filtro.estado().getTexto());
        }
        if (filtro.empresa() != null && !filtro.empresa().isBlank()) {
            sql.append(" AND LOWER(e.nombre) LIKE ?");
            parametros.add("%" + filtro.empresa().trim().toLowerCase() + "%");
        }
        if (filtro.texto() != null && !filtro.texto().isBlank()) {
            sql.append(" AND LOWER(o.titulo) LIKE ?");
            parametros.add("%" + filtro.texto().trim().toLowerCase() + "%");
        }
        if (filtro.salarioMin() != null) {
            sql.append(" AND o.salario >= ?");
            parametros.add(filtro.salarioMin());
        }
        if (filtro.salarioMax() != null) {
            sql.append(" AND o.salario <= ?");
            parametros.add(filtro.salarioMax());
        }
        if (filtro.fechaDesde() != null) {
            sql.append(" AND o.fecha_postulacion >= ?");
            parametros.add(filtro.fechaDesde());
        }
        if (filtro.fechaHasta() != null) {
            sql.append(" AND o.fecha_postulacion <= ?");
            parametros.add(filtro.fechaHasta());
        }
        sql.append(" ORDER BY o.fecha_postulacion DESC, o.id DESC");

        try (Database.Conexion c = database.abrir(); PreparedStatement ps = c.connection().prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<OfertaVista> ofertas = new ArrayList<>();
                while (rs.next()) {
                    ofertas.add(mapearVista(rs));
                }
                return ofertas;
            }
        } catch (SQLException e) {
            throw Jdbc.error("buscar ofertas", e);
        }
    }

    @Override
    public Optional<Oferta> findById(int id) {
        return findVistaById(id).map(OfertaVista::oferta);
    }

    @Override
    public Optional<OfertaVista> findVistaById(int id) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement(SELECT_VISTA + " WHERE o.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearVista(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw Jdbc.error("buscar la oferta", e);
        }
    }

    @Override
    public Oferta save(Oferta oferta) {
        try (Database.Conexion c = database.abrir()) {
            return oferta.id() == null ? insertar(c, oferta) : actualizar(c, oferta);
        } catch (SQLException e) {
            if (Jdbc.esClaveForanea(e)) {
                throw new CandidaturaException("La empresa indicada no existe", e);
            }
            throw Jdbc.error("guardar la oferta", e);
        }
    }

    @Override
    public boolean actualizarEstado(int id, Estado estado) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement("UPDATE oferta SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.getTexto());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw Jdbc.error("cambiar el estado de la oferta", e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement("DELETE FROM oferta WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (Jdbc.esClaveForanea(e)) {
                throw new CandidaturaException("No se puede eliminar la oferta porque tiene entrevistas asociadas", e);
            }
            throw Jdbc.error("eliminar la oferta", e);
        }
    }

    @Override
    public int deleteByEmpresa(int idEmpresa) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement("DELETE FROM oferta WHERE id_empresa = ?")) {
            ps.setInt(1, idEmpresa);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw Jdbc.error("eliminar las ofertas de la empresa", e);
        }
    }

    @Override
    public int countByEmpresa(int idEmpresa) {
        try (Database.Conexion c = database.abrir();
             PreparedStatement ps = c.connection().prepareStatement("SELECT COUNT(*) FROM oferta WHERE id_empresa = ?")) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw Jdbc.error("contar las ofertas de la empresa", e);
        }
    }

    private Oferta insertar(Database.Conexion c, Oferta oferta) throws SQLException {
        String sql = "INSERT INTO oferta (id_empresa, titulo, descripcion, estado, fecha_postulacion, enlace, salario, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(ps, oferta);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return findById(claves.getInt(1)).orElseThrow();
            }
        }
    }

    private Oferta actualizar(Database.Conexion c, Oferta oferta) throws SQLException {
        String sql = "UPDATE oferta SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, fecha_postulacion = ?, "
                + "enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (PreparedStatement ps = c.connection().prepareStatement(sql)) {
            asignar(ps, oferta);
            ps.setInt(9, oferta.id());
            if (ps.executeUpdate() == 0) {
                throw new CandidaturaException("No existe ninguna oferta con ID " + oferta.id());
            }
            return findById(oferta.id()).orElseThrow();
        }
    }

    private static void asignar(PreparedStatement ps, Oferta oferta) throws SQLException {
        ps.setInt(1, oferta.idEmpresa());
        ps.setString(2, oferta.titulo().trim());
        ps.setString(3, Jdbc.vacioANulo(oferta.descripcion()));
        ps.setString(4, oferta.estado().getTexto());
        ps.setObject(5, oferta.fechaPostulacion());
        ps.setString(6, Jdbc.vacioANulo(oferta.enlace()));
        Jdbc.setDecimal(ps, 7, oferta.salario());
        ps.setString(8, Jdbc.vacioANulo(oferta.notas()));
    }

    private static OfertaVista mapearVista(ResultSet rs) throws SQLException {
        Oferta oferta = new Oferta(rs.getInt("id"), rs.getInt("id_empresa"), rs.getString("titulo"),
                Jdbc.noNulo(rs.getString("descripcion")), Estado.desdeTexto(rs.getString("estado")),
                rs.getObject("fecha_postulacion", LocalDate.class), Jdbc.noNulo(rs.getString("enlace")),
                rs.getBigDecimal("salario"), Jdbc.noNulo(rs.getString("notas")));
        return new OfertaVista(oferta, rs.getString("empresa_nombre"));
    }
}
