package com.valledelsol.zonasriesgo.adapter;

/**
 * Puerto agnostico al proveedor climatico (patron Adapter).
 */
public interface WeatherDataPort {

    WeatherSnapshot obtenerCondiciones(double latitud, double longitud);
}
