//package py.multipartesapp.services;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import java.util.List;
//
//import py.multipartesapp.beans.Producto;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class ProductoHttpClient implements Callback<List<Producto>> {
//
//    static final String BASE_URL = "https://git.eclipse.org/r/";
//
//    public void start() {
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create(gson))
//                .build();
//
//        ApiService apiService = retrofit.create(ApiService.class);
//
//        Call<List<Producto>> call = apiService.obtenerProductos();
//        call.enqueue(this);
//
//    }
//
//    @Override
//    public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
//        if(response.isSuccessful()) {
//            List<Producto> productoList = response.body();
//            productoList.forEach(producto -> System.out.println(productoList.toString()));
//        } else {
//            System.out.println(response.errorBody());
//        }
//    }
//
//    @Override
//    public void onFailure(Call<List<Producto>> call, Throwable t) {
//        t.printStackTrace();
//    }
//
//    private class List<T> {
//    }
//}
