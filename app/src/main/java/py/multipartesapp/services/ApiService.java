package py.multipartesapp.services;

import py.multipartesapp.beans.Producto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("productos")
    Call<Producto> obtenerProductos();
}