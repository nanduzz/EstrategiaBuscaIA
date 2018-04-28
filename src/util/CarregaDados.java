/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import modelo.Dia;

/**
 *
 * @author Camargo
 */
public class CarregaDados {

    public static String[] SIGLAS = new String[]{"WEGE3", "NATU3", "SBSP3", "CIEL3", "CSNA3", "VIVT3", "GRND3", "JSLG3", "LREN3", "UGPA3"};
    
    public static List<Dia> carregaDados(File file1, File file2) {
        List dias = carregaDados(file1);
        dias.addAll(carregaDados(file2));
        return dias;
    }

    public static List<Dia> carregaDados(File file) {
        List dias = new ArrayList();

        try {
            Scanner input;

            input = new Scanner(file);
            input.nextLine();

            while (input.hasNextLine()) {
                String line = input.nextLine();
                String sigla = line.substring(12, 24).trim();
                if (Arrays.asList(SIGLAS).contains(sigla)) {

                    String empresa = line.substring(27, 39).trim();
                    LocalDate data = LocalDate.parse(line.substring(02, 10), DateTimeFormatter.BASIC_ISO_DATE);
                    Double precoAbertura = Double.valueOf(line.substring(56, 69)) /100;
                    Double precoMax = Double.valueOf(line.substring(69, 82)) / 100;
                    Double precoMin = Double.valueOf(line.substring(82, 95)) / 100;
                    Double precoMedio = Double.valueOf(line.substring(95, 108)) /100;
                    Double precoFechamento = Double.valueOf(line.substring(108, 121)) / 100;

                    //Double nrNegocios = Double.valueOf(line.substring(147, 152));
                    //Double total = Double.valueOf(line.substring(152, 170));
                    dias.add(new Dia(empresa, sigla, data, precoAbertura, precoFechamento, precoMin, precoMax, precoMedio));
                }
            }
            input.close();

        } catch (FileNotFoundException | NumberFormatException ex) {
            throw new Error("Houve um erro ao abrir o arquivo.");
        } catch (StringIndexOutOfBoundsException | DateTimeParseException ex) {
            return dias;
        }
        return dias;
    }

}
