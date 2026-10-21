package rutasv.modelo;

import java.time.LocalTime;
import java.util.Objects;

public class UnidadTransporte implements Comparable<UnidadTransporte> {
    private String id;
    private TipoRuta tipoRuta;
    private LocalTime horaSalida;

    public UnidadTransporte(String id, TipoRuta tipoRuta, LocalTime horaSalida) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la unidad no puede ser vacio");
        }
        if (tipoRuta == null) {
            throw new IllegalArgumentException("El tipo de ruta no puede ser nulo");
        }
        if (horaSalida == null) {
            throw new IllegalArgumentException("La hora de salida no puede ser nula");
        }
        this.id = id.trim().toUpperCase();
        this.tipoRuta = tipoRuta;
        this.horaSalida = horaSalida;
    }

    public String getId() {
        return id;
    }

    public TipoRuta getTipoRuta() {
        return tipoRuta;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    @Override
    public int compareTo(UnidadTransporte otra) {
        if (otra == null) return -1;

        // 1. Regla RN-4: EXPRES primero que NORMAL
        if (this.tipoRuta != otra.tipoRuta) {
            return this.tipoRuta == TipoRuta.EXPRES ? -1 : 1;
        }

        // 2. Misma prioridad/tipo: hora mas proxima
        int cmpHora = this.horaSalida.compareTo(otra.horaSalida);
        if (cmpHora != 0) {
            return cmpHora;
        }

        // 3. Desempate determinista por ID de unidad
        return this.id.compareTo(otra.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnidadTransporte que = (UnidadTransporte) o;
        return Objects.equals(id, que.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
