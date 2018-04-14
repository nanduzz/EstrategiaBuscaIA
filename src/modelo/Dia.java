package modelo;

import java.time.LocalDate;

/**
 *
 * @author Camargo
 */
public class Dia {

    String acao;
    String sigla;
    LocalDate data;
    Double valorAbertura;
    Double valorFechamento;
    Double precoMin;
    Double precoMax;
    Double precoMedio;

    public Dia(String acao, String sigla, LocalDate data, Double valorAbertura,
            Double valorFechamento, Double precoMin, Double precoMax,
            Double precoMedio) {
        this.acao = acao;
        this.sigla = sigla;
        this.data = data;
        this.valorAbertura = valorAbertura;
        this.valorFechamento = valorFechamento;
        this.precoMin = precoMin;
        this.precoMax = precoMax;
        this.precoMedio = precoMedio;
    }

    public String getAcao() {
        return acao;
    }

    public String getSigla() {
        return sigla;
    }

    public LocalDate getData() {
        return data;
    }

    public Double getValorAbertura() {
        return valorAbertura;
    }

    public Double getValorFechamento() {
        return valorFechamento;
    }

    public Double getPrecoMin() {
        return precoMin;
    }

    public Double getPrecoMax() {
        return precoMax;
    }

    public Double getPrecoMedio() {
        return precoMedio;
    }

}
