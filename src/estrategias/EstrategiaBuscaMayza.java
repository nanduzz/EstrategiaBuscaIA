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
    
    private final Map<Empresas, Empresa> portfolio = new HashMap<>();
    private final Map<String, Mes> relatorio2016 = new HashMap<>();
    private final Ano relatorioAno = new Ano();
    
    private final List<Dia> diasEmpresas = new ArrayList<>();

    public EstrategiaBuscaMayza(){
        this.carteira = 100000D;
        this.carteiraVirtual = 100000D;
        for(Empresas e: Empresas.values()){
            this.portfolio.put(e, new Empresa(e, 0, 0D, 10D));
        }
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
                    Empresa emp = portfolio.get(Empresas.valueOf(diaEmpresa.getSigla()));
                    emp.setComprado(false);
                    emp.setVendido(false);
                    emp.setEvaporar(true);
                }
                diasPassados.get(diaEmpresa.getSigla()).add(diaEmpresa.getValorFechamento());
                diasEmpresas.add(diaEmpresa);
            }
            recalculaFeromonios();          
            diasEmpresas.clear();
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
                    Empresa emp = portfolio.get(Empresas.valueOf(diaEmpresa.getSigla()));
                    emp.setComprado(false);
                    emp.setVendido(false);
                    emp.setEvaporar(true);
                }
                diasPassados.get(diaEmpresa.getSigla()).add(diaEmpresa.getValorFechamento());
                diasEmpresas.add(diaEmpresa);
            }
            recalculaFeromonios();
            diasEmpresas.clear();
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
            return false;
        } else {
            emp.setComprado(true);
            emp.setVendido(false);
            emp.setEvaporar(false);
            Double investimento = (emp.getQtdFeromoniosDeInvestimento()*carteira)/100;
            carteira = carteira - investimento;
            carteiraVirtual = carteiraVirtual - investimento;
            Double sobra = investe(emp, investimento, diaEmpresa);
            carteira = carteira + sobra;
            carteiraVirtual = carteiraVirtual + sobra;
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
            return false;
        } else {
            emp.setVendido(true);
            emp.setComprado(false);
            emp.setEvaporar(false);
            carteira = carteira + (emp.getQtdAcoes()*diaEmpresa.getValorAbertura());
            carteiraVirtual = carteiraVirtual + (emp.getQtdAcoes()*diaEmpresa.getValorAbertura());
            emp.setQtdAcoesVendidas(emp.getQtdAcoes());
            emp.setValorVendaAcoes(diaEmpresa.getValorAbertura());
            emp.setQtdAcoes(0);
            emp.setValorCompraAcao(0D);
            return true;
        }
    }
    
    private boolean recalculaFeromonios(){
        Empresa empresa;
        Empresa lucrativa = null;
        Empresa prejuizo = null;       
        Dia dia = null;
        Double tempMaior = Double.MIN_VALUE;
        Double tempMenor = Double.MIN_VALUE;
        Double lucroEmpresa = null;
        Double porcPrejuizo = 0D;
        Double temp = 0D;
        Double dist = 0D;
        List<Empresa> evaporar = new ArrayList<>();
        for(Empresas emp: Empresas.values()){
            empresa = portfolio.get(emp);
            for(Dia d: diasEmpresas){
                if(d.getSigla().contains(emp.toString())){
                   dia = d;
                   break;
                } 
            }
            if(dia != null){
                if(empresa.isComprado()){    
                    lucroEmpresa = (empresa.getQtdAcoes()*dia.getValorFechamento()) - (empresa.getQtdAcoes()*empresa.getValorCompraAcao());
                    temp = ((empresa.getQtdAcoes()*empresa.getValorCompraAcao())*100)/(empresa.getQtdAcoes()*dia.getValorFechamento());
                } else if(empresa.isVendido()){ 
                    lucroEmpresa = (empresa.getQtdAcoesVendidas()*empresa.getValorVendaAcoes()) - (empresa.getQtdAcoesVendidas()*dia.getValorFechamento());
                    temp = ((empresa.getQtdAcoesVendidas()*dia.getValorFechamento())*100)/(empresa.getQtdAcoesVendidas()*empresa.getValorVendaAcoes());
                } else if(empresa.isEvaporar()){
                    lucroEmpresa = 0D;
                    evaporar.add(empresa);
                }

                if((lucroEmpresa > 0) && (lucroEmpresa > tempMaior)){                
                    tempMaior = lucroEmpresa;
                    lucrativa = empresa;
                } else if((lucroEmpresa < 0) && (lucroEmpresa < tempMenor)){               
                    tempMenor = lucroEmpresa;
                    prejuizo = empresa;
                    porcPrejuizo = temp-100;
                }  
            }
        }
        
        if(lucrativa != null && prejuizo != null){  
            Empresa lucra = portfolio.get(lucrativa.getSiglaEmpresa());
            Empresa preju = portfolio.get(prejuizo.getSiglaEmpresa());
            dist = (preju.getQtdFeromoniosDeInvestimento()*porcPrejuizo)/100;
            lucra.setQtdFeromoniosDeInvestimento(lucrativa.getQtdFeromoniosDeInvestimento() + dist);
            preju.setQtdFeromoniosDeInvestimento(preju.getQtdFeromoniosDeInvestimento() - dist);
            //lucra.setQtdFeromoniosDeInvestimento(lucrativa.getQtdFeromoniosDeInvestimento() + (prejuizo.getQtdFeromoniosDeInvestimento()/2));
            //preju.setQtdFeromoniosDeInvestimento(prejuizo.getQtdFeromoniosDeInvestimento() - (prejuizo.getQtdFeromoniosDeInvestimento()/2));
        } 
        if(!evaporar.isEmpty() && evaporar.size()<10){
            Double feromonios = 0D;
            List<Empresas> aSeremEvaporadas = new ArrayList<>();
            for(Empresa e: evaporar){
                feromonios = feromonios + ((10*e.getQtdFeromoniosDeInvestimento())/100); //tiro 10% doa feromonios dela
                e.setQtdFeromoniosDeInvestimento(e.getQtdFeromoniosDeInvestimento() - ((10*e.getQtdFeromoniosDeInvestimento())/100));
                aSeremEvaporadas.add(e.getSiglaEmpresa());
            }
            Double distribuir = feromonios/(10-evaporar.size()); //divide os feromonios entre as empresas que foram movimentadas
            for(Empresas emp: Empresas.values()){
                if(!aSeremEvaporadas.contains(emp)){
                    empresa = portfolio.get(emp);
                    empresa.setQtdFeromoniosDeInvestimento(empresa.getQtdFeromoniosDeInvestimento() + distribuir);
                }
            }
        }
         Double cont = 0D;
        for(Empresas e: Empresas.values()){
            cont = cont + portfolio.get(e).getQtdFeromoniosDeInvestimento();
            System.out.println("FERO " + portfolio.get(e).getQtdFeromoniosDeInvestimento());
        }
        System.out.println("TOTAL " + cont);
        
        
//        Empresa empresa;
//        Empresa lucrativa = null;
//        Empresa prejuizo = null;
//        Double feroPrejuizoRelativo = null;
//        Dia dia = null;
//        Double tempMaior = Double.MIN_VALUE;
//        Double tempMenor = Double.MIN_VALUE;
//        Double lucroEmpresa = null;
//        List<Empresa> dividirFeromonios = new ArrayList<>();
//        List<Empresa> evaporar = new ArrayList<>();
//        for(Empresas emp: Empresas.values()){
//            empresa = portfolio.get(emp);
//            for(Dia d: diasEmpresas){
//                if(d.getSigla().contains(emp.toString())){
//                   dia = d;
//                   break;
//                } else{
//                    dia = null;
//                }
//            }
//            if(dia!=null){
//            if(empresa.isComprado()){    
//                lucroEmpresa = dia.getValorFechamento() - empresa.getValorCompraAcao();
//            } else if(empresa.isVendido()){ 
//                lucroEmpresa = empresa.getValorVendaAcoes() - dia.getValorFechamento();
//            } else if(empresa.isEvaporar()){
//                lucroEmpresa = 0D;
//                evaporar.add(empresa);
//            }
//             
//            if((lucroEmpresa > 0) && (lucroEmpresa > tempMaior)){                
//                tempMaior = lucroEmpresa;
//                lucrativa = empresa;
//            } else if((lucroEmpresa < 0) && (lucroEmpresa < tempMenor)){               
//                tempMenor = lucroEmpresa;
//                prejuizo = empresa;
//                feroPrejuizoRelativo = (lucroEmpresa*100)/dia.getValorFechamento();
//            } else {
//                dividirFeromonios.add(empresa);
//            }
//            }
//        }
//        
//        if(lucrativa != null){
//            if(prejuizo != null) {
//                Double feroASerRepassado = prejuizo.getQtdFeromoniosDeInvestimento() - ((prejuizo.getQtdFeromoniosDeInvestimento()*feroPrejuizoRelativo)/100);
//                lucrativa.setQtdFeromoniosDeInvestimento(lucrativa.getQtdFeromoniosDeInvestimento() + feroASerRepassado);
//            } else {
//                for(Empresa e: dividirFeromonios){
//                    lucrativa.setQtdFeromoniosDeInvestimento(lucrativa.getQtdFeromoniosDeInvestimento() + 1);
//                    e.setQtdFeromoniosDeInvestimento(e.getQtdFeromoniosDeInvestimento()-1);
//                }
//            }
//        }
//        if(!evaporar.isEmpty()){
//            Integer evap = evaporar.size()/(10-evaporar.size());
//            for(Empresas emp: Empresas.values()){
//                empresa = portfolio.get(emp);
//                
//                empresa.setQtdFeromoniosDeInvestimento(empresa.getQtdFeromoniosDeInvestimento()+evap);
//            }
//        }
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
        Double cont = 0D;
        for(Empresas e: Empresas.values()){
            cont = cont + portfolio.get(e).getQtdFeromoniosDeInvestimento();
            System.out.println("FERO " + portfolio.get(e).getQtdFeromoniosDeInvestimento());
        }
        System.out.println("TOTAL " + cont);
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
        private Double valorVendaAcoes = 0D;
        private Integer qtdAcoesVendidas = 0;
        private Double qtdFeromoniosDeInvestimento; //qtd a ser investida dos 100% na empresa. Na venda é vendido tudo
        boolean evaporar = false;
        boolean vendido = false;
        boolean comprado = false;
        
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

        public boolean isEvaporar() {
            return evaporar;
        }

        public void setEvaporar(boolean evaporar) {
            this.evaporar = evaporar;
        }

        public boolean isVendido() {
            return vendido;
        }

        public void setVendido(boolean vendido) {
            this.vendido = vendido;
        }

        public boolean isComprado() {
            return comprado;
        }

        public void setComprado(boolean comprado) {
            this.comprado = comprado;
        }

        public Double getValorVendaAcoes() {
            return valorVendaAcoes;
        }

        public void setValorVendaAcoes(Double valorVendaAcoes) {
            this.valorVendaAcoes = valorVendaAcoes;
        }

        public Integer getQtdAcoesVendidas() {
            return qtdAcoesVendidas;
        }

        public void setQtdAcoesVendidas(Integer qtdAcoesVendidas) {
            this.qtdAcoesVendidas = qtdAcoesVendidas;
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
