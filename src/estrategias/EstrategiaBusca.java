package estrategias;

import java.util.List;
import modelo.Dia;

/**
 *
 * @author Camargo
 */
public interface EstrategiaBusca {
    public void recebeDadosTreino(List<Dia> periodo);
    public void recebeDadosTeste(List<Dia> periodo);
    public void aplicaEstrategiaBusca();
  //considerem remover e deixar so o string, caso n concordem, discomentem e removam a linha que eu inseri
    //public Double devolveValorPortfolio();
    public String devolveValorPortfolio();
    public String devolveResultadosMesAMes();
    public String devolveAcaoMaiorGanho();
    public String devolveAcaoMaiorPrejuizo();
}
