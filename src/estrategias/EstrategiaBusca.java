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
    public Double devolveValorPortfolio();
    public String devolveResultadosMesAMes();
    public String devolveAcaoMaiorGanho();
    public String devolveAcaoMaiorPrejuizo();
}
