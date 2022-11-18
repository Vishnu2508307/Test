package data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Ack implements Exchangeable, Serializable {

    private static final long serialVersionUID = 1997179687824124010L;

    private UUID id;
    private String clientId;
    private Version n;
    private Version m;

    public UUID getId() {
        return id;
    }

    public Ack setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public Ack setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public Version getN() {
        return n;
    }

    public Ack setN(Version n) {
        this.n = n;
        return this;
    }

    public Version getM() {
        return m;
    }

    public Ack setM(Version m) {
        this.m = m;
        return this;
    }

    @Override
    public Type getType() {
        return Type.ACK;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ack ack = (Ack) o;
        return Objects.equals(id, ack.id)
                && Objects.equals(clientId, ack.clientId)
                && Objects.equals(n, ack.n)
                && Objects.equals(m, ack.m);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clientId, n, m);
    }

    @Override
    public String toString() {
        return "Ack{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", n=" + n +
                ", m=" + m +
                '}';
    }
}
