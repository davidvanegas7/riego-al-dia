package com.example.david.mangualo;

import android.util.Log;

/**
 * Created by david on 28/02/16.
 */
public class Riegos {

    public int profundidadCultivo;
    public double porcentajeRiego;
    public double valorDA;
    public double valorCC;
    public double valorArea;
    public double humedad_arduino = 10;
    public double valor_caudal=0;
    public double volumen_real;
    public double tiempo;
    public double humedad_suelo;

    public Riegos(){}

    public Riegos(int profundidad, double porcentaje, double DA, double CC, double area) {
        this.profundidadCultivo = profundidad;
        this.porcentajeRiego = porcentaje;
        this.valorDA = DA;
        this.valorCC = CC;
        this.valorArea = area;
    }

    public void humedad_arduino(double humedad)
    {
        this.humedad_arduino = humedad;
    }

    public void caudal(double dato){
        this.valor_caudal = dato;
    }

    public double caudal()
    {
        return valor_caudal;
    }

    public void calcularTiempo()
    {
        humedad_suelo = valorCC - humedad_arduino;
        double lamina_de_agua = (humedad_suelo * valorDA * profundidadCultivo)/100;
        double volumen_riego = lamina_de_agua * 100;
         volumen_real = volumen_riego * porcentajeRiego;
         tiempo = volumen_real / (valor_caudal );

        volumen_real = Math.round(volumen_real * 100) / 100;
        tiempo = Math.round(tiempo * 100) / 100;

        Log.d("x", "------------------------ ");
        Log.d("x", "volumen real: " + volumen_real);
        Log.d("x", "tiempo: " + tiempo);
        Log.d("x", "humedad sin funcion: " + ((humedad_arduino-132.3)/(-0.1292)) );
        Log.d("x", "humedad con funcion: " + humedad_arduino);
        Log.d("x", "humedad del suelo: " + humedad_suelo);
        Log.d("x", "Lamina de agua: " + lamina_de_agua);
        Log.d("x", "volumen de riego: " + volumen_riego);
        Log.d("x", "------------------------ ");

    }

    public String retornarResultado()
    {
        if(humedad_suelo < 0){
            return "Mangualo calculo que usted debe: \n\nNO REGAR";
        }
        return "Mangualo calculo que usted debe:\n\nRegar en tu terreno "+volumen_real+" metros cúbicos de agua\n\nSu sistema de riego se demorara aproximadamente "+tiempo+" minutos";
    }

    public double tiempo()
    {
        return tiempo;
    }

    public double volumenReal()
    {
        return volumen_real;
    }

}
