/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias.mayza;

import estrategias.EstrategiaBuscaMayza;

/**
 *
 * @author mayza
 */
public class RelatorioAno {
    private Double carteiraFinal = 0D;
    private Double porcGanhoAnoComRelacaoCarteiraInicial = 0D;
    private EstrategiaBuscaMayza.SiglaEmpresas acaoMaiorGanhoAno;
    private EstrategiaBuscaMayza.SiglaEmpresas acaoMenorGanhoAno;
    private Double valorMaiorGanho = 0D;
    private Double valorMenorGanho = 0D;

    public Double getCarteiraFinal() {
        return carteiraFinal;
    }

    public void setCarteiraFinal(Double carteiraFinal) {
        this.carteiraFinal = carteiraFinal;
    }

    public Double getPorcGanhoAnoComRelacaoCarteiraInicial() {
        return porcGanhoAnoComRelacaoCarteiraInicial;
    }

    public void setPorcGanhoAnoComRelacaoCarteiraInicial(Double porcGanhoAnoComRelacaoCarteiraInicial) {
        this.porcGanhoAnoComRelacaoCarteiraInicial = porcGanhoAnoComRelacaoCarteiraInicial;
    }

    public EstrategiaBuscaMayza.SiglaEmpresas getAcaoMaiorGanhoAno() {
        return acaoMaiorGanhoAno;
    }

    public void setAcaoMaiorGanhoAno(EstrategiaBuscaMayza.SiglaEmpresas acaoMaiorGanhoAno) {
        this.acaoMaiorGanhoAno = acaoMaiorGanhoAno;
    }

    public EstrategiaBuscaMayza.SiglaEmpresas getAcaoMenorGanhoAno() {
        return acaoMenorGanhoAno;
    }

    public void setAcaoMenorGanhoAno(EstrategiaBuscaMayza.SiglaEmpresas acaoMenorGanhoAno) {
        this.acaoMenorGanhoAno = acaoMenorGanhoAno;
    }

    public Double getValorMaiorGanho() {
        return valorMaiorGanho;
    }

    public void setValorMaiorGanho(Double valorMaiorGanho) {
        this.valorMaiorGanho = valorMaiorGanho;
    }

    public Double getValorMenorGanho() {
        return valorMenorGanho;
    }

    public void setValorMenorGanho(Double valorMenorGanho) {
        this.valorMenorGanho = valorMenorGanho;
    }
}
