/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import modelo.Dia;
import util.indicadores.RSI;

/**
 *
 * @author mayza
 */
public class EstrategiaBuscaMayza implements EstrategiaBusca {
    
    private Double carteira;
    private Double carteiraVirtual;
    private final Double CARTEIRA_INICIAL = 100000D;
    private final Map<String, List<Dia>> dadosTreino = new HashMap<>(); //
    private final Map<String, List<Dia>> dadosTeste = new HashMap<>();
    private final Map<String, List<Double>> diasPassados = new HashMap<>(); // siglaEmpresa / valor
    private final Set<String> diasTreino = new LinkedHashSet<>();
    private final Set<String> diasTeste = new LinkedHashSet<>();
    
    private final Map<String, List<Empresa>> movimentoDia = new HashMap<>();
    private final Map<Empresas, Empresa> portfolio = new HashMap<>();
    private final Map<String, Mes> relatorio2016 = new HashMap<>();
    private final Ano relatorioAno = new Ano();

    public EstrategiaBuscaMayza(){
        this.carteira = 100000D;
        this.carteiraVirtual = 100000D;
        for(Empresas e: Empresas.values()){
            this.portfolio.put(e, new Empresa(e, 0, 0D, 10D));
        }
        movimentoDia.put("Compradas", new ArrayList<>());
        movimentoDia.put("Vendidas", new ArrayList<>());
        movimentoDia.put("Ja tinham acoes", new ArrayList<>()); //ia comprar mas ja tinha acao
        movimentoDia.put("Nao tinham acoes", new ArrayList<>()); //ia vender mas nao tinha acao
        for(Integer i=1;i<13;i++){
            if(i < 10){
                relatorio2016.put("0" + i.toString(), new Mes());
            } else {
                relatorio2016.put(i.toString(), new Mes());
            }
        }
    }
    
    @Override
    public void recebeDadosTreino(List<Dia> periodo) {
        periodo.forEach((Dia d) -> {
            this.diasTreino.add(d.getData().toString());
            if (!dadosTreino.containsKey(d.getData().toString())) {
                dadosTreino.put(d.getData().toString(), new ArrayList<>());
            }
            dadosTreino.get(d.getData().toString()).add(d);
        });
    }

    @Override
    public void recebeDadosTeste(List<Dia> periodo) {
        periodo.forEach((Dia d) -> {
            this.diasTeste.add(d.getData().toString());
            if (!dadosTeste.containsKey(d.getData().toString())) {
                dadosTeste.put(d.getData().toString(), new ArrayList<>());
            }
            dadosTeste.get(d.getData().toString()).add(d);
        });
    }

    @Override
    public void aplicaEstrategiaBusca() {
        int cont = 0;
        String mesAnterior = "01";
        String mesAtual = "01";
        
        Dia ultimoDia = null;
        for(String dia: diasTreino){ //para cada dia
            for(Dia diaEmpresa: dadosTreino.get(dia)){
                if (!diasPassados.containsKey(diaEmpresa.getSigla())) {
                    diasPassados.put(diaEmpresa.getSigla(), new ArrayList<>());
                }
                Double rsi = new RSI(this.diasPassados.get(diaEmpresa.getSigla()), 7).calcula();
                if(rsi < 30 && rsi != 0){
                    compra(diaEmpresa);
                } else if(rsi > 70){
                    vendeAcoesEmpresa(diaEmpresa);
                } else {
                    
                }
                recalculaFeromonios();
                diasPassados.get(diaEmpresa.getSigla()).add(diaEmpresa.getValorFechamento());
            }
        } 
        for(String dia: diasTeste){ //para cada dia
            mesAtual = dia.substring(5,7);
            for(Dia diaEmpresa: dadosTeste.get(dia)){
                ultimoDia = diaEmpresa;
                if (!diasPassados.containsKey(diaEmpresa.getSigla())) {
                    diasPassados.put(diaEmpresa.getSigla(), new ArrayList<>());
                }
                Double rsi = new RSI(this.diasPassados.get(diaEmpresa.getSigla()), 7).calcula();
                if(rsi < 30 && rsi != 0){
                    compra(diaEmpresa);
                } else if(rsi > 70){
                    vendeAcoesEmpresa(diaEmpresa);
                } else {
                    
                }
                recalculaFeromonios();
                diasPassados.get(diaEmpresa.getSigla()).add(diaEmpresa.getValorFechamento());
            }
            List<String> listTemp = new ArrayList<>(diasTeste);
            if(cont+1 < diasTeste.size()){
                String diaTemp = listTemp.get(cont + 1);
                if(!mesAtual.equals(diaTemp.substring(5,7))){
                    fechaMes(ultimoDia);
                }
            }            
            cont++;
            if(cont == diasTeste.size()){
                fechaMes(ultimoDia);
                fechaAno(ultimoDia);
            }
            
        } 
    }
    
    private boolean compra(Dia diaEmpresa){
        Empresa emp = portfolio.get(Empresas.valueOf(diaEmpresa.getSigla()));
        if(emp.getQtdAcoes() > 0){ //se tem ações, não compra mais
            movimentoDia.get("Ja tinham acoes").add(emp);
            return false;
        } else {
            Double investimento = (emp.getQtdFeromoniosDeInvestimento()*carteira)/100;
            carteira = carteira - investimento;
            carteiraVirtual = carteiraVirtual - investimento;
            Double sobra = investe(emp, investimento, diaEmpresa);
            carteira = carteira + sobra;
            carteiraVirtual = carteiraVirtual + sobra;
            movimentoDia.get("Compradas").add(emp);
            return true;
        }
    }
   
    private Double investe(Empresa emp, Double investimento, Dia diaEmpresa){
        Double qtdAcoes = (investimento/diaEmpresa.getValorAbertura());
        emp.setQtdAcoes(qtdAcoes.intValue());
        emp.setValorCompraAcao(diaEmpresa.getValorAbertura());
        return investimento - (qtdAcoes.intValue()*diaEmpresa.getValorAbertura());
    }
    
    private boolean vendeAcoesEmpresa(Dia diaEmpresa){
        Empresa emp = portfolio.get(Empresas.valueOf(diaEmpresa.getSigla()));
        if(emp.getQtdAcoes() < 1){ //se nao tem acoes, nao vende
            movimentoDia.get("Nao tinham acoes").add(emp);
            return false;
        } else {
            carteira = carteira + (emp.getQtdAcoes()*diaEmpresa.getValorAbertura());
            carteiraVirtual = carteiraVirtual + (emp.getQtdAcoes()*diaEmpresa.getValorAbertura());
            emp.setQtdAcoes(0);
            emp.setValorCompraAcao(0D);
            movimentoDia.get("Vendidas").add(emp);
            return true;
        }
    }
    
    private boolean recalculaFeromonios(){
        //System.out.println("RECALCULA FEROMONIOS");
        //evaporacao e incremento
        return true;
    }
    
    private void fechaMes(Dia ultimoDia){
        Empresa empresa;
        Double tempMaior = Double.MIN_VALUE;
        Double tempMenor = Double.MIN_VALUE;
        Double lucroEmpresa;
        Mes mes = relatorio2016.get(ultimoDia.getData().toString().substring(5, 7));;
        for(Empresas emp: Empresas.values()){
            empresa = portfolio.get(emp);
            lucroEmpresa = (empresa.getQtdAcoes()*ultimoDia.getValorAbertura()) - (empresa.getQtdAcoes()*empresa.getValorCompraAcao()); //quanto ta valendo - quanto pagou
            carteiraVirtual = carteiraVirtual + (empresa.getQtdAcoes()* ultimoDia.getValorAbertura());
            if(lucroEmpresa > tempMaior ){
                mes.setMaiorLucro(lucroEmpresa);
                mes.setNomeMaiorLucro(empresa.getSiglaEmpresa());
                tempMaior = lucroEmpresa;
            } else if(lucroEmpresa < tempMenor){
                mes.setMenorLucro(lucroEmpresa);
                mes.setNomeMenorLucro(empresa.getSiglaEmpresa());
                tempMenor = lucroEmpresa;
            }
        }
        mes.setCarteiraMes(carteiraVirtual);
        carteiraVirtual = carteira;
    }
    
    private void fechaAno(Dia ultimoDia){
        Empresa empresa;
        Double tempMaior = Double.MIN_VALUE;
        Double tempMenor = Double.MIN_VALUE;
        Double lucroEmpresa;
        for(Empresas emp: Empresas.values()){
            empresa = portfolio.get(emp);
            lucroEmpresa = (empresa.getQtdAcoes()*ultimoDia.getValorAbertura()) - (empresa.getQtdAcoes()*empresa.getValorCompraAcao()); //quanto ta valendo - quanto pagou
            carteira = carteira + (empresa.getQtdAcoes()* ultimoDia.getValorAbertura());
            //carteiraVirtual = carteiraVirtual + (empresa.getQtdAcoes()* ultimoDia.getValorAbertura());
            empresa.setQtdAcoes(0);
            empresa.setValorCompraAcao(0D);           
            if(lucroEmpresa > tempMaior ){
                relatorioAno.setMaiorLucro(lucroEmpresa);
                relatorioAno.setNomeMaiorLucro(empresa.getSiglaEmpresa());
                tempMaior = lucroEmpresa;
            } else if(lucroEmpresa < tempMenor){
                relatorioAno.setMenorLucro(lucroEmpresa);
                relatorioAno.setNomeMenorLucro(empresa.getSiglaEmpresa());
                tempMenor = lucroEmpresa;
            }
        }
        relatorioAno.setCarteiraAno(carteira);
    }

    @Override
    public String devolveValorPortfolio() {
        Double perc = ((relatorioAno.getCarteiraAno() - CARTEIRA_INICIAL)/CARTEIRA_INICIAL) * 100;
        return "\nO investimento de " + CARTEIRA_INICIAL + " foi transformado em " + relatorioAno.getCarteiraAno() 
                + ". Isto equivale a um aumento de " + perc.toString() + "%.";
    }

    @Override
    public String devolveResultadosMesAMes() {
        Mes mes;
        StringBuilder relatorio = new StringBuilder();
        relatorio.append("----------------\nRELATORIO MENSAL\n----------------\n\n");
        for(Integer i=1;i<13;i++){
            if(i < 10){
                mes = relatorio2016.get("0" + i.toString());
                relatorio.append(pegaMes("0" + i.toString()) + "\n");
            } else {
                mes = relatorio2016.get(i.toString());
                relatorio.append(pegaMes(i.toString()) + "\n");
            }
            relatorio.append("Carteira: " + mes.getCarteiraMes() + " (" + ((mes.getCarteiraMes() - CARTEIRA_INICIAL)/CARTEIRA_INICIAL) * 100 + ")\n");
            relatorio.append("Maior Lucro: " + mes.getNomeMaiorLucro() + " com " + mes.getMaiorLucro() + " de retorno.\n");
            relatorio.append("Menor Lucro: " + mes.getNomeMenorLucro() + " com " + mes.getMenorLucro() + " de retorno.\n-------------------------------------\n");
        }
        return relatorio.toString();
    }
    
    private String pegaMes(String numero){
        switch (numero) {
            case "01":
                return "Janeiro";
            case "02":
                return "Fevereiro";
            case "03":
                return "Marco";
            case "04":
                return "Abril";
            case "05":
                return "Maio";
            case "06":
                return "Junho";
            case "07":
                return "Julho";
            case "08":
                return "Agosto";
            case "09":
                return "Setembro";
            case "10":
                return "Outubro";
            case "11":
                return "Novembro";
            case "12":
                return "Dezembro";
        }
        return "";
    }

    @Override
    public String devolveAcaoMaiorGanho() {
        return "Maior ganho";
    }

    @Override
    public String devolveAcaoMaiorPrejuizo() {
        return "Menor ganho";
    }
    
    class Empresa {
        
        private Empresas siglaEmpresa;
        private Integer qtdAcoes; //quantas acoes da empresa eu tenho
        private Double valorCompraAcao; //por quanto cada acao foi comprada
        private Double qtdFeromoniosDeInvestimento; //qtd a ser investida dos 100% na empresa. Na venda é vendido tudo
        private boolean vendida = false;
        private boolean comprada = false;
        
        public Empresa(Empresas siglaEmpresa, Integer qtdAcoes, Double valorCompraAcao, Double qtdFeromoniosDeInvestimento) {
            this.siglaEmpresa = siglaEmpresa;
            this.qtdAcoes = qtdAcoes;
            this.valorCompraAcao = valorCompraAcao;
            this.qtdFeromoniosDeInvestimento = qtdFeromoniosDeInvestimento;
        }
        
        public Empresas getSiglaEmpresa() {
            return siglaEmpresa;
        }

        public void setSiglaEmpresa(Empresas siglaEmpresa) {
            this.siglaEmpresa = siglaEmpresa;
        }

        public Integer getQtdAcoes() {
            return qtdAcoes;
        }

        public void setQtdAcoes(Integer qtdAcoes) {
            this.qtdAcoes = qtdAcoes;
        }

        public Double getQtdFeromoniosDeInvestimento() {
            return qtdFeromoniosDeInvestimento;
        }

        public void setQtdFeromoniosDeInvestimento(Double qtdFeromoniosDeInvestimento) {
            this.qtdFeromoniosDeInvestimento = qtdFeromoniosDeInvestimento;
        }

        public Double getValorCompraAcao() {
            return valorCompraAcao;
        }

        public void setValorCompraAcao(Double valorCompraAcao) {
            this.valorCompraAcao = valorCompraAcao;
        }

        public boolean isVendida() {
            return vendida;
        }

        public void setVendida(boolean vendida) {
            this.vendida = vendida;
        }

        public boolean isComprada() {
            return comprada;
        }

        public void setComprada(boolean comprada) {
            this.comprada = comprada;
        }
        
        
              
    }
    
    class Mes {
        private Double carteiraMes = 0D;
        private Double maiorLucro = 0D;
        private Double menorLucro = 0D;
        private Empresas nomeMaiorLucro;
        private Empresas nomeMenorLucro;

        public Double getCarteiraMes() {
            return carteiraMes;
        }

        public void setCarteiraMes(Double carteiraMes) {
            this.carteiraMes = carteiraMes;
        }

        public Double getMaiorLucro() {
            return maiorLucro;
        }

        public void setMaiorLucro(Double maiorLucro) {
            this.maiorLucro = maiorLucro;
        }

        public Double getMenorLucro() {
            return menorLucro;
        }

        public void setMenorLucro(Double menorLucro) {
            this.menorLucro = menorLucro;
        }

        public Empresas getNomeMaiorLucro() {
            return nomeMaiorLucro;
        }

        public void setNomeMaiorLucro(Empresas nomeMaiorLucro) {
            this.nomeMaiorLucro = nomeMaiorLucro;
        }

        public Empresas getNomeMenorLucro() {
            return nomeMenorLucro;
        }

        public void setNomeMenorLucro(Empresas nomeMenorLucro) {
            this.nomeMenorLucro = nomeMenorLucro;
        }
                
    }
    
    class Ano {
        private Double carteiraAno = 0D;
        private Double maiorLucro = 0D;
        private Double menorLucro = 0D;
        private Empresas nomeMaiorLucro;
        private Empresas nomeMenorLucro;

        public Double getCarteiraAno() {
            return carteiraAno;
        }

        public void setCarteiraAno(Double carteiraAno) {
            this.carteiraAno = carteiraAno;
        }
        
        public Double getMaiorLucro() {
            return maiorLucro;
        }

        public void setMaiorLucro(Double maiorLucro) {
            this.maiorLucro = maiorLucro;
        }

        public Double getMenorLucro() {
            return menorLucro;
        }

        public void setMenorLucro(Double menorLucro) {
            this.menorLucro = menorLucro;
        }

        public Empresas getNomeMaiorLucro() {
            return nomeMaiorLucro;
        }

        public void setNomeMaiorLucro(Empresas nomeMaiorLucro) {
            this.nomeMaiorLucro = nomeMaiorLucro;
        }

        public Empresas getNomeMenorLucro() {
            return nomeMenorLucro;
        }

        public void setNomeMenorLucro(Empresas nomeMenorLucro) {
            this.nomeMenorLucro = nomeMenorLucro;
        }
        
        
    }
     
    enum Empresas {
        WEGE3,
        NATU3,
        SBSP3,
        CIEL3,
        CSNA3,
        VIVT3,
        GRND3,
        JSLG3,
        LREN3,
        UGPA3;
    }
    
}
